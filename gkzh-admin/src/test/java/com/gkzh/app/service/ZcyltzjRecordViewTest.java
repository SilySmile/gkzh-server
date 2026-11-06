package com.gkzh.app.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.zcyltzj.domain.ZcyltzjRecord;
import com.gkzh.zcyltzj.service.ZcyltzjRecordService;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 骰子局学生端展示数据回归：点数、试题、答案与结果字段必须完整返回。
 */
public class ZcyltzjRecordViewTest {

    private final IActivityWeekService activityWeekService = mock(IActivityWeekService.class);
    private final ZcyltzjRecordService service = new ZcyltzjRecordService(null, null, null, activityWeekService);

    @Test
    public void finishedRecordReturnsQuestionsAnswersAndResult() {
        GkzhActivityWeekInstance instance = new GkzhActivityWeekInstance();
        instance.setBizType("job_week");
        when(activityWeekService.getInstance(2L)).thenReturn(instance);

        ZcyltzjRecord record = baseRecord();
        record.setStatus("finished");
        record.setStage("finished");
        record.setPointNo(3);
        record.setPointName("DDL冲刺");
        record.setRedCount(4);
        record.setGreenCount(2);
        record.setPressureValue(67);
        record.setColor("red");

        JSONObject snapshot = new JSONObject();
        snapshot.put("pointNo", 3);
        snapshot.put("pointName", "DDL冲刺");
        snapshot.put("theme", "多任务并行/极限交付");
        snapshot.put("projection", "时间管理、抗压交付、多任务优先级");
        JSONArray questions = new JSONArray();
        JSONObject question = new JSONObject();
        question.put("questionId", 301L);
        question.put("questionNo", 1);
        question.put("content", "你通常提前多久开始赶DDL？");
        question.put("optionA", "一周以上");
        question.put("optionB", "提前三天");
        question.put("optionC", "提前一天");
        question.put("optionD", "截止前两小时");
        questions.add(question);
        snapshot.put("questions", questions);
        record.setQuestionSnapshotJson(JSON.toJSONString(snapshot));

        JSONArray answers = new JSONArray();
        JSONObject answer = new JSONObject();
        answer.put("questionNo", 1);
        answer.put("questionId", 301L);
        answer.put("option", "B");
        answers.add(answer);
        record.setAnswersJson(JSON.toJSONString(answers));

        Map<String, Object> view = service.view(record);

        assertEquals("DDL冲刺", view.get("pointName"));
        assertEquals("job_week", view.get("bizType"));
        assertEquals("多任务并行/极限交付", view.get("theme"));
        assertEquals("时间管理、抗压交付、多任务优先级", view.get("projection"));
        assertNotNull(view.get("questions"));
        assertEquals(1, ((JSONArray) view.get("questions")).size());
        @SuppressWarnings("unchecked")
        Map<Integer, String> saved = (Map<Integer, String>) view.get("answers");
        assertEquals("B", saved.get(1));
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) view.get("result");
        assertEquals(67, result.get("pressureValue"));
        assertEquals("red", result.get("color"));
        assertEquals(4, result.get("redCount"));
        assertEquals(2, result.get("greenCount"));
        assertEquals(6, result.get("total"));
    }

    @Test
    public void unfinishedRecordHasNoResultBlock() {
        ZcyltzjRecord record = baseRecord();
        record.setStatus("participating");
        record.setStage("entered");

        Map<String, Object> view = service.view(record);

        assertFalse(view.containsKey("result"));
        assertEquals("entered", view.get("stage"));
    }

    private static ZcyltzjRecord baseRecord() {
        ZcyltzjRecord record = new ZcyltzjRecord();
        record.setRecordId(1L);
        record.setInstanceId(2L);
        record.setGameId(3L);
        record.setScanTime(new Date());
        record.setRedCount(0);
        record.setGreenCount(0);
        return record;
    }
}
