package com.gkzh.app.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.jlcz.domain.JlczRecord;
import com.gkzh.jlcz.domain.JlczSetting;
import com.gkzh.jlcz.domain.dto.JlczRecordQuery;
import com.gkzh.jlcz.mapper.JlczRecordMapper;
import com.gkzh.jlcz.service.JlczConfigService;
import com.gkzh.jlcz.service.JlczStatisticsService;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 简历称重统计导出回归：工作簿必须包含统计汇总、砝码选择率、类别统计、学生明细和数据说明五个工作表。
 */
public class JlczStatisticsExcelTest {

    @Test
    public void workbookContainsSummaryWeightsCategoriesStudentsAndNotes() throws Exception {
        JlczRecordMapper recordMapper = mock(JlczRecordMapper.class);
        JlczRecordEnrichService enrichService = mock(JlczRecordEnrichService.class);
        JlczStatisticsService statisticsService = mock(JlczStatisticsService.class);
        JlczConfigService configService = mock(JlczConfigService.class);
        GkzhSchoolMapper schoolMapper = mock(GkzhSchoolMapper.class);
        GkzhSchoolDepartmentMapper departmentMapper = mock(GkzhSchoolDepartmentMapper.class);
        IActivityWeekService activityWeekService = mock(IActivityWeekService.class);

        GkzhSchool school = new GkzhSchool();
        school.setTitle("测试大学");
        GkzhActivityWeekInstance instance = new GkzhActivityWeekInstance();
        instance.setTitle("2026 求职活动");
        JlczSetting setting = new JlczSetting();
        setting.setLeftWeightG(80);
        setting.setPassWeightG(80);
        setting.setTimeLimitSeconds(0);

        JlczRecord record = new JlczRecord();
        record.setRecordId(11L);
        record.setStatus("finished");
        record.setResult("pass");
        record.setTotalWeight(80);
        record.setSelectedCount(11);
        record.setAttemptNo(1);
        record.setDurationMs(120000L);
        record.setSelectedJson(selectedJson());

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("recordId", 11L);
        row.put("schoolName", "测试大学");
        row.put("departmentName", "信息学院");
        row.put("major", "软件工程");
        row.put("studentNo", "20260001");
        row.put("studentName", "张三");
        row.put("gender", "男");
        row.put("instanceName", "2026 求职活动");
        row.put("totalWeight", 80);
        row.put("selectedCount", 11);
        row.put("status", "finished");
        row.put("result", "pass");
        row.put("attemptNo", 1);
        row.put("durationMs", 120000L);
        row.put("scanTime", new Date());
        row.put("finishTime", new Date());

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("participantCount", 1L);
        summary.put("finishedCount", 1L);
        summary.put("passCount", 1L);
        summary.put("failCount", 0L);
        summary.put("passRate", 100.0);
        summary.put("avgTotalWeight", 80.0);
        summary.put("avgSelectedCount", 11.0);
        summary.put("avgDurationSeconds", 120.0);

        Map<String, Object> weight = new LinkedHashMap<>();
        weight.put("weightNo", 1);
        weight.put("label", "靶心岗位");
        weight.put("categoryName", "硬通货（核心竞争力）");
        weight.put("weightG", 10);
        weight.put("selectedCount", 1L);
        weight.put("selectedRate", 100.0);
        weight.put("gapCount", 0L);
        weight.put("status", "0");

        Map<String, Object> category = new LinkedHashMap<>();
        category.put("categoryName", "硬通货（核心竞争力）");
        category.put("weightCount", 5);
        category.put("availableWeight", 50);
        category.put("selectedCount", 1L);
        category.put("selectedWeight", 10L);
        category.put("avgWeightPerParticipant", 10.0);
        category.put("selectionRate", 20.0);

        Map<String, Object> bucket = new LinkedHashMap<>();
        bucket.put("label", "80-89g");
        bucket.put("count", 1L);
        bucket.put("rate", 100.0);

        when(schoolMapper.selectGkzhSchoolBySchoolId(1L)).thenReturn(school);
        when(activityWeekService.getInstance(2L)).thenReturn(instance);
        when(recordMapper.selectAdminRecords(any(JlczRecordQuery.class))).thenReturn(Arrays.asList(record));
        when(enrichService.enrich(anyList())).thenReturn(Arrays.asList(row));
        when(statisticsService.summary(any(JlczRecordQuery.class))).thenReturn(summary);
        when(statisticsService.weightStats(any(JlczRecordQuery.class), anyLong()))
                .thenReturn(Arrays.asList(weight));
        when(statisticsService.categoryStats(anyList(), anyLong())).thenReturn(Arrays.asList(category));
        when(statisticsService.distribution(any(JlczRecordQuery.class), any(Map.class)))
                .thenReturn(Arrays.asList(bucket));
        when(configService.getSetting()).thenReturn(setting);

        JlczStatisticsExcelService service = new JlczStatisticsExcelService(recordMapper, enrichService,
                statisticsService, configService, schoolMapper, departmentMapper, activityWeekService);

        try (SXSSFWorkbook workbook = service.create(1L, 2L, null, null, null, null, null, null, null, null)) {
            assertEquals(5, workbook.getNumberOfSheets());
            assertEquals("统计汇总", workbook.getSheetName(0));
            assertEquals("砝码选择率", workbook.getSheetName(1));
            assertEquals("类别统计", workbook.getSheetName(2));
            assertEquals("学生明细", workbook.getSheetName(3));
            assertEquals("数据说明", workbook.getSheetName(4));

            Sheet students = workbook.getSheetAt(3);
            assertEquals("测试大学", students.getRow(1).getCell(1).getStringCellValue());
            assertEquals("张三", students.getRow(1).getCell(5).getStringCellValue());
            assertEquals("通关", students.getRow(1).getCell(9).getStringCellValue());
            assertEquals("靶心岗位", students.getRow(1).getCell(15).getStringCellValue());

            Sheet weights = workbook.getSheetAt(1);
            assertEquals("靶心岗位", weights.getRow(1).getCell(2).getStringCellValue());
            assertEquals("100.0%", weights.getRow(1).getCell(5).getStringCellValue());
        }
    }

    private static String selectedJson() {
        JSONArray array = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("weightNo", 1);
        item.put("label", "靶心岗位");
        item.put("weightG", 10);
        item.put("categoryCode", "core");
        array.add(item);
        return JSON.toJSONString(array);
    }
}
