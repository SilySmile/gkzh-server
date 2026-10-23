package com.gkzh.app.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.zcyltzj.domain.ZcyltzjRecord;
import com.gkzh.zcyltzj.mapper.ZcyltzjRecordMapper;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 骰子局统计 Excel 导出。
 * 工作簿包含统计汇总、学生数据、答题明细和数据说明四个工作表，
 * 导出使用页面筛选条件但不带分页，便于后续直接处理数据。
 */
@Service
public class ZcyltzjStatisticsExcelService {

    private final ZcyltzjRecordMapper recordMapper;
    private final ZcyltzjRecordEnrichService enrichService;
    private final GkzhSchoolMapper schoolMapper;
    private final GkzhSchoolDepartmentMapper departmentMapper;
    private final IActivityWeekService activityWeekService;

    public ZcyltzjStatisticsExcelService(ZcyltzjRecordMapper recordMapper,
                                         ZcyltzjRecordEnrichService enrichService,
                                         GkzhSchoolMapper schoolMapper,
                                         GkzhSchoolDepartmentMapper departmentMapper,
                                         IActivityWeekService activityWeekService) {
        this.recordMapper = recordMapper;
        this.enrichService = enrichService;
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
                                String studentNo, Integer pointNo, String statusFilter,
                                LocalDate startDate, LocalDate endDate) throws IOException {
        if (schoolId == null || instanceId == null) {
            throw new ServiceException("请先选择学校和活动");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        if (pointNo != null && (pointNo < 1 || pointNo > 6)) {
            throw new ServiceException("骰子点数必须为 1 到 6");
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

        List<ZcyltzjRecord> records = recordMapper.selectAdminRecords(schoolId, instanceId, departmentId,
                blankToNull(major), genderValue(gender), blankToNull(studentNo), pointNo, status,
                startDate, endDate == null ? null : endDate.plusDays(1));
        if (records.isEmpty()) {
            throw new ServiceException("当前筛选条件下暂无可导出的参与数据");
        }
        List<Map<String, Object>> rows = enrichService.enrich(records);

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle bodyStyle = bodyStyle(workbook);
        try {
            createSummarySheet(workbook, headerStyle, bodyStyle, rows);
            createStudentSheet(workbook, headerStyle, bodyStyle, rows);
            createAnswerSheet(workbook, headerStyle, bodyStyle, records, rows);
            createNoteSheet(workbook, headerStyle, bodyStyle, school, instance, departmentId, major, gender,
                    studentNo, pointNo, status, startDate, endDate, records.size());
            return workbook;
        } catch (RuntimeException e) {
            workbook.dispose();
            throw e;
        }
    }

    /** 第一页：参与人数、完成率、颜色分布、平均压力值和各点数汇总。 */
    private void createSummarySheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                    List<Map<String, Object>> rows) {
        Sheet sheet = workbook.createSheet("统计汇总");
        int rowIndex = 0;
        Row title = sheet.createRow(rowIndex++);
        title.createCell(0).setCellValue("职场压力·骰子局 统计汇总");
        title.getCell(0).setCellStyle(titleStyle(workbook));

        long finished = rows.stream().filter(row -> "finished".equals(row.get("status"))).count();
        double avg = rows.stream().filter(row -> "finished".equals(row.get("status")))
                .map(row -> number(row.get("pressureValue")))
                .filter(value -> value != null)
                .mapToInt(Integer::intValue).average().orElse(0);
        long red = rows.stream().filter(row -> "finished".equals(row.get("status")) && "red".equals(row.get("color"))).count();
        long green = rows.stream().filter(row -> "finished".equals(row.get("status")) && "green".equals(row.get("color"))).count();
        long yellow = rows.stream().filter(row -> "finished".equals(row.get("status")) && "yellow".equals(row.get("color"))).count();

        rowIndex = writeKeyValueRows(sheet, rowIndex, headerStyle, bodyStyle,
                new Object[][]{
                        {"参与人数", rows.size()},
                        {"完成人数", finished},
                        {"完成率", rows.isEmpty() ? "0%" : String.format("%.1f%%", finished * 100.0 / rows.size())},
                        {"平均压力值", String.format("%.0f%%", avg)},
                        {"红色结果人数", red},
                        {"绿色结果人数", green},
                        {"黄色结果人数", yellow}
                });

        rowIndex++;
        Row pointTitle = sheet.createRow(rowIndex++);
        pointTitle.createCell(0).setCellValue("各点数汇总");
        pointTitle.getCell(0).setCellStyle(titleStyle(workbook));

        String[] headers = {"点数", "点数名称", "参与人数", "完成人数", "完成率", "平均压力值"};
        writeHeader(sheet.createRow(rowIndex++), headers, headerStyle);
        for (int pointNo = 1; pointNo <= 6; pointNo++) {
            final int no = pointNo;
            List<Map<String, Object>> pointRows = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                Integer value = number(row.get("pointNo"));
                if (value != null && value == no) {
                    pointRows.add(row);
                }
            }
            long pointFinished = pointRows.stream().filter(row -> "finished".equals(row.get("status"))).count();
            double pointAvg = pointRows.stream().filter(row -> "finished".equals(row.get("status")))
                    .map(row -> number(row.get("pressureValue")))
                    .filter(value -> value != null)
                    .mapToInt(Integer::intValue).average().orElse(0);
            String name = pointRows.isEmpty() ? "" : String.valueOf(pointRows.get(0).get("pointName"));
            Row row = sheet.createRow(rowIndex++);
            writeCells(row, new Object[]{
                    pointNo, name, pointRows.size(), pointFinished,
                    pointRows.isEmpty() ? "0%" : String.format("%.1f%%", pointFinished * 100.0 / pointRows.size()),
                    pointRows.isEmpty() ? "-" : String.format("%.0f%%", pointAvg)
            }, bodyStyle);
        }
        sheet.setColumnWidth(0, 12 * 256);
        sheet.setColumnWidth(1, 20 * 256);
        sheet.setColumnWidth(2, 12 * 256);
        sheet.setColumnWidth(3, 12 * 256);
        sheet.setColumnWidth(4, 12 * 256);
        sheet.setColumnWidth(5, 14 * 256);
    }

    /** 第二页：每条参与记录的基础信息与结果。 */
    private void createStudentSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                    List<Map<String, Object>> rows) {
        Sheet sheet = workbook.createSheet("学生数据");
        String[] headers = {"序号", "学校", "院系", "专业", "学号", "姓名", "性别", "活动", "点数", "点数名称",
                "状态", "压力值", "结果颜色", "红色答案数", "绿色答案数", "参与时间", "开始答题时间", "完成时间"};
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
                    row.get("pointNo"),
                    text(row.get("pointName")),
                    statusText(text(row.get("status"))),
                    row.get("pressureValue") == null ? "" : row.get("pressureValue") + "%",
                    colorText(text(row.get("color"))),
                    row.get("redCount"),
                    row.get("greenCount"),
                    time(row.get("scanTime")),
                    time(row.get("startTime")),
                    time(row.get("finishTime"))
            }, bodyStyle);
            rowIndex++;
        }
        int[] widths = {8, 22, 20, 18, 16, 12, 8, 24, 8, 16, 10, 10, 10, 12, 12, 20, 20, 20};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        sheet.createFreezePane(0, 1);
    }

    /** 第三页：逐题答案明细，便于直接进行数据处理。 */
    private void createAnswerSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                   List<ZcyltzjRecord> records, List<Map<String, Object>> rows) {
        Sheet sheet = workbook.createSheet("答题明细");
        String[] headers = {"学号", "姓名", "活动", "点数", "点数名称", "题号", "题干", "所选选项", "选项文字", "该题颜色"};
        writeHeader(sheet.createRow(0), headers, headerStyle);
        Map<Long, Map<String, Object>> rowMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            rowMap.put((Long) row.get("recordId"), row);
        }
        int rowIndex = 1;
        for (ZcyltzjRecord record : records) {
            JSONObject snapshot = parse(record.getQuestionSnapshotJson());
            if (snapshot == null) {
                continue;
            }
            JSONArray questions = snapshot.getJSONArray("questions");
            if (questions == null) {
                continue;
            }
            Map<String, Object> base = rowMap.get(record.getRecordId());
            Map<Integer, String> answers = parseAnswers(record.getAnswersJson());
            for (int i = 0; i < questions.size(); i++) {
                JSONObject question = questions.getJSONObject(i);
                Integer questionNo = question.getInteger("questionNo");
                String option = answers.get(questionNo);
                writeCells(sheet.createRow(rowIndex++), new Object[]{
                        base == null ? "" : text(base.get("studentNo")),
                        base == null ? "" : text(base.get("studentName")),
                        base == null ? "" : text(base.get("instanceName")),
                        record.getPointNo(),
                        text(record.getPointName()),
                        questionNo,
                        text(question.getString("content")),
                        option == null ? "" : option,
                        option == null ? "" : text(question.getString("option" + option)),
                        option == null ? "" : optionColor(option)
                }, bodyStyle);
            }
        }
        int[] widths = {16, 12, 24, 8, 16, 8, 40, 10, 28, 10};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        sheet.createFreezePane(0, 1);
    }

    /** 第四页：导出范围、筛选条件和计分口径。 */
    private void createNoteSheet(Workbook workbook, CellStyle headerStyle, CellStyle bodyStyle,
                                 GkzhSchool school, GkzhActivityWeekInstance instance, Long departmentId,
                                 String major, String gender, String studentNo, Integer pointNo, String status,
                                 LocalDate startDate, LocalDate endDate, int recordCount) {
        Sheet sheet = workbook.createSheet("数据说明");
        String departmentName = departmentId == null ? "全部" : departmentName(departmentId);
        String dateRange = startDate == null && endDate == null ? "全部"
                : (startDate == null ? "不限开始日期" : startDate.toString())
                + " 至 " + (endDate == null ? "不限结束日期" : endDate.toString());
        int rowIndex = writeKeyValueRows(sheet, 0, headerStyle, bodyStyle, new Object[][]{
                {"导出范围", school.getTitle() + " / " + instance.getTitle()},
                {"筛选条件-院系", departmentName},
                {"筛选条件-专业", blankToNull(major) == null ? "全部" : major},
                {"筛选条件-性别", blankToNull(gender) == null ? "全部" : gender},
                {"筛选条件-学号", blankToNull(studentNo) == null ? "全部" : studentNo},
                {"筛选条件-点数", pointNo == null ? "全部" : pointNo + " 点"},
                {"筛选条件-状态", status == null ? "全部" : ("finished".equals(status) ? "已完成" : "进行中")},
                {"筛选条件-参与日期", dateRange},
                {"导出记录数", recordCount},
                {"计分口径", "A、B 选项计入红色，C、D 选项计入绿色；压力值 = 红色答案数 ÷ 总题数 × 100，"
                        + "大于 50 显示红色、小于 50 显示绿色、等于 50 显示黄色。"},
                {"导出时间", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}
        });
        rowIndex++;
        Row title = sheet.createRow(rowIndex);
        title.createCell(0).setCellValue("说明：本表仅用于活动复盘和数据分析，不包含登录凭证等敏感信息。");
        title.getCell(0).setCellStyle(bodyStyle);
        sheet.setColumnWidth(0, 24 * 256);
        sheet.setColumnWidth(1, 80 * 256);
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

    private static JSONObject parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return JSON.parseObject(value);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Map<Integer, String> parseAnswers(String value) {
        Map<Integer, String> result = new LinkedHashMap<>();
        if (value == null || value.trim().isEmpty()) {
            return result;
        }
        try {
            JSONArray array = JSON.parseArray(value);
            for (int i = 0; i < array.size(); i++) {
                JSONObject item = array.getJSONObject(i);
                Integer no = item.getInteger("questionNo");
                if (no != null) {
                    result.put(no, item.getString("option"));
                }
            }
        } catch (RuntimeException e) {
            // 历史答案格式异常时返回空集合，导出不中断。
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
        return gender == null ? null : ZcyltzjRecordEnrichService.genderText(gender);
    }

    private static String statusText(String value) {
        return "finished".equals(value) ? "已完成" : "进行中";
    }

    private static String colorText(String value) {
        if ("red".equals(value)) {
            return "红色";
        }
        if ("green".equals(value)) {
            return "绿色";
        }
        if ("yellow".equals(value)) {
            return "黄色";
        }
        return "-";
    }

    private static String optionColor(String option) {
        if ("A".equalsIgnoreCase(option) || "B".equalsIgnoreCase(option)) {
            return "红色";
        }
        if ("C".equalsIgnoreCase(option) || "D".equalsIgnoreCase(option)) {
            return "绿色";
        }
        return "-";
    }

    private static Integer number(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
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
