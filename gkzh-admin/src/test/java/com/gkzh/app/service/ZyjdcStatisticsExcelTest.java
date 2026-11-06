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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 职业角斗场统计导出回归：工作簿必须包含统计汇总、角色分布、学生明细和数据说明四个工作表。
 */
public class ZyjdcStatisticsExcelTest {

    @Test
    public void workbookContainsSummaryRolesStudentsAndNotes() throws Exception {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("enteredCount", 12);
        summary.put("selectedCount", 10);
        summary.put("completedCount", 6);
        summary.put("completionRate", 50.0);
        summary.put("averageAttempts", 1.5);

        List<Map<String, Object>> roles = new ArrayList<>();
        Map<String, Object> role = new LinkedHashMap<>();
        role.put("serialNo", 1);
        role.put("roleName", "狐");
        role.put("jobTitle", "主管");
        role.put("rankLevel", 3);
        role.put("userCount", 6);
        role.put("probability", 60.0);
        role.put("finishedCount", 4);
        role.put("completionRate", 66.67);
        roles.add(role);

        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> win = new LinkedHashMap<>();
        win.put("result", "win");
        win.put("resultName", "胜利");
        win.put("userCount", 6);
        win.put("probability", 60.0);
        results.add(win);

        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("studentName", "张三");
        row.put("studentNo", "20260001");
        row.put("schoolName", "测试大学");
        row.put("departmentName", "信息学院");
        row.put("major", "软件工程");
        row.put("gender", "男");
        row.put("instanceName", "2026 生涯活动");
        row.put("roleName", "狐");
        row.put("jobTitle", "主管");
        row.put("resultName", "胜利");
        row.put("attemptNo", 1);
        row.put("moveCount", 18);
        rows.add(row);

        ZyjdcStatisticsExcelService service = new ZyjdcStatisticsExcelService();
        try (SXSSFWorkbook workbook = service.create(summary, roles, results,
                "本次共 12 人扫码参与，角色偏好集中在狐。", rows, 1L, 21L,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31))) {
            assertEquals(4, workbook.getNumberOfSheets());
            assertTrue(workbook.getSheet("统计汇总") != null);
            assertTrue(workbook.getSheet("角色分布") != null);
            assertTrue(workbook.getSheet("学生明细") != null);
            assertTrue(workbook.getSheet("数据说明") != null);

            Sheet summarySheet = workbook.getSheet("统计汇总");
            assertEquals("职业角斗场参与统计", summarySheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("扫码参与人数", summarySheet.getRow(5).getCell(0).getStringCellValue());

            Sheet roleSheet = workbook.getSheet("角色分布");
            assertEquals("狐", roleSheet.getRow(3).getCell(1).getStringCellValue());
            assertEquals("主管", roleSheet.getRow(3).getCell(2).getStringCellValue());

            Sheet studentSheet = workbook.getSheet("学生明细");
            assertEquals("张三", studentSheet.getRow(3).getCell(1).getStringCellValue());
            assertEquals("20260001", studentSheet.getRow(3).getCell(2).getStringCellValue());
            assertEquals("胜利", studentSheet.getRow(3).getCell(10).getStringCellValue());
        }
    }
}
