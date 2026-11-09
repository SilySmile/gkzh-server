package com.gkzh.app.service;

import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.zmwl.domain.ZmwlDirection;
import com.gkzh.zmwl.domain.ZmwlRecord;
import com.gkzh.zmwl.domain.ZmwlSetting;
import com.gkzh.zmwl.domain.dto.ZmwlRecordQuery;
import com.gkzh.zmwl.mapper.ZmwlRecordMapper;
import com.gkzh.zmwl.service.ZmwlConfigService;
import com.gkzh.zmwl.service.ZmwlStatisticsService;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 职面未来统计导出回归：工作簿必须包含统计汇总、方向分布、院系偏好、学生明细和数据说明五个工作表。
 */
public class ZmwlStatisticsExcelTest {

    @Test
    public void workbookContainsSummaryDirectionsDepartmentsStudentsAndNotes() throws Exception {
        ZmwlRecordMapper recordMapper = mock(ZmwlRecordMapper.class);
        ZmwlRecordEnrichService enrichService = mock(ZmwlRecordEnrichService.class);
        ZmwlStatisticsService statisticsService = mock(ZmwlStatisticsService.class);
        ZmwlConfigService configService = mock(ZmwlConfigService.class);
        GkzhSchoolMapper schoolMapper = mock(GkzhSchoolMapper.class);
        GkzhSchoolDepartmentMapper departmentMapper = mock(GkzhSchoolDepartmentMapper.class);
        IActivityWeekService activityWeekService = mock(IActivityWeekService.class);

        GkzhSchool school = new GkzhSchool();
        school.setTitle("测试大学");
        GkzhActivityWeekInstance instance = new GkzhActivityWeekInstance();
        instance.setTitle("2026 生涯活动");
        ZmwlSetting setting = new ZmwlSetting();
        setting.setAllowReselect("0");
        setting.setEnabled("1");

        ZmwlDirection employment = new ZmwlDirection();
        employment.setDirectionId(1L);
        employment.setDirectionCode("employment");
        employment.setName("就业工作");
        employment.setTagName("职场启航者");
        employment.setShortIntro("走进职场，开启职业生涯");
        employment.setSortOrder(1);
        employment.setStatus("0");

        ZmwlRecord record = new ZmwlRecord();
        record.setRecordId(11L);
        record.setStatus("finished");
        record.setDirectionCode("employment");
        record.setDirectionName("就业工作");
        record.setTagName("职场启航者");
        record.setDurationMs(42000L);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("recordId", 11L);
        row.put("schoolName", "测试大学");
        row.put("departmentName", "信息学院");
        row.put("major", "软件工程");
        row.put("studentNo", "20260001");
        row.put("studentName", "张三");
        row.put("gender", "男");
        row.put("instanceName", "2026 生涯活动");
        row.put("status", "finished");
        row.put("directionName", "就业工作");
        row.put("tagName", "职场启航者");
        row.put("durationMs", 42000L);
        row.put("scanTime", new Date());
        row.put("finishTime", new Date());

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("participantCount", 1L);
        summary.put("finishedCount", 1L);
        summary.put("inProgressCount", 0L);
        summary.put("finishRate", 100.0);
        summary.put("undecidedCount", 0L);
        summary.put("directionCount", 8);
        summary.put("topDirectionName", "就业工作");
        summary.put("topDirectionCount", 1L);
        summary.put("topDirectionRate", 100.0);
        summary.put("avgDurationSeconds", 42.0);

        Map<String, Object> direction = new LinkedHashMap<>();
        direction.put("directionCode", "employment");
        direction.put("name", "就业工作");
        direction.put("tagName", "职场启航者");
        direction.put("shortIntro", "走进职场，开启职业生涯");
        direction.put("sortOrder", 1);
        direction.put("status", "0");
        direction.put("selectedCount", 1L);
        direction.put("rate", 100.0);

        Map<String, Object> department = new LinkedHashMap<>();
        department.put("departmentName", "信息学院");
        department.put("participantCount", 1L);
        department.put("finishedCount", 1L);
        department.put("finishRate", 100.0);
        department.put("topDirectionName", "就业工作");
        department.put("topDirectionCount", 1L);
        department.put("topDirectionRate", 100.0);

        when(schoolMapper.selectGkzhSchoolBySchoolId(1L)).thenReturn(school);
        when(activityWeekService.getInstance(2L)).thenReturn(instance);
        when(recordMapper.selectAdminRecords(any(ZmwlRecordQuery.class))).thenReturn(Arrays.asList(record));
        when(enrichService.enrich(anyList())).thenReturn(Arrays.asList(row));
        when(statisticsService.summary(any(ZmwlRecordQuery.class))).thenReturn(summary);
        when(statisticsService.directionStats(any(ZmwlRecordQuery.class), anyLong()))
                .thenReturn(Collections.singletonList(direction));
        when(statisticsService.departmentStats(any(ZmwlRecordQuery.class)))
                .thenReturn(Collections.singletonList(department));
        when(statisticsService.topDirections(anyList(), anyInt()))
                .thenReturn(Collections.singletonList(direction));
        when(configService.listAll()).thenReturn(Collections.singletonList(employment));
        when(configService.getSetting()).thenReturn(setting);

        ZmwlStatisticsExcelService service = new ZmwlStatisticsExcelService(recordMapper, enrichService,
                statisticsService, configService, schoolMapper, departmentMapper, activityWeekService);

        try (SXSSFWorkbook workbook = service.create(1L, 2L, null, null, null, null, null, null, null, null)) {
            assertEquals(5, workbook.getNumberOfSheets());
            assertEquals("统计汇总", workbook.getSheetName(0));
            assertEquals("方向分布", workbook.getSheetName(1));
            assertEquals("院系偏好", workbook.getSheetName(2));
            assertEquals("学生明细", workbook.getSheetName(3));
            assertEquals("数据说明", workbook.getSheetName(4));

            Sheet students = workbook.getSheetAt(3);
            assertEquals("测试大学", students.getRow(1).getCell(1).getStringCellValue());
            assertEquals("张三", students.getRow(1).getCell(5).getStringCellValue());
            assertEquals("就业工作", students.getRow(1).getCell(9).getStringCellValue());
            assertEquals("42.0", students.getRow(1).getCell(11).getStringCellValue());

            Sheet directions = workbook.getSheetAt(1);
            assertEquals("就业工作", directions.getRow(1).getCell(2).getStringCellValue());
            assertEquals("100.0%", directions.getRow(1).getCell(6).getStringCellValue());

            Sheet departmentSheet = workbook.getSheetAt(2);
            assertEquals("信息学院", departmentSheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("就业工作", departmentSheet.getRow(1).getCell(4).getStringCellValue());
        }
    }
}
