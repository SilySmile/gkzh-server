package com.gkzh.web.controller.zycck;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zycck.domain.ZycckCareerQuestion;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.dto.ZycckCareerBriefView;
import com.gkzh.zycck.mapper.ZycckActivityFeedbackMapper;
import com.gkzh.zycck.mapper.ZycckCareerQuestionMapper;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import com.gkzh.zycck.service.ZycckActivityFeedbackService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 后台统计详情返回浏览、清单及反馈，且保留已删除职业的编号。 */
class ZycckStatisticsDetailTest {
    @Test
    void statisticsDateRangeUsesInclusiveStartAndExclusiveNextDay() throws Exception {
        ZycckAdminController controller = new ZycckAdminController(null, null, null, null, null, null,
                null, null, null, null, null, null);
        Method method = ZycckAdminController.class.getDeclaredMethod("buildRecordQuery", Long.class, Long.class,
                Long.class, String.class, String.class, String.class, String.class, LocalDate.class, LocalDate.class);
        method.setAccessible(true);
        LocalDate start = LocalDate.of(2026, 10, 15), end = LocalDate.of(2026, 10, 16);
        QueryWrapper<?> query = (QueryWrapper<?>) method.invoke(controller, 3L, 9L, null, null, null, null, null, start, end);
        assertTrue(query.getSqlSegment().contains("scan_time >= "));
        assertTrue(query.getSqlSegment().contains("scan_time < "));
        assertTrue(query.getParamNameValuePairs().containsValue(LocalDateTime.of(2026, 10, 15, 0, 0)));
        assertTrue(query.getParamNameValuePairs().containsValue(LocalDateTime.of(2026, 10, 17, 0, 0)));
        InvocationTargetException error = assertThrows(InvocationTargetException.class,
                () -> method.invoke(controller, 3L, 9L, null, null, null, null, null, end, start));
        assertTrue(error.getCause() instanceof ServiceException);
    }

    @Test
    @SuppressWarnings("unchecked")
    void includesCareersInRecordedOrderAndFeedback() {
        ZycckRecordMapper records = mock(ZycckRecordMapper.class);
        ZycckCareerQuestionMapper careers = mock(ZycckCareerQuestionMapper.class);
        IActivityWeekService activities = mock(IActivityWeekService.class);
        when(activities.listInstances(null)).thenReturn(Collections.emptyList());
        ZycckRecord record = new ZycckRecord(); record.setRecordId(1L); record.setGameType("zycck");
        record.setViewedCareerIds("[9,8,99]"); record.setExplorationCareerIds("[8]");
        when(records.selectById(1L)).thenReturn(record);
        ZycckCareerQuestion first = new ZycckCareerQuestion(); first.setCareerQuestionId(8L); first.setCareerName("职业甲");
        ZycckCareerQuestion second = new ZycckCareerQuestion(); second.setCareerQuestionId(9L); second.setCareerName("职业乙");
        when(careers.selectBatchIds(any())).thenReturn(Arrays.asList(first, second));
        ZycckActivityFeedbackService feedback = new ZycckActivityFeedbackService(records, mock(ZycckActivityFeedbackMapper.class), activities);
        ZycckAdminController controller = new ZycckAdminController(null, careers, records, null, null, null,
                activities, null, null, null, null, feedback);
        AjaxResult response = controller.statisticsRecord(1L);
        Map<String, Object> detail = (Map<String, Object>) response.get("data");
        List<ZycckCareerBriefView> viewed = (List<ZycckCareerBriefView>) detail.get("viewedCareers");
        assertEquals(3, viewed.size());
        assertEquals("职业乙", viewed.get(0).getCareerName());
        assertEquals("职业甲", viewed.get(1).getCareerName());
        assertEquals(99L, viewed.get(2).getCareerId());
        assertTrue(viewed.get(2).getCareerName().contains("已删除"));
        assertEquals(1, ((List<?>) detail.get("explorationCareers")).size());
        assertNotNull(detail.get("activityFeedback"));
        assertTrue(((List<?>) detail.get("choices")).isEmpty());
    }
}
