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
 * 能量雷达统计 Excel 导出。
 * 工作簿包含统计汇总、维度偏好、学生明细、选择明细和数据说明，字段与 Web 统计页保持一致。
 */
@Service
public class NlldStatisticsExcelService {

    /** 单个工作表的最大数据行数，超过后自动续表。 */
    private static final int SHEET_ROW_LIMIT = 60000;

    /**
     * 生成工作簿；调用方负责写出响应并释放临时文件。
     */
    public SXSSFWorkbook create(Map<String, Object> summary, List<Map<String, Object>> dimensionStats,
                                List<Map<String, Object>> resultTypeStats,
                                List<Map<String, Object>> departmentStats, String analysis,
                                List<Map<String, Object>> rows,
                                Long schoolId, Long instanceId, LocalDate startDate, LocalDate endDate) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        CellStyle titleStyle = titleStyle(workbook);
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle bodyStyle = bodyStyle(workbook);
        CellStyle wrapStyle = wrapStyle(workbook);

        summarySheet(workbook, summary, analysis, schoolId, instanceId, startDate, endDate,
                titleStyle, headerStyle, bodyStyle, wrapStyle);
        dimensionSheet(workbook, dimensionStats, titleStyle, headerStyle, bodyStyle);
        departmentSheet(workbook, departmentStats, titleStyle, headerStyle, bodyStyle);
        studentSheets(workbook, rows, titleStyle, headerStyle, bodyStyle);
        noteSheet(workbook, titleStyle, headerStyle, wrapStyle);
        return workbook;
    }

    /** 第一页：参与、完成、结果类型和分析。 */
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
        title.createCell(0).setCellValue("能量雷达参与统计");
        title.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        int rowIndex = 2;
        rowIndex = metric(sheet, rowIndex, "学校编号", schoolId, headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "活动实例编号", instanceId, headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "参与日期", dateRange(startDate, endDate), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "扫码参与人数", summary.get("enteredCount"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "开始答题人数", summary.get("startedCount"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "完成测评人数", summary.get("completedCount"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "完成率", summary.get("completionRate") + "%", headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "TOP2 类型人数", summary.get("top2Count"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "TOP2 类型占比", summary.get("top2Rate") + "%", headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "多元开放型人数", summary.get("openCount"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "多元开放型占比", summary.get("openRate") + "%", headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "保存截图人数", summary.get("savedCount"), headerStyle, bodyStyle);
        rowIndex = metric(sheet, rowIndex, "保存截图率", summary.get("savedRate") + "%", headerStyle, bodyStyle);

        rowIndex += 1;
        SXSSFRow analysisRow = sheet.createRow(rowIndex);
        analysisRow.setHeightInPoints(150);
        analysisRow.createCell(0).setCellValue("整体偏好分析");
        analysisRow.getCell(0).setCellStyle(headerStyle);
        analysisRow.createCell(1).setCellValue(analysis == null ? "" : analysis);
        analysisRow.getCell(1).setCellStyle(wrapStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 3));
    }

    /** 第二页：15 个维度的偏好占比和 TOP2 命中情况。 */
    private void dimensionSheet(SXSSFWorkbook workbook, List<Map<String, Object>> dimensionStats,
                                CellStyle titleStyle, CellStyle headerStyle, CellStyle bodyStyle) {
        SXSSFSheet sheet = workbook.createSheet("维度偏好");
        int[] widths = {8, 18, 24, 12, 12, 12, 12, 14, 12};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        SXSSFRow title = sheet.createRow(0);
        title.setHeightInPoints(30);
        title.createCell(0).setCellValue("各职业价值观维度偏好占比统计");
        title.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, widths.length - 1));

        String[] headers = {"序号", "维度", "分组", "被选次数", "选择占比", "得2分人数", "偏好率", "进入TOP2人数", "TOP2占比"};
        SXSSFRow header = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            header.getCell(i).setCellStyle(headerStyle);
        }
        int rowIndex = 3;
        for (Map<String, Object> dimension : dimensionStats) {
            SXSSFRow row = sheet.createRow(rowIndex++);
            String[] values = {String.valueOf(dimension.get("serialNo")), text(dimension.get("dimensionName")),
                    text(dimension.get("dimensionGroup")), String.valueOf(dimension.get("selectedCount")),
                    dimension.get("selectedRate") + "%", String.valueOf(dimension.get("twoPointCount")),
                    dimension.get("twoPointRate") + "%", String.valueOf(dimension.get("top2Count")),
                    dimension.get("top2Rate") + "%"};
            for (int i = 0; i < values.length; i++) {
                row.createCell(i).setCellValue(values[i]);
                row.getCell(i).setCellStyle(bodyStyle);
            }
        }
    }

    /** 第三页：院系维度的选择细化；同一院系重复参与/完成人数，便于按院系筛选和透视。 */
    private void departmentSheet(SXSSFWorkbook workbook, List<Map<String, Object>> departmentStats,
                                 CellStyle titleStyle, CellStyle headerStyle, CellStyle bodyStyle) {
        SXSSFSheet sheet = workbook.createSheet("院系维度偏好");
        int[] widths = {8, 18, 10, 10, 10, 14, 18, 10, 10, 10, 10, 12, 10};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        SXSSFRow title = sheet.createRow(0);
        title.setHeightInPoints(30);
        title.createCell(0).setCellValue("各院系职业价值观维度偏好细化");
        title.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, widths.length - 1));

        String[] headers = {"序号", "院系", "参与人数", "完成人数", "完成率", "维度", "分组",
                "被选次数", "选择占比", "得2分人数", "偏好率", "进入TOP2人数", "TOP2占比"};
        SXSSFRow header = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            header.getCell(i).setCellStyle(headerStyle);
        }
        int rowIndex = 3;
        int serial = 1;
        for (Map<String, Object> item : departmentStats) {
            SXSSFRow row = sheet.createRow(rowIndex++);
            String[] values = {String.valueOf(serial++), text(item.get("collegeName")),
                    String.valueOf(item.get("participantCount")), String.valueOf(item.get("completedCount")),
                    item.get("completionRate") + "%", text(item.get("dimensionName")),
                    text(item.get("dimensionGroup")), String.valueOf(item.get("selectedCount")),
                    item.get("selectedRate") + "%", String.valueOf(item.get("twoPointCount")),
                    item.get("twoPointRate") + "%", String.valueOf(item.get("topDimensionCount")),
                    item.get("topDimensionRate") + "%"};
            for (int i = 0; i < values.length; i++) {
                row.createCell(i).setCellValue(values[i]);
                row.getCell(i).setCellStyle(bodyStyle);
            }
        }
    }

    /** 第四页及续表：逐名学生明细。 */
    private void studentSheets(SXSSFWorkbook workbook, List<Map<String, Object>> rows,
                               CellStyle titleStyle, CellStyle headerStyle, CellStyle bodyStyle) {
        String[] headers = {"序号", "学生姓名", "学号", "学校", "院系", "专业", "性别", "活动实例",
                "TOP1", "TOP2", "结果类型", "已答关数", "参与时间", "完成时间", "保存截图", "保存时间"};
        int[] widths = {7, 12, 16, 20, 20, 18, 8, 24, 14, 14, 14, 10, 20, 20, 10, 20};
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
                    text(item.get("gender")), text(item.get("instanceName")), text(item.get("top1DimensionName")),
                    text(item.get("top2DimensionName")), text(item.get("resultTypeName")),
                    text(item.get("answeredRounds")), time(item.get("scanTime")), time(item.get("finishTime")),
                    text(item.get("posterSavedName")), time(item.get("posterSaveTime"))};
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
        title.createCell(0).setCellValue("能量雷达学生参与明细");
        title.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));
        SXSSFRow header = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            header.getCell(i).setCellStyle(headerStyle);
        }
        return sheet;
    }

    /** 第五页：筛选口径与字段说明。 */
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
                {"统计口径", "扫码参与人数按能量雷达参与记录去重；同一学生在同一活动游戏下只有一条记录。完成测评人数只统计 6 关全部提交、结果已生成的记录。"},
                {"计分规则", "6 关、每关从 5 件物品中选择 2 件；15 个维度在全表 30 件物品中各出现 2 次，单维度最高 2 分、总得分恒为 12。"},
                {"结果类型", "恰好 2 个维度得 2 分为 TOP2；只有 1 个维度得 2 分、3 个及以上并列 2 分、没有任何维度得 2 分统一为多元开放型。"},
                {"维度偏好", "被选次数按选择明细统计；选择占比 = 该维度被选次数 ÷ 全部维度被选次数。得2分人数 = 该维度被选中 2 次的学生数；偏好率 = 得2分人数 ÷ 完成测评人数。"},
                {"院系细化", "“院系维度偏好”按学生专业所属的父部门统计；未填写专业或部门缺失的学生归入“未填写院系”。同一院系的参与/完成人数会在该院系每一行重复，便于按院系筛选和透视。"},
                {"保存截图", "保存截图人数按参与记录 poster_saved=1 去重统计；每场活动游戏只记录首次点击，重复点击不重复计数；保存截图率 = 保存截图人数 ÷ 完成测评人数。"},
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
