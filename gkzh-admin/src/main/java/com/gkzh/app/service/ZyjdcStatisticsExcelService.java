package com.gkzh.app.service;

import com.gkzh.common.utils.DateUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 职业角斗场统计 Excel 导出。
 * 工作簿包含统计汇总、角色分布、学生明细和数据说明，字段与 Web 统计页保持一致。
 */
@Service
public class ZyjdcStatisticsExcelService {

    /** 学生明细超过该行数时自动续表，避免单个工作表过大。 */
    private static final int SHEET_ROW_LIMIT = 60000;

    /**
     * 生成工作簿；调用方负责写出响应并释放临时文件。
     */
    public SXSSFWorkbook create(Map<String, Object> summary, List<Map<String, Object>> roleStats,
                                List<Map<String, Object>> resultStats, String analysis,
                                List<Map<String, Object>> rows, Long schoolId, Long instanceId,
                                LocalDate startDate, LocalDate endDate) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        CellStyle titleStyle = titleStyle(workbook);
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle bodyStyle = bodyStyle(workbook);
        CellStyle wrapStyle = wrapStyle(workbook);

        summarySheet(workbook, summary, resultStats, analysis, schoolId, instanceId, startDate, endDate,
                titleStyle, headerStyle, bodyStyle, wrapStyle);
        roleSheet(workbook, roleStats, titleStyle, headerStyle, bodyStyle);
        studentSheets(workbook, rows, titleStyle, headerStyle, bodyStyle);
        noteSheet(workbook, titleStyle, headerStyle, wrapStyle);
        return workbook;
    }

    /** 第一页：参与、完成、胜负和平均尝试次数。 */
    private void summarySheet(SXSSFWorkbook workbook, Map<String, Object> summary,
                              List<Map<String, Object>> resultStats, String analysis,
                              Long schoolId, Long instanceId, LocalDate startDate, LocalDate endDate,
                              CellStyle titleStyle, CellStyle headerStyle, CellStyle bodyStyle, CellStyle wrapStyle) {
        SXSSFSheet sheet = workbook.createSheet("统计汇总");
        sheet.setColumnWidth(0, 26 * 256);
        sheet.setColumnWidth(1, 34 * 256);
        sheet.setColumnWidth(2, 26 * 256);
        sheet.setColumnWidth(3, 34 * 256);

        SXSSFRow title = sheet.createRow(0);
        title.setHeightInPoints(30);
        title.createCell(0).setCellValue("职业角斗场参与统计");
        title.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        int rowIndex = 2;
        rowIndex = metric(sheet, rowIndex, "学校编号", schoolId, headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "活动实例编号", instanceId, headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "参与日期", dateRange(startDate, endDate), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "扫码参与人数", summary.get("enteredCount"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "完成角色选择人数", summary.get("selectedCount"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "通关人数", summary.get("completedCount"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "完成率", summary.get("completionRate") + "%", headerStyle, bodyStyle);

        long win = 0;
        long lose = 0;
        long draw = 0;
        for (Map<String, Object> item : resultStats) {
            String result = String.valueOf(item.get("result"));
            long count = number(item.get("userCount"));
            if ("win".equals(result)) {
                win = count;
            } else if ("lose".equals(result)) {
                lose = count;
            } else if ("draw".equals(result)) {
                draw = count;
            }
        }
        rowIndex = metric(sheet, rowIndex, "胜利人数", win, headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "失败人数", lose, headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "平局人数", draw, headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "平均每局尝试次数", summary.get("averageAttempts"), headerStyle, bodyStyle);

        rowIndex += 1;
        SXSSFRow analysisRow = sheet.createRow(rowIndex);
        analysisRow.setHeightInPoints(150);
        analysisRow.createCell(0).setCellValue("整体偏好分析");
        analysisRow.getCell(0).setCellStyle(headerStyle);
        analysisRow.createCell(1).setCellValue(analysis == null ? "" : analysis);
        analysisRow.getCell(1).setCellStyle(wrapStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 3));
    }

    /** 第二页：8 个角色的偏好占比和通关情况。 */
    private void roleSheet(SXSSFWorkbook workbook, List<Map<String, Object>> roleStats,
                           CellStyle titleStyle, CellStyle headerStyle, CellStyle bodyStyle) {
        SXSSFSheet sheet = workbook.createSheet("角色分布");
        int[] widths = {8, 12, 24, 10, 12, 12, 12, 12};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        SXSSFRow title = sheet.createRow(0);
        title.setHeightInPoints(30);
        title.createCell(0).setCellValue("各动物角色偏好占比统计");
        title.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, widths.length - 1));

        String[] headers = {"序号", "动物角色", "职场定位", "等级", "选择人数", "偏好占比", "通关人数", "完成率"};
        SXSSFRow header = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            header.getCell(i).setCellStyle(headerStyle);
        }
        int rowIndex = 3;
        for (Map<String, Object> role : roleStats) {
            SXSSFRow row = sheet.createRow(rowIndex++);
            String[] values = {String.valueOf(role.get("serialNo")), String.valueOf(role.get("roleName")),
                    String.valueOf(role.get("jobTitle")), String.valueOf(role.get("rankLevel")),
                    String.valueOf(role.get("userCount")), String.valueOf(role.get("probability")) + "%",
                    String.valueOf(role.get("finishedCount")), String.valueOf(role.get("completionRate")) + "%"};
            for (int i = 0; i < values.length; i++) {
                row.createCell(i).setCellValue(values[i]);
                row.getCell(i).setCellStyle(bodyStyle);
            }
        }
    }

    /** 第三页及续表：逐名学生明细；超过行上限时自动新建工作表。 */
    private void studentSheets(SXSSFWorkbook workbook, List<Map<String, Object>> rows,
                               CellStyle titleStyle, CellStyle headerStyle, CellStyle bodyStyle) {
        String[] headers = {"序号", "学生姓名", "学号", "学校", "院系", "专业", "性别", "活动实例",
                "所选角色", "职场定位", "对局结果", "尝试次数", "落子步数", "参与时间", "开局时间", "结束时间"};
        int[] widths = {7, 12, 16, 20, 20, 18, 8, 24, 12, 20, 10, 10, 10, 20, 20, 20};
        int page = 1;
        int rowIndex = 3;
        // 即使当前条件下没有记录，也保留带表头的“学生明细”工作表，方便确认导出范围。
        SXSSFSheet sheet = newStudentSheet(workbook, page, headers, widths, titleStyle, headerStyle);
        int serial = 1;
        for (Map<String, Object> item : rows) {
            if (rowIndex >= SHEET_ROW_LIMIT) {
                page++;
                sheet = newStudentSheet(workbook, page, headers, widths, titleStyle, headerStyle);
                rowIndex = 3;
            }
            SXSSFRow row = sheet.createRow(rowIndex++);
            String[] values = {String.valueOf(serial++), text(item.get("studentName")), text(item.get("studentNo")),
                    text(item.get("schoolName")), text(item.get("departmentName")), text(item.get("major")),
                    text(item.get("gender")), text(item.get("instanceName")), text(item.get("roleName")),
                    text(item.get("jobTitle")), text(item.get("resultName")), text(item.get("attemptNo")),
                    text(item.get("moveCount")), time(item.get("scanTime")), time(item.get("startTime")),
                    time(item.get("finishTime"))};
            for (int i = 0; i < values.length; i++) {
                row.createCell(i).setCellValue(values[i]);
                row.getCell(i).setCellStyle(bodyStyle);
            }
        }
    }

    /** 创建一页学生明细表头；超过单表行上限时继续创建下一页。 */
    private SXSSFSheet newStudentSheet(SXSSFWorkbook workbook, int page, String[] headers, int[] widths,
                                       CellStyle titleStyle, CellStyle headerStyle) {
        String name = page == 1 ? "学生明细" : "学生明细" + page;
        SXSSFSheet sheet = workbook.createSheet(name);
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        SXSSFRow title = sheet.createRow(0);
        title.setHeightInPoints(30);
        title.createCell(0).setCellValue("职业角斗场学生参与明细");
        title.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));
        SXSSFRow header = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            header.getCell(i).setCellStyle(headerStyle);
        }
        return sheet;
    }

    /** 第四页：筛选口径与字段说明。 */
    private void noteSheet(SXSSFWorkbook workbook, CellStyle titleStyle, CellStyle headerStyle, CellStyle wrapStyle) {
        SXSSFSheet sheet = workbook.createSheet("数据说明");
        sheet.setColumnWidth(0, 22 * 256);
        sheet.setColumnWidth(1, 100 * 256);
        SXSSFRow title = sheet.createRow(0);
        title.setHeightInPoints(30);
        title.createCell(0).setCellValue("数据说明");
        title.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        String[][] notes = {
                {"统计口径", "扫码参与人数按职业角斗场参与记录去重；同一学生在同一活动游戏下只有一条记录。通关人数只统计对局结果为胜利的记录，失败或平局不计为完成。"},
                {"角色偏好", "选择人数按学生最终选择的角色统计；偏好占比 = 该角色选择人数 ÷ 已选择角色总人数。完成率 = 该角色通关人数 ÷ 该角色选择人数。"},
                {"对局结果", "胜利：吃光对方、对方无子可动或时间到时等级/数量占优；失败：己方被吃光、无子可动或时间到时落后；平局：时间到且最高等级和剩余数量均相同。"},
                {"时间范围", "参与日期以扫码时间 scan_time 为准，开始日期含当天零点，结束日期含当天，查询时转换为次日零点之前。"},
                {"隐私说明", "本表仅用于活动统计和教学复盘，请勿用于与活动无关的个人评价或对外传播。"}
        };
        int rowIndex = 2;
        for (String[] note : notes) {
            SXSSFRow row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(60);
            row.createCell(0).setCellValue(note[0]);
            row.getCell(0).setCellStyle(headerStyle);
            row.createCell(1).setCellValue(note[1]);
            row.getCell(1).setCellStyle(wrapStyle);
        }
    }

    private int metric(SXSSFSheet sheet, int rowIndex, String label, Object value,
                       CellStyle headerStyle, CellStyle bodyStyle) {
        SXSSFRow row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(label);
        row.getCell(0).setCellStyle(headerStyle);
        row.createCell(1).setCellValue(value == null ? "" : String.valueOf(value));
        row.getCell(1).setCellStyle(bodyStyle);
        return rowIndex + 1;
    }

    private String dateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return "全部日期";
        }
        return (startDate == null ? "不限" : startDate.toString()) + " 至 " + (endDate == null ? "不限" : endDate.toString());
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String time(Object value) {
        return value instanceof Date ? DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", (Date) value) : text(value);
    }

    private long number(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return value == null ? 0L : Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private CellStyle titleStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle headerStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle bodyStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle wrapStyle(SXSSFWorkbook workbook) {
        CellStyle style = bodyStyle(workbook);
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }
}
