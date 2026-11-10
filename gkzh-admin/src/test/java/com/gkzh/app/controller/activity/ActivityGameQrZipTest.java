package com.gkzh.app.controller.activity;

import com.gkzh.activity.domain.week.GkzhActivityArea;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.web.controller.activity.GkzhActivityWeekController;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 活动配置“一键下载全部游戏二维码”的打包规则。
 * 压缩包内每个 PNG 必须以后台游戏名称命名，重名游戏追加区域名或序号，且附带二维码清单。
 */
class ActivityGameQrZipTest {

    /** 正常场景：包内条目名与游戏名称一一对应，并包含二维码清单与可识别的 PNG。 */
    @Test
    void zipEntryNamesFollowGameTitles() throws Exception {
        GkzhActivityWeekController controller = controllerWithGames(
                instance(11L, "生涯活动"),
                area(101L, "区域一"),
                Collections.singletonList(game(1001L, "职愿探究", 11L, 101L)),
                area(102L, "区域二"),
                Collections.singletonList(game(1002L, "职面未来", 11L, 102L)));

        ResponseEntity<byte[]> response = controller.downloadGameQrCodes(11L, 5L);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getHeaders().getContentDisposition());
        String disposition = response.getHeaders().getContentDisposition().toString();
        assertTrue(disposition.contains("attachment"), "压缩包必须按附件下载: " + disposition);

        Map<String, byte[]> entries = unzip(response.getBody());
        assertTrue(entries.containsKey("职愿探究.png"), "二维码文件名应为游戏名称: " + entries.keySet());
        assertTrue(entries.containsKey("职面未来.png"), "二维码文件名应为游戏名称: " + entries.keySet());
        assertTrue(entries.containsKey("二维码清单.txt"), "应附带二维码清单: " + entries.keySet());
        assertPng(entries.get("职愿探究.png"));

        String manifest = new String(entries.get("二维码清单.txt"), StandardCharsets.UTF_8);
        assertTrue(manifest.contains("职愿探究.png") && manifest.contains("职愿探究"), "清单需记录文件名与游戏名称");
        assertTrue(manifest.contains("区域一") && manifest.contains("区域二"), "清单需记录区域，便于核对归属");
    }

    /** 同一活动下出现同名游戏时，文件名不能互相覆盖。 */
    @Test
    void duplicateGameTitlesAreDisambiguated() throws Exception {
        GkzhActivityWeekController controller = controllerWithGames(
                instance(12L, "就业活动"),
                area(201L, "区域一"),
                Collections.singletonList(game(2001L, "职愿探究", 12L, 201L)),
                area(202L, "区域二"),
                Collections.singletonList(game(2002L, "职愿探究", 12L, 202L)));

        Map<String, byte[]> entries = unzip(controller.downloadGameQrCodes(12L, 5L).getBody());

        assertTrue(entries.containsKey("职愿探究.png"), "首个同名游戏保留原名: " + entries.keySet());
        assertTrue(entries.containsKey("职愿探究-区域二.png"), "第二个同名游戏追加区域名: " + entries.keySet());
    }

    /** 活动下没有游戏时返回中文提示，避免前端保存空压缩包。 */
    @Test
    void emptyActivityReturnsChineseMessage() {
        GkzhActivityWeekController controller = controllerWithGames(
                instance(13L, "空活动"), area(301L, "区域一"), Collections.emptyList());

        ResponseEntity<byte[]> response = controller.downloadGameQrCodes(13L, 5L);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        String body = new String(response.getBody(), StandardCharsets.UTF_8);
        assertTrue(body.contains("暂无可下载的二维码"), "应返回中文业务提示: " + body);
    }

    /** 活动不存在时同样返回中文提示，而不是 404 空响应。 */
    @Test
    void missingActivityReturnsChineseMessage() {
        IActivityWeekService activities = mock(IActivityWeekService.class);
        GkzhActivityWeekController controller = new GkzhActivityWeekController();
        ReflectionTestUtils.setField(controller, "activityWeekService", activities);

        ResponseEntity<byte[]> response = controller.downloadGameQrCodes(99L, null);

        String body = new String(response.getBody(), StandardCharsets.UTF_8);
        assertTrue(body.contains("活动不存在"), "应返回中文业务提示: " + body);
    }

    private GkzhActivityWeekController controllerWithGames(GkzhActivityWeekInstance instance,
                                                           GkzhActivityArea firstArea, List<GkzhActivityGame> firstGames,
                                                           Object... rest) {
        IActivityWeekService activities = mock(IActivityWeekService.class);
        when(activities.getInstance(instance.getInstanceId())).thenReturn(instance);
        Map<Long, GkzhActivityArea> areaMap = new LinkedHashMap<>();
        Map<Long, List<GkzhActivityGame>> gameMap = new LinkedHashMap<>();
        areaMap.put(firstArea.getAreaId(), firstArea);
        gameMap.put(firstArea.getAreaId(), firstGames);
        for (int i = 0; i + 1 < rest.length; i += 2) {
            GkzhActivityArea area = (GkzhActivityArea) rest[i];
            areaMap.put(area.getAreaId(), area);
            gameMap.put(area.getAreaId(), (List<GkzhActivityGame>) rest[i + 1]);
        }
        List<GkzhActivityArea> areas = new ArrayList<>(areaMap.values());
        when(activities.listAreas(instance.getInstanceId(), 5L)).thenReturn(areas);
        when(activities.listAreas(instance.getInstanceId(), null)).thenReturn(areas);
        for (Map.Entry<Long, List<GkzhActivityGame>> entry : gameMap.entrySet()) {
            when(activities.listGames(entry.getKey())).thenReturn(entry.getValue());
        }
        GkzhActivityWeekController controller = new GkzhActivityWeekController();
        ReflectionTestUtils.setField(controller, "activityWeekService", activities);
        return controller;
    }

    private GkzhActivityWeekInstance instance(Long instanceId, String title) {
        GkzhActivityWeekInstance instance = new GkzhActivityWeekInstance();
        instance.setInstanceId(instanceId);
        instance.setTitle(title);
        return instance;
    }

    private GkzhActivityArea area(Long areaId, String title) {
        GkzhActivityArea area = new GkzhActivityArea();
        area.setAreaId(areaId);
        area.setTitle(title);
        return area;
    }

    private GkzhActivityGame game(Long gameId, String title, Long instanceId, Long areaId) {
        GkzhActivityGame game = new GkzhActivityGame();
        game.setGameId(gameId);
        game.setTitle(title);
        game.setInstanceId(instanceId);
        game.setAreaId(areaId);
        game.setGameType("zdyp");
        game.setConfig("{\"route\":\"zdyp\"}");
        return game;
    }

    private Map<String, byte[]> unzip(byte[] bytes) throws Exception {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries.put(entry.getName(), zip.readAllBytes());
            }
        }
        return entries;
    }

    private void assertPng(byte[] bytes) {
        assertNotNull(bytes);
        assertTrue(bytes.length > 8, "二维码 PNG 不能为空");
        assertEquals((byte) 0x89, bytes[0]);
        assertEquals((byte) 0x50, bytes[1]);
        assertEquals((byte) 0x4E, bytes[2]);
        assertEquals((byte) 0x47, bytes[3]);
    }
}
