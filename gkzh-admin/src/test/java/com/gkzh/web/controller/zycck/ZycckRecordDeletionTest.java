package com.gkzh.web.controller.zycck;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.domain.week.GkzhGameParticipation;
import com.gkzh.activity.mapper.week.GkzhGameParticipationMapper;
import com.gkzh.activity.service.impl.ActivityWeekServiceImpl;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.mapper.ZycckActivityFeedbackMapper;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import com.gkzh.zycck.service.ZycckActivityFeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/** 执行真实删除流程及 Spring 事务拦截，验证关联清理范围和失败回滚。 */
class ZycckRecordDeletionTest {
    private ZycckRecord record;
    private ZycckRecordMapper records;
    private ZycckActivityFeedbackMapper feedbacks;
    private GkzhGameParticipationMapper participations;
    private ZycckAdminController controller;
    private MemoryTables tables;
    private MemoryTransactionManager transactionManager;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        record = new ZycckRecord(); record.setRecordId(1L); record.setInstanceId(3L);
        record.setGameId(4L); record.setUserId(2L); record.setGameType("zycck");
        tables = new MemoryTables();
        // 同一学生本次游戏有两条旧记录，另外三条分别属于其他活动、游戏和学生。
        tables.participations.addAll(Arrays.asList(row(3L, 4L, 2L, "1"), row(3L, 4L, 2L, "0"),
                row(30L, 4L, 2L, "1"), row(3L, 40L, 2L, "1"), row(3L, 4L, 20L, "1")));
        records = mock(ZycckRecordMapper.class);
        feedbacks = mock(ZycckActivityFeedbackMapper.class);
        participations = mock(GkzhGameParticipationMapper.class);
        when(records.selectOne(any())).thenAnswer(call -> {
            QueryWrapper<ZycckRecord> query = call.getArgument(0);
            assertTrue(query.getSqlSegment().endsWith("FOR UPDATE"));
            return tables.recordPresent ? record : null;
        });
        when(records.deleteById(1L)).thenAnswer(call -> { tables.recordPresent = false; return 1; });
        when(feedbacks.deleteById(1L)).thenAnswer(call -> { tables.feedbackPresent = false; return 1; });
        when(participations.delete(any())).thenAnswer(call -> {
            QueryWrapper<GkzhGameParticipation> query = call.getArgument(0);
            int before = tables.participations.size();
            tables.participations.removeIf(row -> matches(query, row));
            return before - tables.participations.size();
        });
        ActivityWeekServiceImpl activities = new ActivityWeekServiceImpl();
        ReflectionTestUtils.setField(activities, "gameParticipationMapper", participations);
        ZycckActivityFeedbackService feedback = new ZycckActivityFeedbackService(records, feedbacks, activities);
        ZycckAdminController target = new ZycckAdminController(null, null, records, null, null, null,
                activities, null, null, null, null, feedback);
        transactionManager = new MemoryTransactionManager(tables);
        ProxyFactory factory = new ProxyFactory(target); factory.setProxyTargetClass(true);
        factory.addAdvice(new TransactionInterceptor(transactionManager, new AnnotationTransactionAttributeSource()));
        controller = (ZycckAdminController) factory.getProxy();
    }

    @Test
    void deletesRecordFeedbackAndAllMatchingParticipationsOnly() {
        assertEquals(200, controller.deleteRecord(1L).get("code"));
        assertFalse(tables.recordPresent);
        assertFalse(tables.feedbackPresent);
        assertEquals(3, tables.participations.size());
        assertEquals(30L, tables.participations.get(0).getInstanceId());
        assertEquals(40L, tables.participations.get(1).getGameId());
        assertEquals(20L, tables.participations.get(2).getUserId());
        assertEquals(1, transactionManager.commits);
        assertEquals(0, transactionManager.rollbacks);
    }

    @Test
    void previouslyClearedChildrenDoNotPreventRecordDeletion() {
        tables.participations.remove(0); tables.participations.remove(0);
        tables.feedbackPresent = false;
        doReturn(0).when(feedbacks).deleteById(1L);
        assertEquals(200, controller.deleteRecord(1L).get("code"));
        assertFalse(tables.recordPresent);
        assertFalse(tables.feedbackPresent);
        assertEquals(3, tables.participations.size());
        assertEquals(1, transactionManager.commits);
    }

    @Test
    void feedbackDeleteFailureRollsBackParticipationDeletion() {
        doThrow(new ServiceException("模拟反馈删除失败")).when(feedbacks).deleteById(1L);
        assertThrows(ServiceException.class, () -> controller.deleteRecord(1L));
        assertAllTablesIntact();
        verify(records, never()).deleteById(anyLong());
        assertEquals(1, transactionManager.rollbacks);
        assertEquals(0, transactionManager.commits);
    }

    @Test
    void parentDeleteFailureRollsBackBothChildTables() {
        doReturn(0).when(records).deleteById(1L);
        assertThrows(ServiceException.class, () -> controller.deleteRecord(1L));
        assertAllTablesIntact();
        assertEquals(1, transactionManager.rollbacks);
        assertEquals(0, transactionManager.commits);
    }

    @Test
    void missingOrOtherGameRecordDoesNotDeleteAnything() {
        tables.recordPresent = false;
        assertEquals(500, controller.deleteRecord(1L).get("code"));
        tables.recordPresent = true; record.setGameType("other");
        assertEquals(500, controller.deleteRecord(1L).get("code"));
        verify(participations, never()).delete(any());
        verify(feedbacks, never()).deleteById(anyLong());
        verify(records, never()).deleteById(anyLong());
    }

    @Test
    void incompleteContextCannotBroadenDeletionScope() {
        record.setInstanceId(null);
        assertThrows(ServiceException.class, () -> controller.deleteRecord(1L));
        record.setInstanceId(3L); record.setGameId(null);
        assertThrows(ServiceException.class, () -> controller.deleteRecord(1L));
        record.setGameId(4L); record.setUserId(null);
        assertThrows(ServiceException.class, () -> controller.deleteRecord(1L));
        assertAllTablesIntact();
        verify(participations, never()).delete(any());
        verify(feedbacks, never()).deleteById(anyLong());
        verify(records, never()).deleteById(anyLong());
    }

    private void assertAllTablesIntact() {
        assertTrue(tables.recordPresent);
        assertTrue(tables.feedbackPresent);
        assertEquals(5, tables.participations.size());
    }

    private static GkzhGameParticipation row(Long instanceId, Long gameId, Long userId, String status) {
        GkzhGameParticipation row = new GkzhGameParticipation();
        row.setInstanceId(instanceId); row.setGameId(gameId); row.setUserId(userId); row.setStatus(status);
        return row;
    }

    /** 根据正式 QueryWrapper 的绑定条件筛选测试表，验证不会误删其他活动/游戏/用户。 */
    private static boolean matches(QueryWrapper<GkzhGameParticipation> query, GkzhGameParticipation row) {
        String sql = query.getSqlSegment();
        Map<String, Object> values = query.getParamNameValuePairs();
        Matcher matcher = Pattern.compile("(instance_id|game_id|user_id) = #\\{ew.paramNameValuePairs.(MPGENVAL\\d+)\\}").matcher(sql);
        boolean matched = true; int criteria = 0;
        while (matcher.find()) {
            criteria++;
            Object actual = "instance_id".equals(matcher.group(1)) ? row.getInstanceId()
                    : "game_id".equals(matcher.group(1)) ? row.getGameId() : row.getUserId();
            matched &= java.util.Objects.equals(actual, values.get(matcher.group(2)));
        }
        assertEquals(3, criteria, "删除必须同时限定活动实例、游戏和用户");
        return matched;
    }

    private static class MemoryTables {
        boolean recordPresent = true;
        boolean feedbackPresent = true;
        List<GkzhGameParticipation> participations = new ArrayList<>();
    }

    /** 测试事务资源保存内存表快照，失败时恢复；无需连接或删除真实数据库数据。 */
    private static class MemoryTransactionManager extends AbstractPlatformTransactionManager {
        private final MemoryTables tables;
        private boolean recordBefore, feedbackBefore;
        private List<GkzhGameParticipation> participationsBefore;
        int commits, rollbacks;
        MemoryTransactionManager(MemoryTables tables) { this.tables = tables; }
        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) {
            recordBefore = tables.recordPresent; feedbackBefore = tables.feedbackPresent;
            participationsBefore = new ArrayList<>(tables.participations);
        }
        @Override protected void doCommit(DefaultTransactionStatus status) { commits++; }
        @Override protected void doRollback(DefaultTransactionStatus status) {
            rollbacks++; tables.recordPresent = recordBefore; tables.feedbackPresent = feedbackBefore;
            tables.participations = new ArrayList<>(participationsBefore);
        }
    }
}
