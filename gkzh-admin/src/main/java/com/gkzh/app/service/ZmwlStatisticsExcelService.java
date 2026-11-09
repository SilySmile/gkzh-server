package com.gkzh.app.service;

import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
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
 * 职面未来统计 Excel 导出。
 * 工作簿包含统计汇总、方向分布、院系偏好、学生明细和数据说明五个工作表，
 * 导出使用页面筛选条件但不带分页，便于后续直接处理数据。
 */
@Service
public class ZmwlStatisticsExcelService {

    private final ZmwlRecordMapper recordMapper;
    private final ZmwlRecordEnrichService enrichService;
    private final ZmwlStatisticsService statisticsService;
    private final ZmwlConfigService configService;
    private final GkzhSchoolMapper schoolMapper;
    private final GkzhSchoolDepartmentMapper departmentMapper;
    private final IActivityWeekService activityWeekService;

    public ZmwlStatisticsExcelService(ZmwlRecordMapper recordMapper, ZmwlRecordEnrichService enrichService,
                                       ZmwlStatisticsService statisticsService, ZmwlConfigService configService,
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
                                String studentNo, String directionCode, String statusFilter,
                                LocalDate startDate, LocalDate endDate) throws IOException {
        if (schoolId == null || instanceId == null) {
            throw new ServiceException("请先选择学校和活动");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        String status = normalizeStatus(statusFilter);
        String direction = blankToNull(directionCode);
        GkzhSchool school = schoolMapper.selectGkzhSchoolBySchoolId(schoolId);
        if (school == null) {
            throw new ServiceException("所选学校不存在");
        }
        GkzhActivityWeekInstance instance = activityWeekService.getInstance(instanceId);
        if (instance == null) {
            throw new ServiceException("所选活动不存在");
        }

        ZmwlRecordQuery query = new ZmwlRecordQuery();
        query.setSchoolId(schoolId);
        query.setInstanceId(instanceId);
        query.setDepartmentId(departmentId);
        query.setMajor(blankToNull(major));
        query.setGenderValue(genderValue(gender));
        query.setStudentNo(blankToNull(studentNo));
        query.setDirectionCode(direction);
        query.setStatus(status);
        query.setStartDate(startDate);
        query.setEndDate(endDate == null ? null : endDate.plusDays(1));

        List<ZmwlRecord> records = recordMapper.selectAdminRecords(query);
        if (records.isEmpty()) {
            throw new ServiceException("当前筛选条件下暂无可导出的参与数据");
        }
        List<Map<String, Object>> rows = enrichService.enrich(records);
        Map<String, Object> summary = statisticsService.summary(query);
        long finished = number(summary.get("finishedCount"));
        List<Map<String, Object>> directionStats = statisticsService.directionStats(query, finished);
        List<Map<String, Object>> departmentStats = statisticsService.departmentStats(query);

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle bodyStyle = bodyStyle(workbook);
        try {
            createSummarySheet(workbook, headerStyle, bodyStyle, summary, directionStats);
            createDirectionSheet(workbook, headerStyle, bodyStyle, directionStats);
            createDepartmentSheet(workbook, headerStyle, bodyStyle, departmentStats);
            createStudentSheet(workbook, headerStyle, bodyStyle, rows);
            createNoteSheet(workbook, headerStyle, bodyStyle, school, instance, departmentId, query, records.size());
            return workbook;
        } catch (RuntimeException e) {
            workbook.dispose();
            throw e;
        }
    }

    /** 第一页：参与、完成、方向偏好概况与前三方向。 */
    private void createSummarySheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                    Map<String, Object> summary, List<Map<String, Object>> directionStats) {
        Sheet sheet = workbook.createSheet("统计汇总");
        int rowIndex = 0;
        Row title = sheet.createRow(rowIndex++);
        title.createCell(0).setCellValue("职面未来 统计汇总");
        title.getCell(0).setCellStyle(titleStyle(workbook));

        rowIndex = writeKeyValueRows(sheet, rowIndex, headerStyle, bodyStyle, new Object[][]{
                {"参与人数", summary.get("participantCount")},
                {"已完成选择人数", summary.get("finishedCount")},
                {"进行中人数", summary.get("inProgressCount")},
                {"完成率", summary.get("finishRate") + "%"},
                {"暂未确定人数", summary.get("undecidedCount")},
                {"方向数量", summary.get("directionCount")},
                {"最受欢迎方向", summary.get("topDirectionName")},
                {"最受欢迎方向人数", summary.get("topDirectionCount")},
                {"最受欢迎方向占比", summary.get("topDirectionRate") + "%"},
                {"平均选择用时（秒）", summary.get("avgDurationSeconds")}
        });

        rowIndex++;
        Row topTitle = sheet.createRow(rowIndex++);
        topTitle.createCell(0).setCellValue("方向偏好前三");
        topTitle.getCell(0).setCellStyle(titleStyle(workbook));
        writeHeader(sheet.createRow(rowIndex++), new String[]{"排名", "发展方向", "标签称号", "人数", "占比"}, headerStyle);
        List<Map<String, Object>> top = statisticsService.topDirections(directionStats, 3);
        for (int i = 0; i < top.size(); i++) {
            Map<String, Object> row = top.get(i);
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    i + 1, text(row.get("name")), text(row.get("tagName")),
                    row.get("selectedCount"), text(row.get("rate")) + "%"
            }, bodyStyle);
        }
        int[] widths = {26, 20, 20, 12, 12};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }

    /** 第二页：8 个方向的人数与占比。 */
    private void createDirectionSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                      List<Map<String, Object>> directionStats) {
        Sheet sheet = workbook.createSheet("方向分布");
        writeHeader(sheet.createRow(0),
                new String[]{"排序", "方向编码", "发展方向", "标签称号", "简短介绍", "人数", "占比", "状态"}, headerStyle);
        int rowIndex = 1;
        for (Map<String, Object> row : directionStats) {
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    row.get("sortOrder"), text(row.get("directionCode")), text(row.get("name")),
                    text(row.get("tagName")), text(row.get("shortIntro")), row.get("selectedCount"),
                    text(row.get("rate")) + "%", "1".equals(text(row.get("status"))) ? "停用" : "启用"
            }, bodyStyle);
        }
        int[] widths = {8, 22, 16, 16, 40, 10, 10, 10};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        sheet.createFreezePane(0, 1);
    }

    /** 第三页：院系参与、完成情况与最受欢迎方向。 */
    private void createDepartmentSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                       List<Map<String, Object>> departmentStats) {
        Sheet sheet = workbook.createSheet("院系偏好");
        writeHeader(sheet.createRow(0),
                new String[]{"院系", "参与人数", "完成人数", "完成率", "最受欢迎方向", "该方向人数", "该方向占比"}, headerStyle);
        int rowIndex = 1;
        for (Map<String, Object> row : departmentStats) {
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    text(row.get("departmentName")), row.get("participantCount"), row.get("finishedCount"),
                    text(row.get("finishRate")) + "%", text(row.get("topDirectionName")),
                    row.get("topDirectionCount"), text(row.get("topDirectionRate")) + "%"
            }, bodyStyle);
        }
        int[] widths = {26, 12, 12, 10, 18, 14, 12};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        sheet.createFreezePane(0, 1);
    }

    /** 第四页：每条参与记录的基础信息与所选方向。 */
    private void createStudentSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                    List<Map<String, Object>> rows) {
        Sheet sheet = workbook.createSheet("学生明细");
        String[] headers = {"序号", "学校", "院系", "专业", "学号", "姓名", "性别", "活动", "参与状态",
                "发展方向", "标签称号", "选择用时(秒)", "参与时间", "确认时间"};
        writeHeader(sheet.createRow(0), headers, headerStyle);
        int rowIndex = 1;
        for (Map<String, Object> row : rows) {
            writeCells(sheet.createRow(rowIndex), new Object[]{
                    rowIndex,
                    text(row.get("schoolName")),
                    text(row.get("departmentName")),
                    text(row.get("major")),
                    text(row.get("studentNo")),
                    text(row.get("studentName")),
                    text(row.get("gender")),
                    text(row.get("instanceName")),
                    statusText(text(row.get("status"))),
                    text(row.get("directionName")),
                    text(row.get("tagName")),
                    seconds(row.get("durationMs")),
                    time(row.get("scanTime")),
                    time(row.get("finishTime"))
            }, bodyStyle);
            rowIndex++;
        }
        int[] widths = {8, 22, 20, 18, 16, 12, 8, 24, 12, 18, 16, 14, 20, 20};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        sheet.createFreezePane(0, 1);
    }

    /** 第五页：导出范围、筛选条件与统计口径。 */
    private void createNoteSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                 GkzhSchool school, GkzhActivityWeekInstance instance, Long departmentId,
                                 ZmwlRecordQuery query, int recordCount) {
        Sheet sheet = workbook.createSheet("数据说明");
        ZmwlSetting setting = configService.getSetting();
        String departmentName = departmentId == null ? "全部" : departmentName(departmentId);
        String dateRange = query.getStartDate() == null && query.getEndDate() == null ? "全部"
                : (query.getStartDate() == null ? "不限开始日期" : query.getStartDate().toString())
                + " 至 " + (query.getEndDate() == null ? "不限结束日期" : query.getEndDate().minusDays(1).toString());
        String directionName = "全部";
        if (query.getDirectionCode() != null) {
            directionName = query.getDirectionCode();
            for (ZmwlDirection direction : configService.listAll()) {
                if (query.getDirectionCode().equals(direction.getDirectionCode())) {
                    directionName = direction.getName();
                    break;
                }
            }
        }
        int rowIndex = writeKeyValueRows(sheet, 0, headerStyle, bodyStyle, new Object[][]{
                {"导出范围", school.getTitle() + " / " + instance.getTitle()},
                {"筛选条件-院系", departmentName},
                {"筛选条件-专业", query.getMajor() == null ? "全部" : query.getMajor()},
                {"筛选条件-性别", query.getGenderValue() == null ? "全部" : query.getGenderValue()},
                {"筛选条件-学号", query.getStudentNo() == null ? "全部" : query.getStudentNo()},
                {"筛选条件-发展方向", directionName},
                {"筛选条件-参与状态", query.getStatus() == null ? "全部"
                        : ("finished".equals(query.getStatus()) ? "已完成" : "进行中")},
                {"筛选条件-参与日期", dateRange},
                {"导出记录数", recordCount},
                {"方向偏好口径", "以已完成选择人数为分母，同一学生只计一次；“暂未确定”按独立方向统计"},
                {"重新选择", "1".equals(setting.getAllowReselect()) ? "允许（统计取最新一次选择）" : "不允许，确认后只读"},
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

    private static String statusText(String status) {
        return "finished".equals(status) ? "已完成" : "进行中";
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
        return gender == null ? null : ZmwlRecordEnrichService.genderText(gender);
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
