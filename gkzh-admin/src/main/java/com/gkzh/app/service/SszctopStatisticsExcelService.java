package com.gkzh.app.service;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 谁是职场TOP统计 Excel 导出。
 * 工作簿包含统计汇总、维度统计、学生明细和数据说明，字段与后台页面保持一致。
 */
@Service
public class SszctopStatisticsExcelService {

    private static final String[] DETAIL_HEADERS = {
            "序号", "学生", "学号", "学校", "院系", "专业", "性别", "所选维度", "结果", "选择职业", "完成时间"
    };

    /** 生成统计工作簿；调用方负责写出响应并释放临时文件。 */
    public SXSSFWorkbook create(Map<String, Object> summary,
                                List<Map<String, Object>> dimensionStats,
                                List<Map<String, Object>> resultStats,
                                String analysis,
                                List<Map<String, Object>> rows,
                                Map<String, String> criteria) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(200);
        workbook.setCompressTempFiles(true);
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle bodyStyle = bodyStyle(workbook);
        CellStyle wrapStyle = wrapStyle(workbook);

        summarySheet(workbook, headerStyle, bodyStyle, summary, resultStats, analysis, criteria);
        dimensionSheet(workbook, headerStyle, bodyStyle, dimensionStats);
        detailSheet(workbook, headerStyle, bodyStyle, wrapStyle, rows);
        noteSheet(workbook, headerStyle, bodyStyle, criteria, rows.size());
        return workbook;
    }

    private void summarySheet(SXSSFWorkbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                              Map<String, Object> summary, List<Map<String, Object>> resultStats,
                              String analysis, Map<String, String> criteria) {
        SXSSFSheet sheet = workbook.createSheet("统计汇总");
        sheet.setColumnWidth(0, 22 * 256);
        sheet.setColumnWidth(1, 42 * 256);
        int rowIndex = 0;
        rowIndex = writeHeader(sheet, rowIndex, headerStyle, new String[]{"指标", "数值"});
        rowIndex = writeRow(sheet, rowIndex, bodyStyle, new Object[]{"完成人数", number(summary.get("participantCount"))});
        rowIndex = writeRow(sheet, rowIndex, bodyStyle, new Object[]{"挑战成功", number(summary.get("passedCount"))});
        rowIndex = writeRow(sheet, rowIndex, bodyStyle, new Object[]{"本局未通过", number(summary.get("failedCount"))});
        rowIndex = writeRow(sheet, rowIndex, bodyStyle, new Object[]{"整体成功率", text(summary.get("passRate")) + "%"});
        for (Map<String, Object> item : safe(resultStats)) {
            rowIndex = writeRow(sheet, rowIndex, bodyStyle,
                    new Object[]{text(item.get("resultName")) + "占比", text(item.get("probability")) + "%"});
        }
        rowIndex++;
        rowIndex = writeRow(sheet, rowIndex, bodyStyle, new Object[]{"导出条件", criteriaText(criteria)});
        rowIndex = writeRow(sheet, rowIndex, bodyStyle, new Object[]{"分析", analysis});
    }

    private void dimensionSheet(SXSSFWorkbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                List<Map<String, Object>> dimensionStats) {
        SXSSFSheet sheet = workbook.createSheet("维度统计");
        int[] widths = {8, 20, 12, 12, 12};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        int rowIndex = writeHeader(sheet, rowIndex(sheet), headerStyle,
                new String[]{"序号", "职业认知维度", "完成人数", "成功人数", "成功率"});
        for (Map<String, Object> item : safe(dimensionStats)) {
            rowIndex = writeRow(sheet, rowIndex, bodyStyle, new Object[]{
                    number(item.get("serialNo")),
                    text(item.get("dimensionName")),
                    number(item.get("participantCount")),
                    number(item.get("passedCount")),
                    text(item.get("passRate")) + "%"
            });
        }
    }

    private void detailSheet(SXSSFWorkbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                             CellStyle wrapStyle, List<Map<String, Object>> rows) {
        SXSSFSheet sheet = workbook.createSheet("学生明细");
        int[] widths = {6, 12, 16, 22, 18, 18, 8, 16, 12, 32, 18};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        int rowIndex = writeHeader(sheet, 0, headerStyle, DETAIL_HEADERS);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int serial = 1;
        for (Map<String, Object> item : safe(rows)) {
            Object createTime = item.get("createTime");
            rowIndex = writeRow(sheet, rowIndex, bodyStyle, new Object[]{
                    serial++,
                    text(item.get("studentName")),
                    text(item.get("studentNo")),
                    text(item.get("schoolName")),
                    text(item.get("departmentName")),
                    text(item.get("major")),
                    text(item.get("gender")),
                    text(item.get("dimensionName")),
                    text(item.get("resultName")),
                    text(item.get("careers")),
                    createTime instanceof Date ? format.format((Date) createTime) : text(createTime)
            }, wrapStyle, 9);
        }
    }

    private void noteSheet(SXSSFWorkbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                           Map<String, String> criteria, int rowCount) {
        SXSSFSheet sheet = workbook.createSheet("数据说明");
        sheet.setColumnWidth(0, 22 * 256);
        sheet.setColumnWidth(1, 80 * 256);
        int rowIndex = writeHeader(sheet, 0, headerStyle, new String[]{"项目", "说明"});
        rowIndex = writeRow(sheet, rowIndex, bodyStyle, new Object[]{"导出范围", criteriaText(criteria)});
        rowIndex = writeRow(sheet, rowIndex, bodyStyle, new Object[]{"明细行数", rowCount});
        rowIndex = writeRow(sheet, rowIndex, bodyStyle, new Object[]{"时间口径", "完成时间取个人报告生成时间（gkzh_sszctop_student_report.create_time），结束日期包含当天。"});
        rowIndex = writeRow(sheet, rowIndex, bodyStyle, new Object[]{"人数口径", "每位学生在同一活动只保留一份终局报告，完成人数按报告数统计。"});
        rowIndex = writeRow(sheet, rowIndex, bodyStyle, new Object[]{"维度说明", "所选维度来自报告生成时的维度快照，历史快照缺失时归入“未记录维度”。"});
        rowIndex = writeRow(sheet, rowIndex, bodyStyle, new Object[]{"导出时间", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())});
    }

    private int rowIndex(SXSSFSheet sheet) {
        return sheet.getLastRowNum() + 1;
    }

    private int writeHeader(SXSSFSheet sheet, int rowIndex, CellStyle style, String[] headers) {
        SXSSFRow row = sheet.createRow(rowIndex);
        for (int i = 0; i < headers.length; i++) {
            SXSSFCell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
        return rowIndex + 1;
    }

    private int writeRow(SXSSFSheet sheet, int rowIndex, CellStyle style, Object[] values) {
        return writeRow(sheet, rowIndex, style, values, null, -1);
    }

    /** 写入一行；wrapColumn 指定的列使用自动换行样式。 */
    private int writeRow(SXSSFSheet sheet, int rowIndex, CellStyle style, Object[] values,
                         CellStyle wrapStyle, int wrapColumn) {
        SXSSFRow row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            SXSSFCell cell = row.createCell(i);
            Object value = values[i];
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else {
                cell.setCellValue(text(value));
            }
            cell.setCellStyle(i == wrapColumn && wrapStyle != null ? wrapStyle : style);
        }
        return rowIndex + 1;
    }

    private String criteriaText(Map<String, String> criteria) {
        if (criteria == null || criteria.isEmpty()) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String, String> entry : criteria.entrySet()) {
            if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                continue;
            }
            if (text.length() > 0) {
                text.append("；");
            }
            text.append(entry.getKey()).append("：").append(entry.getValue());
        }
        return text.toString();
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
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle bodyStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle wrapStyle(SXSSFWorkbook workbook) {
        CellStyle style = bodyStyle(workbook);
        style.setWrapText(true);
        return style;
    }

    private static List<Map<String, Object>> safe(List<Map<String, Object>> rows) {
        return rows == null ? java.util.Collections.emptyList() : rows;
    }

    private static long number(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return value == null ? 0L : Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
