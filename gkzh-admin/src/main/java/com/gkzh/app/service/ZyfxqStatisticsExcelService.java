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
 * 职业飞行棋统计 Excel 导出（结果输出-学校端）。
 * 工作簿包含统计汇总、知识点统计、题目统计、学生明细和数据说明，字段与 Web 统计页一致。
 */
@Service
public class ZyfxqStatisticsExcelService {

    /** 单个工作表的最大数据行数，超过后自动续表。 */
    private static final int SHEET_ROW_LIMIT = 60000;

    /** 生成工作簿；调用方负责写出响应并释放临时文件。 */
    public SXSSFWorkbook create(Map<String, Object> summary, List<Map<String, Object>> typeStats,
                                List<Map<String, Object>> questionStats, List<Map<String, Object>> wrongQuestions,
                                String analysis, List<Map<String, Object>> rows,
                                Long schoolId, Long instanceId, LocalDate startDate, LocalDate endDate) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        CellStyle titleStyle = titleStyle(workbook);
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle bodyStyle = bodyStyle(workbook);
        CellStyle wrapStyle = wrapStyle(workbook);

        summarySheet(workbook, summary, analysis, schoolId, instanceId, startDate, endDate,
                titleStyle, headerStyle, bodyStyle, wrapStyle);
        typeSheet(workbook, typeStats, titleStyle, headerStyle, bodyStyle);
        questionSheet(workbook, questionStats, titleStyle, headerStyle, bodyStyle);
        studentSheets(workbook, rows, titleStyle, headerStyle, bodyStyle);
        noteSheet(workbook, titleStyle, headerStyle, wrapStyle);
        return workbook;
    }

    /** 第一页：参与总人数、平均单局耗时、累计掷骰次数、整体通过人数与整体分析。 */
    private void summarySheet(SXSSFWorkbook workbook, Map<String, Object> summary, String analysis,
                              Long schoolId, Long instanceId, LocalDate startDate, LocalDate endDate,
                              CellStyle titleStyle, CellStyle headerStyle, CellStyle bodyStyle, CellStyle wrapStyle) {
        SXSSFSheet sheet = workbook.createSheet("统计汇总");
        sheet.setColumnWidth(0, 26 * 256);
        sheet.setColumnWidth(1, 34 * 256);
        sheet.setColumnWidth(2, 26 * 256);
        sheet.setColumnWidth(3, 34 * 256);

        SXSSFRow title = sheet.createRow(0);
        title.setHeightInPoints(30);
        title.createCell(0).setCellValue("职业飞行棋参与统计");
        title.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        int rowIndex = 2;
        rowIndex = metric(sheet, rowIndex, "学校编号", schoolId, headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "活动实例编号", instanceId, headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "参与日期", dateRange(startDate, endDate), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "参与总人数", summary.get("participantCount"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "对局结束人数", summary.get("finishedCount"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "整体通过人数", summary.get("winCount"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "未通关人数", summary.get("loseCount"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "对局中人数", summary.get("playingCount"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "整体通过率", summary.get("passRate") + "%", headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "参与者平均单局耗时", summary.get("avgDurationSeconds") + " 秒",
                headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "全员累计掷骰总次数", summary.get("rollTotal"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "全员累计行进步数", summary.get("stepTotal"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "累计作答题次", summary.get("answerTotal"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "整体答题正确率", summary.get("correctRate") + "%", headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "超时答题占比", summary.get("timeoutRate") + "%", headerStyle, bodyStyle);

        rowIndex += 1;
        SXSSFRow analysisRow = sheet.createRow(rowIndex);
        analysisRow.setHeightInPoints(150);
        analysisRow.createCell(0).setCellValue("整体分析");
        analysisRow.getCell(0).setCellStyle(headerStyle);
        analysisRow.createCell(1).setCellValue(analysis == null ? "" : analysis);
        analysisRow.getCell(1).setCellStyle(wrapStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 3));
    }

    /** 第二页：各知识点（题型）正确率与超时占比。 */
    private void typeSheet(SXSSFWorkbook workbook, List<Map<String, Object>> typeStats,
                           CellStyle titleStyle, CellStyle headerStyle, CellStyle bodyStyle) {
        SXSSFSheet sheet = workbook.createSheet("知识点统计");
        int[] widths = {8, 20, 12, 12, 12, 12, 12, 16};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        SXSSFRow title = sheet.createRow(0);
        title.setHeightInPoints(30);
        title.createCell(0).setCellValue("各知识点正确率统计");
        title.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, widths.length - 1));

        String[] headers = {"序号", "知识点", "作答次数", "答对次数", "正确率", "超时次数", "超时占比", "平均作答用时"};
        SXSSFRow header = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            header.getCell(i).setCellStyle(headerStyle);
        }
        int rowIndex = 3;
        for (Map<String, Object> item : typeStats) {
            SXSSFRow row = sheet.createRow(rowIndex++);
            String[] values = {String.valueOf(item.get("serialNo")), text(item.get("questionTypeName")),
                    String.valueOf(item.get("answerCount")), String.valueOf(item.get("correctCount")),
                    item.get("correctRate") + "%", String.valueOf(item.get("timeoutCount")),
                    item.get("timeoutRate") + "%", item.get("avgCostSeconds") + " 秒"};
            for (int i = 0; i < values.length; i++) {
                row.createCell(i).setCellValue(values[i]);
                row.getCell(i).setCellStyle(bodyStyle);
            }
        }
    }

    /** 第二页：全活动各题目正确率、平均作答用时和高频错题标记。 */
    private void questionSheet(SXSSFWorkbook workbook, List<Map<String, Object>> questionStats,
                               CellStyle titleStyle, CellStyle headerStyle, CellStyle bodyStyle) {
        SXSSFSheet sheet = workbook.createSheet("题目统计");
        int[] widths = {8, 16, 60, 12, 12, 12, 12, 12, 18};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        SXSSFRow title = sheet.createRow(0);
        title.setHeightInPoints(30);
        title.createCell(0).setCellValue("各题目正确率与作答用时统计");
        title.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, widths.length - 1));

        String[] headers = {"序号", "知识点", "题目", "作答次数", "答对次数", "答错次数", "正确率", "超时次数", "平均作答用时"};
        SXSSFRow header = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            header.getCell(i).setCellStyle(headerStyle);
        }
        int rowIndex = 3;
        for (Map<String, Object> item : questionStats) {
            SXSSFRow row = sheet.createRow(rowIndex++);
            String[] values = {String.valueOf(item.get("serialNo")), text(item.get("questionTypeName")),
                    text(item.get("title")), String.valueOf(item.get("answerCount")),
                    String.valueOf(item.get("correctCount")), String.valueOf(item.get("wrongCount")),
                    item.get("correctRate") + "%", String.valueOf(item.get("timeoutCount")),
                    item.get("avgCostSeconds") + " 秒"};
            for (int i = 0; i < values.length; i++) {
                row.createCell(i).setCellValue(values[i]);
                row.getCell(i).setCellStyle(bodyStyle);
            }
        }
    }

    /** 第三页及续表：逐名学生明细。 */
    private void studentSheets(SXSSFWorkbook workbook, List<Map<String, Object>> rows,
                               CellStyle titleStyle, CellStyle headerStyle, CellStyle bodyStyle) {
        String[] headers = {"序号", "学生姓名", "学号", "学校", "院系", "专业", "性别", "活动实例",
                "第几局", "状态", "结果", "终点格号", "掷骰次数", "行进步数", "作答题数", "答对题数", "耗时(秒)",
                "参与时间", "本局开始", "本局结束"};
        int[] widths = {7, 12, 16, 20, 20, 18, 8, 24, 8, 12, 10, 10, 10, 10, 10, 10, 10, 20, 20, 20};
        int page = 1;
        int rowIndex = 3;
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
                    text(item.get("gender")), text(item.get("instanceName")), text(item.get("attemptNo")),
                    statusText(item.get("status")), text(item.get("resultName")), text(item.get("position")),
                    text(item.get("rollCount")), text(item.get("stepTotal")), text(item.get("answerCount")),
                    text(item.get("correctCount")), text(item.get("durationSeconds")),
                    time(item.get("scanTime")), time(item.get("startTime")), time(item.get("endTime"))};
            for (int i = 0; i < values.length; i++) {
                row.createCell(i).setCellValue(values[i]);
                row.getCell(i).setCellStyle(bodyStyle);
            }
        }
    }

    private SXSSFSheet newStudentSheet(SXSSFWorkbook workbook, int page, String[] headers, int[] widths,
                                       CellStyle titleStyle, CellStyle headerStyle) {
        String name = page == 1 ? "学生明细" : "学生明细" + page;
        SXSSFSheet sheet = workbook.createSheet(name);
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        SXSSFRow title = sheet.createRow(0);
        title.setHeightInPoints(30);
        title.createCell(0).setCellValue("职业飞行棋学生参与明细");
        title.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));
        SXSSFRow header = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            header.getCell(i).setCellStyle(headerStyle);
        }
        return sheet;
    }

    /** 第四页：筛选口径、计分规则与隐私说明。 */
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
                {"统计口径", "参与总人数按职业飞行棋参与记录去重；同一学生在同一活动游戏下只有一条记录。整体通过人数只统计结果为 win 的记录。"},
                {"对局规则", "19 格环形棋盘，起点为位置 0；单骰取 1-6 点，点数超过终点按抵达终点处理；落在答题格需在限时内作答。"},
                {"答题判定", "答对原地继续掷骰，答错或超时后退固定格数；单题超时按答错处理，并计入超时次数。"},
                {"通过标准", "3 分钟内抵达第 19 格为通关并标记活动完成；未通关可重开一局，重开后 attempt_no 累加，参与记录仍复用同一条。"},
                {"耗时口径", "单局耗时 = 本局结束时间 - 本局开始时间；未通关按单局总时长计入。平均单局耗时只统计已结束的对局。"},
                {"知识点", "知识点按题型划分为“职业问答”和“情景任务卡”，正确率 = 答对次数 ÷ 作答次数；超时占比 = 超时次数 ÷ 作答次数。"},
                {"题目统计", "题目统计按题目编号汇总；题干、选项与正确答案在答题时已快照保存，题库后续修改不影响历史统计。"},
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
        return (startDate == null ? "不限" : startDate.toString()) + " 至 "
                + (endDate == null ? "不限" : endDate.toString());
    }

    /** 记录状态中文名称。 */
    private String statusText(Object value) {
        String status = value == null ? "" : String.valueOf(value);
        if ("ready".equals(status)) {
            return "未开始";
        }
        if ("playing".equals(status)) {
            return "对局中";
        }
        if ("finished".equals(status)) {
            return "已结束";
        }
        return status;
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String time(Object value) {
        return value instanceof Date ? DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", (Date) value) : text(value);
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
