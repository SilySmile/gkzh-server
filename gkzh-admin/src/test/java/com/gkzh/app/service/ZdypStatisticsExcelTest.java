package com.gkzh.app.service;

import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.zdyp.domain.ZdypCareer;
import com.gkzh.zdyp.domain.ZdypRecord;
import com.gkzh.zdyp.domain.ZdypSetting;
import com.gkzh.zdyp.mapper.ZdypRecordMapper;
import com.gkzh.zdyp.service.ZdypConfigService;
import com.gkzh.zdyp.service.ZdypStatisticsService;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 职得一拼统计 Excel 导出回归。
 * 工作簿必须包含统计汇总、行为分型、维度错误、岗位分布、学生明细和数据说明六个工作表，
 * 且统计页的汇总数字与页面筛选口径一致。
 */
public class ZdypStatisticsExcelTest {

    @Test
    void workbookContainsSixSheets() throws Exception {
        ZdypRecordMapper recordMapper = mock(ZdypRecordMapper.class);
        ZdypRecordEnrichService enrichService = mock(ZdypRecordEnrichService.class);
        ZdypStatisticsService statisticsService = mock(ZdypStatisticsService.class);
        ZdypConfigService configService = mock(ZdypConfigService.class);
        GkzhSchoolMapper schoolMapper = mock(GkzhSchoolMapper.class);
        GkzhSchoolDepartmentMapper departmentMapper = mock(GkzhSchoolDepartmentMapper.class);
        IActivityWeekService activityWeekService = mock(IActivityWeekService.class);

        GkzhSchool school = new GkzhSchool();
        school.setSchoolId(5L);
        school.setTitle("测试大学");
        when(schoolMapper.selectGkzhSchoolBySchoolId(5L)).thenReturn(school);

        GkzhActivityWeekInstance instance = new GkzhActivityWeekInstance();
        instance.setInstanceId(21L);
        instance.setTitle("测试活动");
        when(activityWeekService.getInstance(21L)).thenReturn(instance);

        ZdypRecord record = new ZdypRecord();
        record.setRecordId(1L);
        record.setSchoolId(5L);
        record.setGameType("zdyp");
        record.setStatus("finished");
        record.setCareerName("大数据工程技术人员");
        record.setAttemptNo(1);
        record.setFirstAttemptCorrect("1");
        record.setTotalWrong(0);
        record.setTimeoutCount(0);
        record.setBestMs(18_500L);
        record.setBehaviorName("全面均衡型");
        record.setScanTime(new Date());
        when(recordMapper.selectAdminRecords(any())).thenReturn(Collections.singletonList(record));

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("schoolName", "测试大学");
        row.put("departmentName", "中文系");
        row.put("major", "中文专业");
        row.put("studentNo", "2026001");
        row.put("studentName", "张三");
        row.put("gender", "男");
        row.put("instanceName", "测试活动");
        row.put("careerName", "大数据工程技术人员");
        row.put("status", "finished");
        row.put("attemptNo", 1);
        row.put("firstAttemptCorrect", "1");
        row.put("totalWrong", 0);
        row.put("timeoutCount", 0);
        row.put("behaviorName", "全面均衡型");
        row.put("firstAttemptMs", 18_500L);
        row.put("bestMs", 18_500L);
        row.put("scanTime", new Date());
        row.put("finishTime", new Date());
        when(enrichService.enrich(any())).thenReturn(Collections.singletonList(row));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("participantCount", 1L);
        summary.put("startedCount", 1L);
        summary.put("finishedCount", 1L);
        summary.put("inProgressCount", 0L);
        summary.put("finishRate", new java.math.BigDecimal("100.00"));
        summary.put("firstPassCount", 1L);
        summary.put("firstPassRate", new java.math.BigDecimal("100.00"));
        summary.put("reattemptTotal", 0L);
        summary.put("avgAttempts", new java.math.BigDecimal("1.00"));
        summary.put("timeoutPlayers", 0L);
        summary.put("timeoutRate", new java.math.BigDecimal("0.00"));
        summary.put("timeoutCount", 0L);
        summary.put("avgFirstSeconds", new java.math.BigDecimal("18.5"));
        summary.put("avgBestSeconds", new java.math.BigDecimal("18.5"));
        summary.put("minBestSeconds", new java.math.BigDecimal("18.5"));
        summary.put("maxBestSeconds", new java.math.BigDecimal("18.5"));
        when(statisticsService.summary(any())).thenReturn(summary);

        List<Map<String, Object>> behaviors = new ArrayList<>();
        behaviors.add(stats("balanced", "全面均衡型", 1L, "100.00"));
        for (String type : new String[]{"advantage", "weak", "rushed"}) {
            behaviors.add(stats(type, type, 0L, "0.00"));
        }
        when(statisticsService.behaviorStats(any(), anyLong())).thenReturn(behaviors);

        List<Map<String, Object>> slots = new ArrayList<>();
        for (String[] slot : new String[][]{{"content", "工作内容"}, {"skill", "核心技能"},
                {"weakness", "职业缺点"}, {"prospect", "发展前景"}}) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("slotCode", slot[0]);
            item.put("slotName", slot[1]);
            item.put("placedCount", 1L);
            item.put("correctCount", 1L);
            item.put("wrongCount", 0L);
            item.put("unfilledCount", 0L);
            item.put("correctRate", new java.math.BigDecimal("100.00"));
            slots.add(item);
        }
        when(statisticsService.slotStats(any(), anyLong())).thenReturn(slots);

        Map<String, Object> career = new LinkedHashMap<>();
        career.put("careerCode", "big_data_engineer");
        career.put("careerName", "大数据工程技术人员");
        career.put("selectedCount", 1L);
        career.put("finishedCount", 1L);
        career.put("finishRate", new java.math.BigDecimal("100.00"));
        career.put("status", "0");
        when(statisticsService.careerStats(any())).thenReturn(Collections.singletonList(career));

        ZdypSetting setting = new ZdypSetting();
        setting.setSettingId(1);
        setting.setTimeLimit(45);
        setting.setEnabled("1");
        when(configService.getSetting()).thenReturn(setting);
        ZdypCareer careerConfig = new ZdypCareer();
        careerConfig.setCareerId(1L);
        careerConfig.setCareerCode("big_data_engineer");
        careerConfig.setCareerName("大数据工程技术人员");
        when(configService.listAllCareers()).thenReturn(Collections.singletonList(careerConfig));

        ZdypStatisticsExcelService excelService = new ZdypStatisticsExcelService(recordMapper, enrichService,
                statisticsService, configService, schoolMapper, departmentMapper, activityWeekService);

        try (SXSSFWorkbook workbook = excelService.create(5L, 21L, null, null, null, null,
                null, null, null, null, null)) {
            assertNotNull(workbook.getSheet("统计汇总"));
            assertNotNull(workbook.getSheet("行为分型"));
            assertNotNull(workbook.getSheet("维度错误"));
            assertNotNull(workbook.getSheet("岗位分布"));
            assertNotNull(workbook.getSheet("学生明细"));
            assertNotNull(workbook.getSheet("数据说明"));
            assertEquals(6, workbook.getNumberOfSheets());
            // 第一页第一个键值对必须是参与人数，避免列错位。
            assertEquals("参与人数", workbook.getSheet("统计汇总").getRow(1).getCell(0).getStringCellValue());
        }
    }

    private Map<String, Object> stats(String type, String name, Long count, String rate) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("behaviorType", type);
        row.put("behaviorName", name);
        row.put("playerCount", count);
        row.put("rate", new java.math.BigDecimal(rate));
        row.put("conclusion", "结论");
        row.put("suggestion", "建议");
        return row;
    }
}
