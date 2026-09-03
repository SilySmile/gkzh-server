package com.gkzh.app.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gkzh.sszctop.service.SszctopRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * 通用游戏房间通道：频道以 gameType/gameId/roomCode 隔离。
 * 心跳只维护连接存活；业务排序仍通过 REST 提交，避免 WebSocket 消息绕过版本校验。
 */
@Component
public class GameRoomSocketHandler extends TextWebSocketHandler {
    @Autowired
    private SszctopRoomService roomService;
    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();
    private final Map<String, Set<WebSocketSession>> channels = new ConcurrentHashMap<>();
    private final Map<String, Long> lastSeen = new ConcurrentHashMap<>();
    private final Map<String, DragLock> dragLocks = new ConcurrentHashMap<>();

    public String ticket(String channel, Long userId, String name) {
        String t = UUID.randomUUID().toString().replace("-", "");
        tickets.put(t, new Ticket(channel, userId, name, System.currentTimeMillis() + 30000));
        return t;
    }

    public Ticket consume(String ticket) {
        Ticket t = tickets.remove(ticket);
        return t == null || t.expire < System.currentTimeMillis() ? null : t;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession s) throws Exception {
        Ticket t = (Ticket) s.getAttributes().get("ticket");
        if (t == null) {
            s.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        channels.computeIfAbsent(t.channel, k -> ConcurrentHashMap.newKeySet()).add(s);
        lastSeen.put(s.getId(), System.currentTimeMillis());
        send(s, event("socket.ready", t.userId));
        broadcast(t.channel, event("member.online", t.name));
    }

    @Override
    protected void handleTextMessage(WebSocketSession s, TextMessage msg) throws Exception {
        lastSeen.put(s.getId(), System.currentTimeMillis());
        Ticket t = (Ticket) s.getAttributes().get("ticket");
        JSONObject data = JSON.parseObject(msg.getPayload());
        String type = data.getString("type");
        if ("heartbeat.ping".equals(type)) {
            send(s, event("heartbeat.pong", null));
            return;
        }
        Long career = data.getLong("careerId");
        if (career == null) return;
        String key = t.channel + ":" + career;
        if ("career.lock".equals(type)) {
            DragLock old = dragLocks.putIfAbsent(key, new DragLock(s.getId(), System.currentTimeMillis() + 8000));
            if (old == null || old.sessionId.equals(s.getId())) {
                dragLocks.put(key, new DragLock(s.getId(), System.currentTimeMillis() + 8000));
                broadcast(t.channel, event("career.locked", map("careerId", career, "userId", t.userId, "name", t.name)));
            }
        } else if ("career.unlock".equals(type) && owned(key, s.getId())) {
            dragLocks.remove(key);
            broadcast(t.channel, event("career.unlocked", map("careerId", career)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession s, CloseStatus status) {
        close(s, "连接断开");
    }

    @Scheduled(fixedDelay = 2000)
    public void heartbeat() {
        long now = System.currentTimeMillis();
        for (Set<WebSocketSession> list : channels.values())
            for (WebSocketSession s : new ArrayList<>(list))
                if (now - lastSeen.getOrDefault(s.getId(), 0L) > 12000) {
                    try {
                        s.close(CloseStatus.SESSION_NOT_RELIABLE);
                    } catch (IOException ignored) {
                    }
                    close(s, "心跳超时");
                }
        for (Map.Entry<String, DragLock> e : new ArrayList<>(dragLocks.entrySet()))
            if (e.getValue().expireAt <= now && dragLocks.remove(e.getKey(), e.getValue())) unlockNotice(e.getKey());
    }

    private boolean owned(String key, String sessionId) {
        DragLock lock = dragLocks.get(key);
        return lock != null && lock.sessionId.equals(sessionId);
    }

    private void unlockNotice(String key) {
        int p = key.lastIndexOf(':');
        if (p < 0) return;
        try {
            broadcast(key.substring(0, p), event("career.unlocked", map("careerId", Long.valueOf(key.substring(p + 1)))));
        } catch (NumberFormatException ignored) {
        }
    }

    private void close(WebSocketSession s, String reason) {
        lastSeen.remove(s.getId());
        Ticket t = (Ticket) s.getAttributes().get("ticket");
        if (t == null) return;
        Set<WebSocketSession> list = channels.get(t.channel);
        if (list == null || !list.remove(s)) return;
        for (Map.Entry<String, DragLock> e : new ArrayList<>(dragLocks.entrySet()))
            if (s.getId().equals(e.getValue().sessionId) && dragLocks.remove(e.getKey(), e.getValue()))
                unlockNotice(e.getKey());
        // 同一用户短暂重连时，新连接已在线则不能因为旧连接关闭而把该用户踢出房间。
        if (hasOtherActiveSession(list, t.userId, s.getId())) return;
        String[] p = t.channel.split("/", 3);
        if (p.length == 3 && "sszctop".equals(p[0])) try {
            roomService.disconnect(p[2], t.userId);
            broadcast(t.channel, event("member.disconnected", map("userId", t.userId, "name", t.name, "reason", reason)));
        } catch (Exception ignored) {
        }
    }

    /** 判断同一频道内该用户是否仍持有另一条可用连接。 */
    private boolean hasOtherActiveSession(Set<WebSocketSession> sessions, Long userId, String closedSessionId) {
        for (WebSocketSession session : sessions) {
            if (closedSessionId.equals(session.getId()) || !session.isOpen()) continue;
            Ticket ticket = (Ticket) session.getAttributes().get("ticket");
            if (ticket != null && Objects.equals(ticket.userId, userId)) return true;
        }
        return false;
    }

    public void broadcast(String channel, Object payload) {
        Set<WebSocketSession> list = channels.getOrDefault(channel, Collections.emptySet());
        for (WebSocketSession s : list) send(s, payload);
    }

    private void send(WebSocketSession s, Object o) {
        try {
            if (s.isOpen()) s.sendMessage(new TextMessage(JSON.toJSONString(o)));
        } catch (IOException ignored) {
        }
    }

    private static Map<String, Object> event(String type, Object data) {
        return map("type", type, "data", data);
    }

    private static Map<String, Object> map(Object... v) {
        Map<String, Object> r = new LinkedHashMap<>();
        for (int i = 0; i < v.length; i += 2) r.put(String.valueOf(v[i]), v[i + 1]);
        return r;
    }

    private static class DragLock {
        final String sessionId;
        final long expireAt;

        DragLock(String sessionId, long expireAt) {
            this.sessionId = sessionId;
            this.expireAt = expireAt;
        }
    }

    public static class Ticket {
        final String channel;
        final Long userId;
        final String name;
        final long expire;

        Ticket(String c, Long u, String n, long e) {
            channel = c;
            userId = u;
            name = n;
            expire = e;
        }
    }
}
