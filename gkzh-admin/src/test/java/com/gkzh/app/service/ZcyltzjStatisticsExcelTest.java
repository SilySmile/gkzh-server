package com.gkzh.app.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.zcyltzj.domain.ZcyltzjRecord;
import com.gkzh.zcyltzj.mapper.ZcyltzjRecordMapper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 骰子局统计导出回归：工作簿必须包含统计汇总、学生数据、答题明细和数据说明四个工作表。
 */
public class ZcyltzjStatisticsExcelTest {

    @Test
    public void workbookContainsSummaryStudentsAnswersAndNotes() throws Exception {
        ZcyltzjRecordMapper recordMapper = mock(ZcyltzjRecordMapper.class);
        ZcyltzjRecordEnrichService enrichService = mock(ZcyltzjRecordEnrichService.class);
        GkzhSchoolMapper schoolMapper = mock(GkzhSchoolMapper.class);
        GkzhSchoolDepartmentMapper departmentMapper = mock(GkzhSchoolDepartmentMapper.class);
        IActivityWeekService activityWeekService = mock(IActivityWeekService.class);

        GkzhSchool school = new GkzhSchool();
        school.setTitle("测试大学");
        GkzhActivityWeekInstance instance = new GkzhActivityWeekInstance();
        instance.setTitle("2026 生涯活动");

        ZcyltzjRecord record = new ZcyltzjRecord();
        record.setRecordId(11L);
        record.setPointNo(1);
        record.setPointName("早八生存");
        record.setStatus("finished");
        record.setPressureValue(67);
        record.setColor("red");
        record.setRedCount(4);
        record.setGreenCount(2);
        record.setQuestionSnapshotJson(snapshot());
        record.setAnswersJson(answers());

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("recordId", 11L);
        row.put("schoolName", "测试大学");
        row.put("departmentName", "信息学院");
        row.put("major", "软件工程");
        row.put("studentNo", "20260001");
        row.put("studentName", "张三");
        row.put("gender", "男");
        row.put("instanceName", "2026 生涯活动");
        row.put("pointNo", 1);
        row.put("pointName", "早八生存");
        row.put("pressureValue", 67);
        row.put("color", "red");
        row.put("redCount", 4);
        row.put("greenCount", 2);
        row.put("status", "finished");
        row.put("scanTime", new Date());
        row.put("startTime", new Date());
        row.put("finishTime", new Date());

        when(schoolMapper.selectGkzhSchoolBySchoolId(1L)).thenReturn(school);
        when(activityWeekService.getInstance(2L)).thenReturn(instance);
        when(recordMapper.selectAdminRecords(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Arrays.asList(record));
        when(enrichService.enrich(anyList())).thenReturn(Arrays.asList(row));

        ZcyltzjStatisticsExcelService service = new ZcyltzjStatisticsExcelService(recordMapper, enrichService,
                schoolMapper, departmentMapper, activityWeekService);

        try (SXSSFWorkbook workbook = service.create(1L, 2L, null, null, null, null, null, null, null, null)) {
            assertEquals(4, workbook.getNumberOfSheets());
            assertEquals("统计汇总", workbook.getSheetName(0));
            assertEquals("学生数据", workbook.getSheetName(1));
            assertEquals("答题明细", workbook.getSheetName(2));
            assertEquals("数据说明", workbook.getSheetName(3));

            Sheet students = workbook.getSheetAt(1);
            assertEquals("测试大学", students.getRow(1).getCell(1).getStringCellValue());
            assertEquals("张三", students.getRow(1).getCell(5).getStringCellValue());
            assertEquals("67%", students.getRow(1).getCell(11).getStringCellValue());
            assertEquals("红色", students.getRow(1).getCell(12).getStringCellValue());

            Sheet answers = workbook.getSheetAt(2);
            assertEquals("20260001", answers.getRow(1).getCell(0).getStringCellValue());
            assertEquals("A", answers.getRow(1).getCell(7).getStringCellValue());
            assertEquals("红色", answers.getRow(1).getCell(9).getStringCellValue());
            assertTrue(answers.getLastRowNum() >= 2);
        }
    }

    private static String snapshot() {
        JSONObject snapshot = new JSONObject();
        snapshot.put("pointNo", 1);
        snapshot.put("pointName", "早八生存");
        JSONArray questions = new JSONArray();
        JSONObject first = new JSONObject();
        first.put("questionId", 101L);
        first.put("questionNo", 1);
        first.put("content", "你能接受一周有几天需要早起赶早八？");
        first.put("optionA", "0天");
        first.put("optionB", "1-2天");
        first.put("optionC", "3-4天");
        first.put("optionD", "5天全早八");
        questions.add(first);
        JSONObject second = new JSONObject();
        second.put("questionId", 102L);
        second.put("questionNo", 2);
        second.put("content", "如果未来工作要求每天8点前到岗，你的感受是？");
        second.put("optionA", "完全能接受");
        second.put("optionB", "有点困但能撑");
        second.put("optionC", "靠咖啡续命");
        second.put("optionD", "直接崩溃");
        questions.add(second);
        snapshot.put("questions", questions);
        return JSON.toJSONString(snapshot);
    }

    private static String answers() {
        JSONArray answers = new JSONArray();
        JSONObject first = new JSONObject();
        first.put("questionNo", 1);
        first.put("questionId", 101L);
        first.put("option", "A");
        answers.add(first);
        JSONObject second = new JSONObject();
        second.put("questionNo", 2);
        second.put("questionId", 102L);
        second.put("option", "C");
        answers.add(second);
        return JSON.toJSONString(answers);
    }
}
