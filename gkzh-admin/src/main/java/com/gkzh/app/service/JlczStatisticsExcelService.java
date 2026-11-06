package com.gkzh.app.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.jlcz.domain.JlczRecord;
import com.gkzh.jlcz.domain.JlczSetting;
import com.gkzh.jlcz.domain.dto.JlczRecordQuery;
import com.gkzh.jlcz.mapper.JlczRecordMapper;
import com.gkzh.jlcz.service.JlczConfigService;
import com.gkzh.jlcz.service.JlczStatisticsService;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 简历称重统计 Excel 导出。
 * 工作簿包含统计汇总、砝码选择率、类别统计、学生明细和数据说明五个工作表，
 * 导出使用页面筛选条件但不带分页，便于后续直接处理数据。
 */
@Service
public class JlczStatisticsExcelService {

    private final JlczRecordMapper recordMapper;
    private final JlczRecordEnrichService enrichService;
    private final JlczStatisticsService statisticsService;
    private final JlczConfigService configService;
    private final GkzhSchoolMapper schoolMapper;
    private final GkzhSchoolDepartmentMapper departmentMapper;
    private final IActivityWeekService activityWeekService;

    public JlczStatisticsExcelService(JlczRecordMapper recordMapper, JlczRecordEnrichService enrichService,
                                      JlczStatisticsService statisticsService, JlczConfigService configService,
                                      GkzhSchoolMapper schoolMapper, GkzhSchoolDepartmentMapper departmentMapper,
                                      IActivityWeekService activityWeekService) {
        this.recordMapper = recordMapper;
        this.enrichService = enrichService;
        this.statisticsService = statisticsService;
        this.configService = configService;
        this.schoolMapper = schoolMapper;
        this.departmentMapper = departmentMapper;
        this.activityWeekService = activityWeekService;
    }

    /**
     * 生成导出工作簿；学校与活动必填，其他条件可选。
     *
     * @throws IOException 写入工作簿失败
     */
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public SXSSFWorkbook create(Long schoolId, Long instanceId, Long departmentId, String major, String gender,
                                String studentNo, String resultFilter, String statusFilter,
                                LocalDate startDate, LocalDate endDate) throws IOException {
        if (schoolId == null || instanceId == null) {
            throw new ServiceException("请先选择学校和活动");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        String result = normalizeResult(resultFilter);
        String status = normalizeStatus(statusFilter);
        GkzhSchool school = schoolMapper.selectGkzhSchoolBySchoolId(schoolId);
        if (school == null) {
            throw new ServiceException("所选学校不存在");
        }
        GkzhActivityWeekInstance instance = activityWeekService.getInstance(instanceId);
        if (instance == null) {
            throw new ServiceException("所选活动不存在");
        }

        JlczRecordQuery query = new JlczRecordQuery();
        query.setSchoolId(schoolId);
        query.setInstanceId(instanceId);
        query.setDepartmentId(departmentId);
        query.setMajor(blankToNull(major));
        query.setGenderValue(genderValue(gender));
        query.setStudentNo(blankToNull(studentNo));
        query.setResultValue(result);
        query.setStatus(status);
        query.setStartDate(startDate);
        query.setEndDate(endDate == null ? null : endDate.plusDays(1));

        List<JlczRecord> records = recordMapper.selectAdminRecords(query);
        if (records.isEmpty()) {
            throw new ServiceException("当前筛选条件下暂无可导出的参与数据");
        }
        List<Map<String, Object>> rows = enrichService.enrich(records);
        Map<String, Object> summary = statisticsService.summary(query);
        long finished = number(summary.get("finishedCount"));
        List<Map<String, Object>> weightStats = statisticsService.weightStats(query, finished);
        List<Map<String, Object>> categoryStats = statisticsService.categoryStats(weightStats, finished);
        List<Map<String, Object>> distribution = statisticsService.distribution(query, summary);

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle bodyStyle = bodyStyle(workbook);
        try {
            createSummarySheet(workbook, headerStyle, bodyStyle, summary, categoryStats, distribution);
            createWeightSheet(workbook, headerStyle, bodyStyle, weightStats);
            createCategorySheet(workbook, headerStyle, bodyStyle, categoryStats);
            createStudentSheet(workbook, headerStyle, bodyStyle, records, rows);
            createNoteSheet(workbook, headerStyle, bodyStyle, school, instance, departmentId, query, records.size());
            return workbook;
        } catch (RuntimeException e) {
            workbook.dispose();
            throw e;
        }
    }

    /** 第一页：参与、通关、平均重量/用时、类别小计与总重区间分布。 */
    private void createSummarySheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                    Map<String, Object> summary, List<Map<String, Object>> categoryStats,
                                    List<Map<String, Object>> distribution) {
        Sheet sheet = workbook.createSheet("统计汇总");
        int rowIndex = 0;
        Row title = sheet.createRow(rowIndex++);
        title.createCell(0).setCellValue("简历称重 统计汇总");
        title.getCell(0).setCellStyle(titleStyle(workbook));

        rowIndex = writeKeyValueRows(sheet, rowIndex, headerStyle, bodyStyle, new Object[][]{
                {"参与人数", summary.get("participantCount")},
                {"已称重人数", summary.get("finishedCount")},
                {"通关人数", summary.get("passCount")},
                {"未通关人数", summary.get("failCount")},
                {"通关率", summary.get("passRate") + "%"},
                {"平均简历分量（g）", summary.get("avgTotalWeight")},
                {"平均勾选砝码数", summary.get("avgSelectedCount")},
                {"平均称重用时（秒）", summary.get("avgDurationSeconds")}
        });

        rowIndex++;
        Row categoryTitle = sheet.createRow(rowIndex++);
        categoryTitle.createCell(0).setCellValue("四大类砝码获得情况");
        categoryTitle.getCell(0).setCellStyle(titleStyle(workbook));
        String[] categoryHeaders = {"类别", "砝码数", "可获重量(g)", "被选次数", "人均获得重量(g)", "类别选择率"};
        writeHeader(sheet.createRow(rowIndex++), categoryHeaders, headerStyle);
        for (Map<String, Object> row : categoryStats) {
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    text(row.get("categoryName")), row.get("weightCount"), row.get("availableWeight"),
                    row.get("selectedCount"), row.get("avgWeightPerParticipant"),
                    text(row.get("selectionRate")) + "%"
            }, bodyStyle);
        }

        rowIndex++;
        Row distributionTitle = sheet.createRow(rowIndex++);
        distributionTitle.createCell(0).setCellValue("简历总重区间分布");
        distributionTitle.getCell(0).setCellStyle(titleStyle(workbook));
        writeHeader(sheet.createRow(rowIndex++), new String[]{"总重区间", "人数", "占比"}, headerStyle);
        for (Map<String, Object> row : distribution) {
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    text(row.get("label")), row.get("count"), text(row.get("rate")) + "%"
            }, bodyStyle);
        }
        sheet.setColumnWidth(0, 26 * 256);
        sheet.setColumnWidth(1, 16 * 256);
        sheet.setColumnWidth(2, 16 * 256);
        sheet.setColumnWidth(3, 14 * 256);
        sheet.setColumnWidth(4, 20 * 256);
        sheet.setColumnWidth(5, 16 * 256);
    }

    /** 第二页：20 个砝码的勾选人数、选择率与未勾选缺口。 */
    private void createWeightSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                   List<Map<String, Object>> weightStats) {
        Sheet sheet = workbook.createSheet("砝码选择率");
        String[] headers = {"挂钩号", "类别", "砝码标签", "重量(g)", "勾选人数", "选择率", "未勾选人数", "状态"};
        writeHeader(sheet.createRow(0), headers, headerStyle);
        int rowIndex = 1;
        for (Map<String, Object> row : weightStats) {
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    row.get("weightNo"), text(row.get("categoryName")), text(row.get("label")),
                    row.get("weightG"), row.get("selectedCount"), text(row.get("selectedRate")) + "%",
                    row.get("gapCount"), "1".equals(text(row.get("status"))) ? "停用" : "启用"
            }, bodyStyle);
        }
        int[] widths = {8, 24, 18, 10, 12, 12, 14, 10};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        sheet.createFreezePane(0, 1);
    }

    /** 第三页：四大类的可获重量、被选次数、人均获得重量与类别选择率。 */
    private void createCategorySheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                     List<Map<String, Object>> categoryStats) {
        Sheet sheet = workbook.createSheet("类别统计");
        String[] headers = {"类别", "砝码数", "可获重量(g)", "被选次数", "被选总重量(g)", "人均获得重量(g)", "类别选择率"};
        writeHeader(sheet.createRow(0), headers, headerStyle);
        int rowIndex = 1;
        for (Map<String, Object> row : categoryStats) {
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    text(row.get("categoryName")), row.get("weightCount"), row.get("availableWeight"),
                    row.get("selectedCount"), row.get("selectedWeight"), row.get("avgWeightPerParticipant"),
                    text(row.get("selectionRate")) + "%"
            }, bodyStyle);
        }
        int[] widths = {26, 10, 14, 12, 16, 18, 14};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        sheet.createFreezePane(0, 1);
    }

    /** 第四页：每条参与记录的基础信息、结果与本次勾选的砝码。 */
    private void createStudentSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                    List<JlczRecord> records, List<Map<String, Object>> rows) {
        Sheet sheet = workbook.createSheet("学生明细");
        String[] headers = {"序号", "学校", "院系", "专业", "学号", "姓名", "性别", "活动", "总重量(g)",
                "结果", "勾选数", "用时(秒)", "称重次数", "参与时间", "提交时间", "勾选砝码"};
        writeHeader(sheet.createRow(0), headers, headerStyle);
        int rowIndex = 1;
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            JlczRecord record = records.get(i);
            writeCells(sheet.createRow(rowIndex), new Object[]{
                    rowIndex,
                    text(row.get("schoolName")),
                    text(row.get("departmentName")),
                    text(row.get("major")),
                    text(row.get("studentNo")),
                    text(row.get("studentName")),
                    text(row.get("gender")),
                    text(row.get("instanceName")),
                    row.get("totalWeight"),
                    resultText(text(row.get("status")), text(row.get("result"))),
                    row.get("selectedCount"),
                    seconds(row.get("durationMs")),
                    row.get("attemptNo"),
                    time(row.get("scanTime")),
                    time(row.get("finishTime")),
                    selectedLabels(record.getSelectedJson())
            }, bodyStyle);
            rowIndex++;
        }
        int[] widths = {8, 22, 20, 18, 16, 12, 8, 24, 12, 10, 10, 12, 12, 20, 20, 40};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        sheet.createFreezePane(0, 1);
    }

    /** 第五页：导出范围、筛选条件与计分口径。 */
    private void createNoteSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                 GkzhSchool school, GkzhActivityWeekInstance instance, Long departmentId,
                                 JlczRecordQuery query, int recordCount) {
        Sheet sheet = workbook.createSheet("数据说明");
        JlczSetting setting = configService.getSetting();
        String departmentName = departmentId == null ? "全部" : departmentName(departmentId);
        String dateRange = query.getStartDate() == null && query.getEndDate() == null ? "全部"
                : (query.getStartDate() == null ? "不限开始日期" : query.getStartDate().toString())
                + " 至 " + (query.getEndDate() == null ? "不限结束日期" : query.getEndDate().minusDays(1).toString());
        int rowIndex = writeKeyValueRows(sheet, 0, headerStyle, bodyStyle, new Object[][]{
                {"导出范围", school.getTitle() + " / " + instance.getTitle()},
                {"筛选条件-院系", departmentName},
                {"筛选条件-专业", query.getMajor() == null ? "全部" : query.getMajor()},
                {"筛选条件-性别", query.getGenderValue() == null ? "全部" : query.getGenderValue()},
                {"筛选条件-学号", query.getStudentNo() == null ? "全部" : query.getStudentNo()},
                {"筛选条件-结果", query.getResultValue() == null ? "全部"
                        : ("pass".equals(query.getResultValue()) ? "通关" : "未通关")},
                {"筛选条件-状态", query.getStatus() == null ? "全部"
                        : ("finished".equals(query.getStatus()) ? "已完成" : "进行中")},
                {"筛选条件-参与日期", dateRange},
                {"导出记录数", recordCount},
                {"通关口径", "左侧标准砝码 " + setting.getLeftWeightG() + "g，总重达到 "
                        + setting.getPassWeightG() + "g 即通关，恰好等于阈值视为刚好达标"},
                {"称重限时", setting.getTimeLimitSeconds() == null || setting.getTimeLimitSeconds() == 0
                        ? "不限时" : setting.getTimeLimitSeconds() + " 秒"},
                {"导出时间", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}
        });
        Row note = sheet.createRow(rowIndex + 1);
        note.createCell(0).setCellValue("说明：本表仅用于活动复盘和数据分析，不包含登录凭证等敏感信息。");
        note.getCell(0).setCellStyle(bodyStyle);
        sheet.setColumnWidth(0, 24 * 256);
        sheet.setColumnWidth(1, 90 * 256);
    }

    private int writeKeyValueRows(Sheet sheet, int rowIndex, CellStyle headerStyle, CellStyle bodyStyle,
                                  Object[][] values) {
        for (Object[] pair : values) {
            Row row = sheet.createRow(rowIndex++);
            Cell label = row.createCell(0);
            label.setCellValue(String.valueOf(pair[0]));
            label.setCellStyle(headerStyle);
            Cell value = row.createCell(1);
            value.setCellValue(pair[1] == null ? "" : String.valueOf(pair[1]));
            value.setCellStyle(bodyStyle);
        }
        return rowIndex;
    }

    private static void writeHeader(Row row, String[] headers, CellStyle style) {
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private static void writeCells(Row row, Object[] values, CellStyle style) {
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            Object value = values[i];
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else {
                cell.setCellValue(value == null ? "" : String.valueOf(value));
            }
            cell.setCellStyle(style);
        }
    }

    private CellStyle headerStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        border(style);
        return style;
    }

    private CellStyle bodyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        border(style);
        return style;
    }

    private CellStyle titleStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private static void border(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private String departmentName(Long departmentId) {
        try {
            return departmentMapper.selectDepartmentById(departmentId).getTitle();
        } catch (RuntimeException e) {
            return "院系编号：" + departmentId;
        }
    }

    /** 本次勾选的砝码标签，用于学生明细快速阅读。 */
    private static String selectedLabels(String selectedJson) {
        if (selectedJson == null || selectedJson.trim().isEmpty()) {
            return "";
        }
        try {
            JSONArray array = JSON.parseArray(selectedJson);
            List<String> labels = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                JSONObject item = array.getJSONObject(i);
                labels.add(item.getString("label"));
            }
            return String.join("、", labels);
        } catch (RuntimeException e) {
            return "";
        }
    }

    private static String resultText(String status, String result) {
        if (!"finished".equals(status)) {
            return "进行中";
        }
        return "pass".equals(result) ? "通关" : "未通关";
    }

    private static String normalizeResult(String value) {
        String result = blankToNull(value);
        if (result == null) {
            return null;
        }
        if (!"pass".equals(result) && !"fail".equals(result)) {
            throw new ServiceException("称重结果筛选值无效");
        }
        return result;
    }

    private static String normalizeStatus(String value) {
        String status = blankToNull(value);
        if (status == null) {
            return null;
        }
        if (!"finished".equals(status) && !"inProgress".equals(status)) {
            throw new ServiceException("参与状态筛选值无效");
        }
        return status;
    }

    private static String genderValue(String value) {
        String gender = blankToNull(value);
        return gender == null ? null : JlczRecordEnrichService.genderText(gender);
    }

    private static String seconds(Object value) {
        if (!(value instanceof Number)) {
            return "";
        }
        return BigDecimal.valueOf(((Number) value).longValue() / 1000D)
                .setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    private static long number(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return value == null ? 0L : Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static String time(Object value) {
        if (!(value instanceof Date)) {
            return value == null ? "" : String.valueOf(value);
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) value);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
