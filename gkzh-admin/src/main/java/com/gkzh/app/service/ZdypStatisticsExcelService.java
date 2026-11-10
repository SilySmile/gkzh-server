package com.gkzh.app.service;

import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.zdyp.domain.ZdypCareer;
import com.gkzh.zdyp.domain.ZdypRecord;
import com.gkzh.zdyp.domain.ZdypSetting;
import com.gkzh.zdyp.domain.dto.ZdypRecordQuery;
import com.gkzh.zdyp.mapper.ZdypRecordMapper;
import com.gkzh.zdyp.service.ZdypBehavior;
import com.gkzh.zdyp.service.ZdypConfigService;
import com.gkzh.zdyp.service.ZdypStatisticsService;
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
 * 职得一拼统计 Excel 导出。
 * 工作簿包含统计汇总、行为分型、维度错误、岗位分布、学生明细和数据说明六个工作表，
 * 导出使用页面筛选条件但不带分页，便于后续直接处理数据。
 */
@Service
public class ZdypStatisticsExcelService {

    private final ZdypRecordMapper recordMapper;
    private final ZdypRecordEnrichService enrichService;
    private final ZdypStatisticsService statisticsService;
    private final ZdypConfigService configService;
    private final GkzhSchoolMapper schoolMapper;
    private final GkzhSchoolDepartmentMapper departmentMapper;
    private final IActivityWeekService activityWeekService;

    public ZdypStatisticsExcelService(ZdypRecordMapper recordMapper, ZdypRecordEnrichService enrichService,
                                      ZdypStatisticsService statisticsService, ZdypConfigService configService,
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
                                String studentNo, String careerCode, String behaviorType, String statusFilter,
                                LocalDate startDate, LocalDate endDate) throws IOException {
        if (schoolId == null || instanceId == null) {
            throw new ServiceException("请先选择学校和活动");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        String status = normalizeStatus(statusFilter);
        String behavior = normalizeBehavior(behaviorType);
        GkzhSchool school = schoolMapper.selectGkzhSchoolBySchoolId(schoolId);
        if (school == null) {
            throw new ServiceException("所选学校不存在");
        }
        GkzhActivityWeekInstance instance = activityWeekService.getInstance(instanceId);
        if (instance == null) {
            throw new ServiceException("所选活动不存在");
        }

        ZdypRecordQuery query = new ZdypRecordQuery();
        query.setSchoolId(schoolId);
        query.setInstanceId(instanceId);
        query.setDepartmentId(departmentId);
        query.setMajor(blankToNull(major));
        query.setGenderValue(genderValue(gender));
        query.setStudentNo(blankToNull(studentNo));
        query.setCareerCode(blankToNull(careerCode));
        query.setBehaviorType(behavior);
        query.setStatus(status);
        query.setStartDate(startDate);
        query.setEndDate(endDate == null ? null : endDate.plusDays(1));

        List<ZdypRecord> records = recordMapper.selectAdminRecords(query);
        if (records.isEmpty()) {
            throw new ServiceException("当前筛选条件下暂无可导出的参与数据");
        }
        List<Map<String, Object>> rows = enrichService.enrich(records);
        Map<String, Object> summary = statisticsService.summary(query);
        long finished = number(summary.get("finishedCount"));
        long started = number(summary.get("startedCount"));
        List<Map<String, Object>> behaviorStats = statisticsService.behaviorStats(query, finished);
        List<Map<String, Object>> slotStats = statisticsService.slotStats(query, started);
        List<Map<String, Object>> careerStats = statisticsService.careerStats(query);

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle bodyStyle = bodyStyle(workbook);
        try {
            createSummarySheet(workbook, headerStyle, bodyStyle, summary, behaviorStats);
            createBehaviorSheet(workbook, headerStyle, bodyStyle, behaviorStats);
            createSlotSheet(workbook, headerStyle, bodyStyle, slotStats);
            createCareerSheet(workbook, headerStyle, bodyStyle, careerStats);
            createStudentSheet(workbook, headerStyle, bodyStyle, rows);
            createNoteSheet(workbook, headerStyle, bodyStyle, school, instance, departmentId, query, records.size());
            return workbook;
        } catch (RuntimeException e) {
            workbook.dispose();
            throw e;
        }
    }

    /** 第一页：参与、通关、首次通关、重闯、超时与用时概况。 */
    private void createSummarySheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                    Map<String, Object> summary, List<Map<String, Object>> behaviorStats) {
        Sheet sheet = workbook.createSheet("统计汇总");
        int rowIndex = 0;
        Row title = sheet.createRow(rowIndex++);
        title.createCell(0).setCellValue("职得一拼 统计汇总");
        title.getCell(0).setCellStyle(titleStyle(workbook));

        rowIndex = writeKeyValueRows(sheet, rowIndex, headerStyle, bodyStyle, new Object[][]{
                {"参与人数", summary.get("participantCount")},
                {"已开始闯关人数", summary.get("startedCount")},
                {"通关人数", summary.get("finishedCount")},
                {"进行中人数", summary.get("inProgressCount")},
                {"通关率", summary.get("finishRate") + "%"},
                {"首次即通关人数", summary.get("firstPassCount")},
                {"首次通关率", summary.get("firstPassRate") + "%"},
                {"累计重闯次数", summary.get("reattemptTotal")},
                {"平均闯关次数", summary.get("avgAttempts")},
                {"超时人数", summary.get("timeoutPlayers")},
                {"超时人数占比", summary.get("timeoutRate") + "%"},
                {"超时尝试次数", summary.get("timeoutCount")},
                {"平均首次用时（秒）", summary.get("avgFirstSeconds")},
                {"平均最快通关用时（秒）", summary.get("avgBestSeconds")},
                {"最短通关用时（秒）", summary.get("minBestSeconds")},
                {"最长通关用时（秒）", summary.get("maxBestSeconds")}
        });

        rowIndex++;
        Row topTitle = sheet.createRow(rowIndex++);
        topTitle.createCell(0).setCellValue("行为分型分布");
        topTitle.getCell(0).setCellStyle(titleStyle(workbook));
        writeHeader(sheet.createRow(rowIndex++), new String[]{"行为分型", "人数", "占比"}, headerStyle);
        for (Map<String, Object> row : behaviorStats) {
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    text(row.get("behaviorName")), row.get("playerCount"), text(row.get("rate")) + "%"
            }, bodyStyle);
        }
        int[] widths = {28, 16, 12, 12};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }

    /** 第二页：四类行为分型的人数、占比与对应结论建议。 */
    private void createBehaviorSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                     List<Map<String, Object>> behaviorStats) {
        Sheet sheet = workbook.createSheet("行为分型");
        writeHeader(sheet.createRow(0),
                new String[]{"行为分型编码", "行为分型", "人数", "占比", "个人结论", "提升建议"}, headerStyle);
        int rowIndex = 1;
        for (Map<String, Object> row : behaviorStats) {
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    text(row.get("behaviorType")), text(row.get("behaviorName")), row.get("playerCount"),
                    text(row.get("rate")) + "%", text(row.get("conclusion")), text(row.get("suggestion"))
            }, bodyStyle);
        }
        int[] widths = {16, 16, 10, 10, 70, 50};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        sheet.createFreezePane(0, 1);
    }

    /** 第三页：首次尝试各卡槽的放对、放错与未填写数量。 */
    private void createSlotSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                 List<Map<String, Object>> slotStats) {
        Sheet sheet = workbook.createSheet("维度错误");
        writeHeader(sheet.createRow(0),
                new String[]{"卡槽编码", "卡槽名称", "首次放置人数", "放对次数", "放错次数", "首次正确率", "未填写人数"},
                headerStyle);
        int rowIndex = 1;
        for (Map<String, Object> row : slotStats) {
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    text(row.get("slotCode")), text(row.get("slotName")), row.get("placedCount"),
                    row.get("correctCount"), row.get("wrongCount"), text(row.get("correctRate")) + "%",
                    row.get("unfilledCount")
            }, bodyStyle);
        }
        int[] widths = {16, 16, 16, 12, 12, 14, 14};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        sheet.createFreezePane(0, 1);
    }

    /** 第四页：10 套岗位的选择人数与通关情况。 */
    private void createCareerSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                   List<Map<String, Object>> careerStats) {
        Sheet sheet = workbook.createSheet("岗位分布");
        writeHeader(sheet.createRow(0),
                new String[]{"岗位编码", "岗位名称", "选择人数", "通关人数", "通关率", "状态"}, headerStyle);
        int rowIndex = 1;
        for (Map<String, Object> row : careerStats) {
            writeCells(sheet.createRow(rowIndex++), new Object[]{
                    text(row.get("careerCode")), text(row.get("careerName")), row.get("selectedCount"),
                    row.get("finishedCount"), text(row.get("finishRate")) + "%",
                    "1".equals(text(row.get("status"))) ? "停用" : "启用"
            }, bodyStyle);
        }
        int[] widths = {30, 26, 12, 12, 12, 10};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        sheet.createFreezePane(0, 1);
    }

    /** 第五页：每条参与记录的基础信息、岗位与行为分型。 */
    private void createStudentSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                    List<Map<String, Object>> rows) {
        Sheet sheet = workbook.createSheet("学生明细");
        String[] headers = {"序号", "学校", "院系", "专业", "学号", "姓名", "性别", "活动", "岗位",
                "参与状态", "闯关次数", "首次即通关", "累计错误", "超时次数", "行为分型",
                "首次用时(秒)", "最快通关用时(秒)", "参与时间", "通关时间"};
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
                    text(row.get("careerName")),
                    statusText(text(row.get("status"))),
                    row.get("attemptNo"),
                    "1".equals(text(row.get("firstAttemptCorrect"))) ? "是" : "否",
                    row.get("totalWrong"),
                    row.get("timeoutCount"),
                    text(row.get("behaviorName")),
                    seconds(row.get("firstAttemptMs")),
                    seconds(row.get("bestMs")),
                    time(row.get("scanTime")),
                    time(row.get("finishTime"))
            }, bodyStyle);
            rowIndex++;
        }
        int[] widths = {8, 22, 20, 18, 16, 12, 8, 24, 24, 12, 10, 12, 10, 10, 14, 14, 16, 20, 20};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        sheet.createFreezePane(0, 1);
    }

    /** 第六页：导出范围、筛选条件与统计口径。 */
    private void createNoteSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                 GkzhSchool school, GkzhActivityWeekInstance instance, Long departmentId,
                                 ZdypRecordQuery query, int recordCount) {
        Sheet sheet = workbook.createSheet("数据说明");
        ZdypSetting setting = configService.getSetting();
        String departmentName = departmentId == null ? "全部" : departmentName(departmentId);
        String dateRange = query.getStartDate() == null && query.getEndDate() == null ? "全部"
                : (query.getStartDate() == null ? "不限开始日期" : query.getStartDate().toString())
                + " 至 " + (query.getEndDate() == null ? "不限结束日期" : query.getEndDate().minusDays(1).toString());
        String careerName = "全部";
        if (query.getCareerCode() != null) {
            careerName = query.getCareerCode();
            for (ZdypCareer career : configService.listAllCareers()) {
                if (query.getCareerCode().equals(career.getCareerCode())) {
                    careerName = career.getCareerName();
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
                {"筛选条件-岗位", careerName},
                {"筛选条件-行为分型", query.getBehaviorType() == null ? "全部" : ZdypBehavior.name(query.getBehaviorType())},
                {"筛选条件-参与状态", query.getStatus() == null ? "全部"
                        : ("finished".equals(query.getStatus()) ? "已通关" : "进行中")},
                {"筛选条件-参与日期", dateRange},
                {"导出记录数", recordCount},
                {"限时（秒）", setting.getTimeLimit() != null && setting.getTimeLimit() > 0
                        ? setting.getTimeLimit() : "不限时"},
                {"统计口径", "维度错误、首填偏好与行为分型只统计首次尝试；行为分型按首次尝试的用时与错误分布判定"},
                {"行为分型规则", "零错误=全面均衡型；职业缺点错且最集中=优势偏好型；"
                        + "用时不超过20秒且错2个以上=急躁粗放型；其余=信息薄弱型"},
                {"导出时间", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}
        });
        Row note = sheet.createRow(rowIndex + 1);
        note.createCell(0).setCellValue("说明：本表仅用于活动复盘和数据分析，不包含登录凭证等敏感信息。");
        note.getCell(0).setCellStyle(bodyStyle);
        sheet.setColumnWidth(0, 26 * 256);
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
        return "finished".equals(status) ? "已通关" : "进行中";
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

    private static String normalizeBehavior(String value) {
        String behavior = blankToNull(value);
        if (behavior == null) {
            return null;
        }
        if (!ZdypBehavior.BALANCED.equals(behavior) && !ZdypBehavior.ADVANTAGE.equals(behavior)
                && !ZdypBehavior.WEAK.equals(behavior) && !ZdypBehavior.RUSHED.equals(behavior)) {
            throw new ServiceException("行为分型筛选值无效");
        }
        return behavior;
    }

    private static String genderValue(String value) {
        String gender = blankToNull(value);
        return gender == null ? null : ZdypRecordEnrichService.genderText(gender);
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
