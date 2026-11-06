package com.gkzh.app.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 能量雷达统计导出回归：工作簿必须包含统计汇总、维度偏好、院系维度偏好、学生明细和数据说明五个工作表，
 * 不再包含“选择明细”，院系选择数据改为在“院系维度偏好”中细化。
 */
public class NlldStatisticsExcelTest {

    @Test
    public void workbookContainsSummaryDimensionsStudentsAnswersAndNotes() throws Exception {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("enteredCount", 12);
        summary.put("startedCount", 10);
        summary.put("completedCount", 8);
        summary.put("completionRate", 66.67);
        summary.put("top2Count", 3);
        summary.put("top2Rate", 37.5);
        summary.put("openCount", 5);
        summary.put("openRate", 62.5);
        summary.put("savedCount", 2);
        summary.put("savedRate", 25.0);

        List<Map<String, Object>> dimensions = new ArrayList<>();
        Map<String, Object> dimension = new LinkedHashMap<>();
        dimension.put("serialNo", 1);
        dimension.put("dimensionName", "智力激发");
        dimension.put("dimensionGroup", "内在价值");
        dimension.put("selectedCount", 10);
        dimension.put("selectedRate", 10.42);
        dimension.put("twoPointCount", 4);
        dimension.put("twoPointRate", 50.0);
        dimension.put("top2Count", 4);
        dimension.put("top2Rate", 50.0);
        dimensions.add(dimension);

        List<Map<String, Object>> resultTypes = new ArrayList<>();
        Map<String, Object> top2 = new LinkedHashMap<>();
        top2.put("serialNo", 1);
        top2.put("resultType", "top2");
        top2.put("resultName", "TOP2 类型");
        top2.put("userCount", 3);
        top2.put("probability", 37.5);
        resultTypes.add(top2);

        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("studentName", "张三");
        row.put("studentNo", "20260001");
        row.put("schoolName", "测试大学");
        row.put("departmentName", "信息学院");
        row.put("major", "软件工程");
        row.put("gender", "男");
        row.put("instanceName", "2026 生涯活动");
        row.put("top1DimensionName", "智力激发");
        row.put("top2DimensionName", "成就满足");
        row.put("resultTypeName", "TOP2 类型");
        row.put("answeredRounds", 6);
        rows.add(row);

        List<Map<String, Object>> departments = new ArrayList<>();
        Map<String, Object> department = new LinkedHashMap<>();
        department.put("collegeName", "信息学院");
        department.put("participantCount", 6);
        department.put("completedCount", 5);
        department.put("completionRate", 83.33);
        department.put("dimensionName", "智力激发");
        department.put("dimensionGroup", "内在价值");
        department.put("selectedCount", 8);
        department.put("selectedRate", 22.22);
        department.put("twoPointCount", 3);
        department.put("twoPointRate", 60.0);
        department.put("topDimensionCount", 3);
        department.put("topDimensionRate", 60.0);
        departments.add(department);

        NlldStatisticsExcelService service = new NlldStatisticsExcelService();
        try (SXSSFWorkbook workbook = service.create(summary, dimensions, resultTypes, departments,
                "本次共 12 人扫码参与，8 人完成测评。", rows, 1L, 21L,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31))) {
            assertEquals(5, workbook.getNumberOfSheets());
            assertNotNull(workbook.getSheet("统计汇总"));
            assertNotNull(workbook.getSheet("维度偏好"));
            assertNotNull(workbook.getSheet("院系维度偏好"));
            assertNotNull(workbook.getSheet("学生明细"));
            assertNotNull(workbook.getSheet("数据说明"));
            assertNull(workbook.getSheet("选择明细"));

            Sheet summarySheet = workbook.getSheet("统计汇总");
            assertEquals("能量雷达参与统计", summarySheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("扫码参与人数", summarySheet.getRow(5).getCell(0).getStringCellValue());
            assertEquals("保存截图人数", summarySheet.getRow(13).getCell(0).getStringCellValue());

            Sheet dimensionSheet = workbook.getSheet("维度偏好");
            assertEquals("智力激发", dimensionSheet.getRow(3).getCell(1).getStringCellValue());
            assertEquals("50.0%", dimensionSheet.getRow(3).getCell(6).getStringCellValue());

            Sheet studentSheet = workbook.getSheet("学生明细");
            assertEquals("张三", studentSheet.getRow(3).getCell(1).getStringCellValue());
            assertEquals("TOP2 类型", studentSheet.getRow(3).getCell(10).getStringCellValue());

            Sheet departmentSheet = workbook.getSheet("院系维度偏好");
            assertEquals("信息学院", departmentSheet.getRow(3).getCell(1).getStringCellValue());
            assertEquals("智力激发", departmentSheet.getRow(3).getCell(5).getStringCellValue());
            assertEquals("60.0%", departmentSheet.getRow(3).getCell(10).getStringCellValue());
        }
    }
}
