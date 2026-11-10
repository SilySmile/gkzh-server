package com.gkzh.app.service;

import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.zytj.domain.ZytjItem;
import com.gkzh.zytj.domain.ZytjRecord;
import com.gkzh.zytj.domain.ZytjSetting;
import com.gkzh.zytj.domain.dto.ZytjRecordQuery;
import com.gkzh.zytj.mapper.ZytjRecordMapper;
import com.gkzh.zytj.service.ZytjConfigService;
import com.gkzh.zytj.service.ZytjStatisticsService;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 职愿探究统计 Excel 导出。
 * 工作簿包含统计汇总、16 型分布、维度倾向、院系分布、学生明细和数据说明六个工作表，
 * 导出使用页面筛选条件但不带分页，便于后续直接处理数据。
 */
@Service
public class ZytjStatisticsExcelService {

    private final ZytjRecordMapper recordMapper;
    private final ZytjRecordEnrichService enrichService;
    private final ZytjStatisticsService statisticsService;
    private final ZytjConfigService configService;
    private final GkzhSchoolMapper schoolMapper;
    private final GkzhSchoolDepartmentMapper departmentMapper;
    private final IActivityWeekService activityWeekService;

    public ZytjStatisticsExcelService(ZytjRecordMapper recordMapper, ZytjRecordEnrichService enrichService,
                                       ZytjStatisticsService statisticsService, ZytjConfigService configService,
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
                                String studentNo, String typeCode, String statusFilter,
                                LocalDate startDate, LocalDate endDate) throws IOException {
        if (schoolId == null || instanceId == null) {
            throw new ServiceException("请先选择学校和活动");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        String status = normalizeStatus(statusFilter);
        GkzhSchool school = schoolMapper.selectGkzhSchoolBySchoolId(schoolId);
        if (school == null) {
            throw new ServiceException("所选学校不存在");
        }
        GkzhActivityWeekInstance instance = activityWeekService.getInstance(instanceId);
        if (instance == null) {
            throw new ServiceException("所选活动不存在");
        }

        ZytjRecordQuery query = new ZytjRecordQuery();
        query.setSchoolId(schoolId);
        query.setInstanceId(instanceId);
        query.setDepartmentId(departmentId);
        query.setMajor(blankToNull(major));
        query.setGenderValue(genderValue(gender));
        query.setStudentNo(blankToNull(studentNo));
        query.setTypeCode(blankToNull(typeCode) == null ? null : typeCode.trim().toUpperCase());
        query.setStatus(status);
        query.setStartDate(startDate);
        query.setEndDate(endDate == null ? null : endDate.plusDays(1));

        List<ZytjRecord> records = recordMapper.selectAdminRecords(query);
        if (records.isEmpty()) {
            throw new ServiceException("当前筛选条件下暂无可导出的参与数据");
        }
        List<Map<String, Object>> rows = enrichService.enrich(records);
        Map<String, Object> summary = statisticsService.summary(query);
        long finished = number(summary.get("finishedCount"));
        long total = number(summary.get("participantCount"));
        List<Map<String, Object>> typeStats = statisticsService.typeStats(query, finished);
        List<Map<String, Object>> dimensionStats = statisticsService.dimensionStats(query, finished);
        List<Map<String, Object>> departmentStats = statisticsService.departmentStats(query, total);

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle bodyStyle = bodyStyle(workbook);
        try {
            createSummarySheet(workbook, headerStyle, bodyStyle, summary);
            createTypeSheet(workbook, headerStyle, bodyStyle, typeStats);
            createDimensionSheet(workbook, headerStyle, bodyStyle, dimensionStats);
            createDepartmentSheet(workbook, headerStyle, bodyStyle, departmentStats);
            createStudentSheet(workbook, headerStyle, bodyStyle, rows);
            createNoteSheet(workbook, headerStyle, bodyStyle, school, instance, departmentId, query, records.size());
            return workbook;
        } catch (RuntimeException e) {
            workbook.dispose();
            throw e;
        }
    }

    /** 第一页：参与、提交、超时与平均用时等概况。 */
    private void createSummarySheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                    Map<String, Object> summary) {
        Sheet sheet = workbook.createSheet("统计汇总");
        int rowIndex = 0;
        Row title = sheet.createRow(rowIndex++);
        title.createCell(0).setCellValue("职愿探究 统计汇总");
        title.getCell(0).setCellStyle(titleStyle(workbook));

        writeKeyValueRows(sheet, rowIndex, headerStyle, bodyStyle, new Object[][]{
                {"参与人数", summary.get("participantCount")},
                {"已生成探索卡人数", summary.get("finishedCount")},
                {"进行中人数", summary.get("inProgressCount")},
                {"完成率", summary.get("finishRate") + "%"},
                {"已识别人格类型人数", summary.get("typedCount")},
                {"超时自动提交人数", summary.get("timeoutCount")},
                {"超时占比", summary.get("timeoutRate") + "%"},
                {"出现的型别数量", summary.get("typeCount")},
                {"平均用时（秒）", summary.get("avgDurationSeconds")},
                {"占比最高型别", summary.get("topTypeCode")},
                {"最受欢迎型别人数", summary.get("topTypeCount")},
                {"最受欢迎型别占比", summary.get("topTypeRate") + "%"}
        });
        sheet.setColumnWidth(0, 26 * 256);
        sheet.setColumnWidth(1, 40 * 256);
    }

    /** 第二页：16 型分布。 */
    private void createTypeSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                 List<Map<String, Object>> typeStats) {
        Sheet sheet = workbook.createSheet("16型分布");
        Row header = sheet.createRow(0);
        writeHeader(header, new String[]{"人格类型", "人数", "占比", "性格特点", "适合职业领域", "典型职业"}, headerStyle);
        int rowIndex = 1;
        for (Map<String, Object> row : typeStats) {
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    row.get("typeCode"),
                    row.get("selectedCount"),
                    text(row.get("rate")) + "%",
                    row.get("feature"),
                    row.get("careerField"),
                    row.get("typicalCareers")
            }, bodyStyle);
        }
        for (int i = 0; i < 6; i++) {
            sheet.setColumnWidth(i, (i == 0 ? 12 : (i < 3 ? 12 : 34)) * 256);
        }
    }

    /** 第三页：四个维度的字母倾向。 */
    private void createDimensionSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                      List<Map<String, Object>> dimensionStats) {
        Sheet sheet = workbook.createSheet("维度倾向");
        Row header = sheet.createRow(0);
        writeHeader(header, new String[]{"维度", "字母", "代表物品", "人数", "占比"}, headerStyle);
        int rowIndex = 1;
        for (Map<String, Object> row : dimensionStats) {
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    dimensionText(text(row.get("dimensionCode"))),
                    row.get("letter"),
                    row.get("itemName"),
                    row.get("selectedCount"),
                    text(row.get("rate")) + "%"
            }, bodyStyle);
        }
        for (int i = 0; i < 5; i++) {
            sheet.setColumnWidth(i, 18 * 256);
        }
    }

    /** 第四页：院系分布。 */
    private void createDepartmentSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                       List<Map<String, Object>> departmentStats) {
        Sheet sheet = workbook.createSheet("院系分布");
        Row header = sheet.createRow(0);
        writeHeader(header, new String[]{"院系", "参与人数", "已提交人数", "参与占比"}, headerStyle);
        int rowIndex = 1;
        for (Map<String, Object> row : departmentStats) {
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    row.get("departmentName"),
                    row.get("participantCount"),
                    row.get("finishedCount"),
                    text(row.get("rate")) + "%"
            }, bodyStyle);
        }
        for (int i = 0; i < 4; i++) {
            sheet.setColumnWidth(i, (i == 0 ? 30 : 16) * 256);
        }
    }

    /** 第五页：学生明细。 */
    private void createStudentSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                    List<Map<String, Object>> rows) {
        Sheet sheet = workbook.createSheet("学生明细");
        Row header = sheet.createRow(0);
        writeHeader(header, new String[]{"学校", "活动", "学号", "姓名", "性别", "院系", "专业",
                "人格类型", "性格特点", "适合职业领域", "典型职业", "选择物品", "是否超时", "用时（秒）",
                "参与状态", "扫码时间", "提交时间"}, headerStyle);
        int rowIndex = 1;
        for (Map<String, Object> row : rows) {
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    row.get("schoolName"),
                    row.get("instanceName"),
                    row.get("studentNo"),
                    row.get("studentName"),
                    row.get("gender"),
                    row.get("departmentName"),
                    row.get("major"),
                    row.get("typeCode"),
                    row.get("feature"),
                    row.get("careerField"),
                    row.get("typicalCareers"),
                    itemNames(text(row.get("itemCodes"))),
                    Boolean.TRUE.equals(row.get("isTimeout")) ? "是" : "否",
                    seconds(row.get("durationMs")),
                    statusText(text(row.get("status"))),
                    time(row.get("scanTime")),
                    time(row.get("finishTime"))
            }, bodyStyle);
        }
        int[] widths = {18, 22, 16, 12, 8, 20, 20, 12, 30, 26, 30, 30, 10, 12, 12, 20, 20};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }

    /** 第六页：数据说明与口径。 */
    private void createNoteSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                 GkzhSchool school, GkzhActivityWeekInstance instance, Long departmentId,
                                 ZytjRecordQuery query, int recordCount) {
        Sheet sheet = workbook.createSheet("数据说明");
        ZytjSetting setting = configService.getSetting();
        String dateRange = (query.getStartDate() == null ? "不限" : query.getStartDate().toString())
                + " ~ "
                + (query.getEndDate() == null ? "不限" : query.getEndDate().minusDays(1).toString());
        int rowIndex = 0;
        Row title = sheet.createRow(rowIndex++);
        title.createCell(0).setCellValue("职愿探究 数据说明");
        title.getCell(0).setCellStyle(titleStyle(workbook));
        writeKeyValueRows(sheet, rowIndex, headerStyle, bodyStyle, new Object[][]{
                {"学校", school.getTitle()},
                {"活动", instance.getTitle()},
                {"院系筛选", departmentId == null ? "全部" : departmentName(departmentId)},
                {"筛选条件-参与状态", query.getStatus() == null ? "全部"
                        : ("finished".equals(query.getStatus()) ? "已提交" : "进行中")},
                {"筛选条件-人格类型", query.getTypeCode() == null ? "全部" : query.getTypeCode()},
                {"筛选条件-参与日期", dateRange},
                {"导出记录数", recordCount},
                {"型别占比口径", "以已生成探索卡人数为分母，同一学生只计一次"},
                {"维度倾向口径", "按所选物品的四列位置拆分 item_codes，统计 E/I、S/N、T/F、J/P 分布"},
                {"院系口径", "取专业的父部门；未填写专业或部门缺失归入“未填写院系”"},
                {"单局限时", setting.getTimeLimit() == null ? "0（不限时）" : setting.getTimeLimit() + " 秒"},
                {"超时处理", "到点未选满时由服务端按该列首个启用物品补全后提交，并标记超时"},
                {"重新探究", "1".equals(setting.getAllowReselect()) ? "允许（统计取最新一次）" : "不允许，提交后只读"},
                {"导出时间", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}
        });
        Row note = sheet.createRow(rowIndex + 1);
        note.createCell(0).setCellValue("说明：本表仅用于活动复盘和数据分析，不包含登录凭证等敏感信息。");
        note.getCell(0).setCellStyle(bodyStyle);
        sheet.setColumnWidth(0, 24 * 256);
        sheet.setColumnWidth(1, 90 * 256);
    }

    /** 把“E,N,T,P”翻译成物品名称，便于非技术老师阅读明细。 */
    private String itemNames(String itemCodes) {
        if (itemCodes == null || itemCodes.trim().isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String code : itemCodes.split(",")) {
            String letter = code.trim();
            if (letter.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('、');
            }
            builder.append(nameOf(letter));
        }
        return builder.toString();
    }

    private String nameOf(String letter) {
        for (ZytjItem item : configService.listItems()) {
            if (letter.equals(item.getItemCode())) {
                return item.getItemName();
            }
        }
        return letter;
    }

    private static String dimensionText(String dimensionCode) {
        if ("EI".equals(dimensionCode)) {
            return "外向/内向";
        }
        if ("SN".equals(dimensionCode)) {
            return "实感/直觉";
        }
        if ("TF".equals(dimensionCode)) {
            return "思考/情感";
        }
        if ("JP".equals(dimensionCode)) {
            return "判断/感知";
        }
        return dimensionCode;
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

    private static String statusText(String status) {
        return "finished".equals(status) ? "已提交" : "进行中";
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
        return gender == null ? null : ZytjRecordEnrichService.genderText(gender);
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
