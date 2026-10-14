package com.gkzh.app.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.PageHelper;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.zycck.domain.ZycckCareerQuestion;
import com.gkzh.zycck.domain.ZycckCategory;
import com.gkzh.zycck.dto.ZycckFeedbackQuestionView;
import com.gkzh.zycck.dto.ZycckStatisticsExportRow;
import com.gkzh.zycck.mapper.ZycckCareerQuestionMapper;
import com.gkzh.zycck.mapper.ZycckCategoryMapper;
import com.gkzh.zycck.mapper.ZycckStatisticsExportMapper;
import com.gkzh.zycck.service.ZycckActivityFeedbackService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/** 学校活动全量 Excel 导出：按主键分批读取，流式工作簿避免把全部明细驻留内存。 */
@Service
public class ZycckStatisticsExcelService {
    /** 每批查询学生参与记录数量，与页面分页无关。 */
    private static final int BATCH_SIZE = 500;
    /** 查询学生与反馈的关联数据。 */
    private final ZycckStatisticsExportMapper exports;
    /** 职业及题库资料。 */
    private final ZycckCareerQuestionMapper careers;
    /** 职业大类资料。 */
    private final ZycckCategoryMapper categories;
    /** 学校存在性校验。 */
    private final GkzhSchoolMapper schools;
    /** 活动存在性校验。 */
    private final IActivityWeekService activities;
    /** 问卷中文定义的统一来源。 */
    private final ZycckActivityFeedbackService feedbackService;

    public ZycckStatisticsExcelService(ZycckStatisticsExportMapper exports, ZycckCareerQuestionMapper careers,
            ZycckCategoryMapper categories, GkzhSchoolMapper schools, IActivityWeekService activities,
            ZycckActivityFeedbackService feedbackService) {
        this.exports = exports; this.careers = careers; this.categories = categories;
        this.schools = schools; this.activities = activities; this.feedbackService = feedbackService;
    }

    /** 必选学校和活动；同一次只读事务内导出全部游戏记录，不受院系等列表筛选及页码影响。 */
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public SXSSFWorkbook create(Long schoolId, Long instanceId) throws IOException {
        if (schoolId == null || instanceId == null || schoolId <= 0 || instanceId <= 0)
            throw new ServiceException("请先选择学校和活动，再下载Excel");
        PageHelper.clearPage();
        if (schools.selectGkzhSchoolBySchoolId(schoolId) == null) throw new ServiceException("所选学校不存在，请重新选择");
        if (activities.getInstance(instanceId) == null) throw new ServiceException("所选活动不存在，请重新选择");
        List<ZycckStatisticsExportRow> batch = exports.selectBatch(schoolId, instanceId, 0L, BATCH_SIZE);
        if (batch.isEmpty()) throw new ServiceException("所选学校和活动暂无职业猜猜看参与数据");
        Map<Integer, ZycckFeedbackQuestionView> definitions = feedbackService.exportQuestionDefinitions().stream()
                .collect(Collectors.toMap(ZycckFeedbackQuestionView::getQuestionNo, value -> value));
        Map<Long, ZycckCategory> categoryMap = categories.selectList(new QueryWrapper<>()).stream()
                .collect(Collectors.toMap(ZycckCategory::getCategoryId, value -> value));
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        boolean complete = false;
        try {
            Tables tables = new Tables(workbook);
            List<String> studentHeaders = new ArrayList<>(Arrays.asList("记录编号", "学校", "活动", "游戏", "学生姓名", "学号",
                    "学生编号", "用户编号", "学校编号", "活动编号", "游戏编号", "院系/专业部门", "部门编号", "专业", "性别",
                    "参与状态", "流程阶段", "当前题号", "扫码时间", "开始时间", "结束时间", "已记录答题数", "非超时作答数", "超时数"));
            for (int no = 1; no <= 5; no++) for (String field : Arrays.asList("场景职业", "学生选项", "学生选择职业", "正确选项",
                    "正确职业", "作答结果", "作答秒数", "职业了解程度")) studentHeaders.add("第" + no + "题" + field);
            studentHeaders.addAll(Arrays.asList("浏览职业数", "浏览职业", "探索清单数", "探索清单职业", "曾加入清单",
                    "反馈提交状态", "反馈版本", "反馈第2题适用", "反馈第1题选项编码", "反馈第1题认识", "反馈第1题其他",
                    "反馈第2题选项编码", "反馈第2题加入原因", "反馈第2题其他", "反馈第3题选项编码", "反馈第3题后续需求", "反馈第3题其他", "反馈提交时间"));
            Table students = tables.table("学生数据", studentHeaders);
            Table answers = tables.table("答题明细", Arrays.asList("记录编号", "学校", "活动", "游戏", "学生姓名", "学号", "用户编号",
                    "题号", "题目编号", "职业大类编号", "职业大类", "场景职业", "场景图片", "A选项", "B选项", "C选项", "D选项",
                    "学生选项", "学生选择职业", "正确选项", "正确职业", "作答结果", "作答秒数", "了解程度编码", "职业了解程度", "正确答案来源"));
            Table careerRows = tables.table("职业明细", Arrays.asList("记录编号", "学校", "活动", "游戏", "学生姓名", "学号", "用户编号",
                    "职业编号", "职业名称", "已经浏览", "在探索清单", "职业大类编号", "职业大类", "一句话介绍", "主要工作内容", "一天工作示例",
                    "职业存在原因", "职业图片", "职业资料状态"));
            Table raw = tables.table("原始记录", Arrays.asList("记录编号", "学校编号", "活动编号", "游戏编号", "用户编号", "原学生编号",
                    "原部门编号", "原专业", "原性别", "游戏类型", "原状态", "原阶段", "当前题号", "题目编号列表", "题目顺序", "关联职业编号",
                    "关联大类编号", "题目选项快照JSON", "答案JSON", "了解程度JSON", "浏览职业编号", "探索清单职业编号", "曾加入标记",
                    "配置版本", "当前题目开始时间", "当前题目累计秒数", "扫码时间", "开始时间", "原结束时间", "创建时间", "更新时间",
                    "反馈记录编号", "反馈版本", "反馈第1题编码JSON", "反馈第1题其他", "反馈第2题适用标记", "反馈第2题编码", "反馈第2题其他",
                    "反馈第3题编码JSON", "反馈第3题其他", "反馈提交时间"));
            Table notes = tables.table("数据说明", Arrays.asList("说明项", "内容"));
            notes.write("导出范围", "仅按学校编号 " + schoolId + " 与活动编号 " + instanceId + " 筛选；包含进行中和已结束记录，不使用页面页码、院系、专业、性别、学号筛选。");
            notes.write("学生数据粒度", "每个学生在每个游戏的一份参与记录对应一行；同一活动存在多个猜猜看游戏时分别保留，不覆盖答案。");
            notes.write("完成状态", "反馈已提交且参与记录已结束才为已完成；旧已结束但未反馈的记录显示待活动反馈，原状态和原结束时间保留在原始记录。");
            notes.write("题目与正确答案", "场景及选项来自学生本局快照；旧快照未保存正确答案时从当前题库读取并注明来源。题库删除且快照无正确答案时标为无法判定，不计为错误。");
            notes.write("未作答与超时", "未作答、超时、正确、错误、无法判定分别保留；尚未开始抽题的学生也保留一行学生数据。");
            notes.write("职业资料", "浏览与探索清单按编号去重；已删除职业保留编号及提示，职业详情来自导出时的资料。");
            notes.write("原始记录与长文本", "原始记录保留全部游戏记录及反馈字段；超过Excel单元格容量的内容拆到长文本工作表，原单元格提供索引，不截断内容。");
            notes.write("导出时间", new Date());
            for (ZycckFeedbackQuestionView question : definitions.values()) {
                notes.write("反馈第" + question.getQuestionNo() + "题", question.getTitle());
                for (ZycckFeedbackQuestionView.Option option : question.getOptions()) notes.write(option.getCode(), option.getLabel());
            }
            long afterId = 0;
            while (!batch.isEmpty()) {
                // 每批只查询本批所需职业一次，避免每个学生、每道题重复查数据库。
                Set<Long> careerIds = new HashSet<>();
                for (ZycckStatisticsExportRow record : batch) {
                    careerIds.addAll(ids(record.getViewedCareerIds())); careerIds.addAll(ids(record.getExplorationCareerIds()));
                    for (JSONObject snapshot : objects(record.getOptionSnapshotJson())) if (snapshot.getLong("questionId") != null) careerIds.add(snapshot.getLong("questionId"));
                    for (JSONObject answer : objects(record.getAnswerJson())) if (answer.getLong("questionId") != null) careerIds.add(answer.getLong("questionId"));
                }
                Map<Long, ZycckCareerQuestion> careerMap = careerIds.isEmpty() ? Collections.emptyMap()
                        : careers.selectBatchIds(careerIds).stream().collect(Collectors.toMap(ZycckCareerQuestion::getCareerQuestionId, value -> value));
                for (ZycckStatisticsExportRow record : batch) {
                    exportRecord(record, definitions, categoryMap, careerMap, students, answers, careerRows, raw);
                    afterId = record.getRecordId();
                }
                if (batch.size() < BATCH_SIZE) break;
                batch = exports.selectBatch(schoolId, instanceId, afterId, BATCH_SIZE);
            }
            tables.finish(); complete = true; return workbook;
        } finally {
            if (!complete) { try { workbook.close(); } finally { workbook.dispose(); } }
        }
    }

    /** 按学生记录展开全部业务数据，未提交学生不丢失、条件题不冒充未回答。 */
    private void exportRecord(ZycckStatisticsExportRow r, Map<Integer, ZycckFeedbackQuestionView> definitions,
            Map<Long, ZycckCategory> categoryMap, Map<Long, ZycckCareerQuestion> careerMap,
            Table students, Table answers, Table careerRows, Table raw) {
        Map<Integer, JSONObject> snapshots = indexed(r.getOptionSnapshotJson()), selected = indexed(r.getAnswerJson()), aware = indexed(r.getAwarenessJson());
        int timeoutCount = (int) selected.values().stream().filter(value -> value.getBooleanValue("timeout")).count();
        int answeredCount = (int) selected.values().stream().filter(value -> !value.getBooleanValue("timeout") && value.getString("optionKey") != null).count();
        boolean submitted = r.getFeedbackRecordId() != null;
        boolean showSecond = submitted ? "1".equals(r.getFeedbackShowQuestion2()) : "1".equals(r.getExplorationAdded()) || !ids(r.getExplorationCareerIds()).isEmpty();
        List<Object> values = identity(r);
        values.addAll(Arrays.asList(id(r.getEffectiveStudentId()), id(r.getUserId()), id(r.getSchoolId()), id(r.getInstanceId()), id(r.getGameId()),
                r.getDepartmentName(), id(r.getEffectiveDepartmentId()), first(r.getMajor(), r.getDepartmentName()), gender(first(r.getGender(), r.getStudentGender())),
                submitted && "finished".equals(r.getStatus()) ? "已完成" : "finished".equals(r.getStatus()) || "activity_feedback".equals(r.getStage()) ? "待活动反馈" : "进行中",
                stage(r.getStage()), r.getCurrentQuestionNo(), r.getScanTime(), r.getStartTime(), submitted ? r.getFinishTime() : null,
                selected.size(), answeredCount, timeoutCount));
        Set<Integer> questionNos = new TreeSet<>(snapshots.keySet()); questionNos.addAll(selected.keySet()); questionNos.addAll(aware.keySet());
        Map<Integer, List<Object>> questionValues = new HashMap<>();
        for (Integer no : questionNos) {
            JSONObject snapshot = snapshots.getOrDefault(no, new JSONObject()), answer = selected.get(no), awareness = aware.get(no);
            Long questionId = snapshot.getLong("questionId"); if (questionId == null && answer != null) questionId = answer.getLong("questionId");
            ZycckCareerQuestion question = careerMap.get(questionId);
            String correctKey = first(snapshot.getString("correctOptionKey"), question == null ? null : question.getCorrectOptionKey());
            String key = answer == null ? null : answer.getString("optionKey");
            String level = awareness == null ? null : first(awareness.getString("level"), awareness.getString("awareness"));
            String result = answer == null ? "未作答" : answer.getBooleanValue("timeout") ? "超时" : key == null ? "未作答"
                    : correctKey == null ? "无法判定" : key.equalsIgnoreCase(correctKey) ? "正确" : "错误";
            List<Object> detail = new ArrayList<>(Arrays.asList(snapshot.getString("careerName"), key, option(snapshot, key), correctKey,
                    option(snapshot, correctKey), result, answer == null ? null : answer.getInteger("elapsedSeconds"), awareness(level)));
            questionValues.put(no, detail);
            List<Object> row = identity(r); row.add(id(r.getUserId()));
            Long categoryId = snapshot.getLong("categoryId");
            row.addAll(Arrays.asList(no, id(questionId), id(categoryId), categoryName(categoryMap, categoryId), snapshot.getString("careerName"),
                    snapshot.getString("questionImageUrl"), snapshot.getString("optionA"), snapshot.getString("optionB"), snapshot.getString("optionC"), snapshot.getString("optionD"),
                    key, option(snapshot, key), correctKey, option(snapshot, correctKey), result,
                    answer == null ? null : answer.getInteger("elapsedSeconds"), level, awareness(level),
                    snapshot.getString("correctOptionKey") != null ? "本局快照" : question != null ? "当前题库" : "题库已删除或未保存"));
            answers.write(row.toArray());
        }
        for (int no = 1; no <= 5; no++) values.addAll(questionValues.getOrDefault(no, Arrays.asList(null, null, null, null, null, "未抽题", null, null)));
        Set<Long> viewed = ids(r.getViewedCareerIds()), explored = ids(r.getExplorationCareerIds());
        values.addAll(Arrays.asList(viewed.size(), careerNames(viewed, careerMap), explored.size(), careerNames(explored, careerMap),
                "1".equals(r.getExplorationAdded()) || !explored.isEmpty() ? "是" : "否", submitted ? "已提交" : "未提交", r.getFeedbackVersion(), showSecond ? "适用" : "不适用",
                codes(r.getFeedbackQuestion1Answers()), feedbackText(definitions.get(1), r.getFeedbackQuestion1Answers(), submitted), r.getFeedbackQuestion1Other(),
                r.getFeedbackQuestion2Answer(), !showSecond ? "不适用" : feedbackText(definitions.get(2), r.getFeedbackQuestion2Answer(), submitted), r.getFeedbackQuestion2Other(),
                codes(r.getFeedbackQuestion3Answers()), feedbackText(definitions.get(3), r.getFeedbackQuestion3Answers(), submitted), r.getFeedbackQuestion3Other(), r.getFeedbackSubmittedTime()));
        students.write(values.toArray());
        Set<Long> allCareers = new LinkedHashSet<>(viewed); allCareers.addAll(explored);
        for (Long careerId : allCareers) {
            ZycckCareerQuestion career = careerMap.get(careerId);
            List<Object> row = identity(r); row.add(id(r.getUserId()));
            row.addAll(Arrays.asList(id(careerId), careerName(careerId, career), viewed.contains(careerId) ? "是" : "否", explored.contains(careerId) ? "是" : "否",
                    career == null ? null : id(career.getCategoryId()), career == null ? null : categoryName(categoryMap, career.getCategoryId()),
                    career == null ? null : career.getOneLineIntro(), career == null ? null : career.getMainWork(), career == null ? null : career.getDayExample(),
                    career == null ? null : career.getWhyExists(), career == null ? null : career.getCareerImageUrl(), career == null ? "已删除" : "0".equals(career.getStatus()) ? "正常" : "停用"));
            careerRows.write(row.toArray());
        }
        raw.write(id(r.getRecordId()), id(r.getSchoolId()), id(r.getInstanceId()), id(r.getGameId()), id(r.getUserId()), id(r.getStudentId()),
                id(r.getDepartmentId()), r.getMajor(), r.getGender(), r.getGameType(), r.getStatus(), r.getStage(), r.getCurrentQuestionNo(), r.getQuestionIds(),
                r.getQuestionOrder(), r.getCareerIds(), r.getCategoryIds(), r.getOptionSnapshotJson(), r.getAnswerJson(), r.getAwarenessJson(), r.getViewedCareerIds(),
                r.getExplorationCareerIds(), r.getExplorationAdded(), r.getConfigVersion(), r.getQuestionStartTime(), r.getQuestionElapsedSeconds(), r.getScanTime(),
                r.getStartTime(), r.getFinishTime(), r.getCreateTime(), r.getUpdateTime(), id(r.getFeedbackRecordId()), r.getFeedbackVersion(),
                r.getFeedbackQuestion1Answers(), r.getFeedbackQuestion1Other(), r.getFeedbackShowQuestion2(), r.getFeedbackQuestion2Answer(), r.getFeedbackQuestion2Other(),
                r.getFeedbackQuestion3Answers(), r.getFeedbackQuestion3Other(), r.getFeedbackSubmittedTime());
    }

    private static List<Object> identity(ZycckStatisticsExportRow r) { return new ArrayList<>(Arrays.asList(id(r.getRecordId()), r.getSchoolName(), r.getInstanceName(), r.getGameName(), r.getStudentName(), r.getStudentNo())); }
    private static String id(Long value) { return value == null ? "" : value.toString(); }
    private static String first(String value, String fallback) { return value == null || value.trim().isEmpty() ? fallback : value; }
    private static String gender(String value) { return "0".equals(value) || "男".equals(value) ? "男" : "1".equals(value) || "女".equals(value) ? "女" : "其他"; }
    private static String awareness(String value) { return "A".equals(value) ? "以前就知道" : "B".equals(value) ? "听说过但不了解" : "C".equals(value) ? "第一次知道" : value; }
    private static String stage(String value) { Map<String, String> names = Map.of("scanned", "已扫码", "question", "竞猜中", "feedback", "答题反馈", "summary", "探索中", "exploration", "探索清单", "activity_feedback", "待活动反馈", "finished", "已结束"); return names.getOrDefault(value == null ? "" : value, value); }
    private static String option(JSONObject snapshot, String key) { return key == null ? null : snapshot.getString("option" + key.toUpperCase(Locale.ROOT)); }
    private static List<JSONObject> objects(String value) { try { List<JSONObject> rows = value == null ? null : JSON.parseArray(value, JSONObject.class); return rows == null ? Collections.emptyList() : rows.stream().filter(Objects::nonNull).collect(Collectors.toList()); } catch (RuntimeException e) { return Collections.emptyList(); } }
    private static Map<Integer, JSONObject> indexed(String value) { Map<Integer, JSONObject> values = new TreeMap<>(); for (JSONObject item : objects(value)) if (item != null && item.getInteger("questionNo") != null) values.put(item.getInteger("questionNo"), item); return values; }
    private static Set<Long> ids(String value) { try { List<Long> rows = value == null ? null : JSON.parseArray(value, Long.class); return rows == null ? Collections.emptySet() : rows.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new)); } catch (RuntimeException e) { return Collections.emptySet(); } }
    private static String categoryName(Map<Long, ZycckCategory> values, Long id) { ZycckCategory category = values.get(id); return category == null ? id == null ? "" : "大类已删除（" + id + "）" : category.getName(); }
    private static String careerName(Long id, ZycckCareerQuestion career) { return career == null ? "职业已删除（编号：" + id + "）" : career.getCareerName(); }
    private static String careerNames(Set<Long> ids, Map<Long, ZycckCareerQuestion> careers) { return ids.stream().map(id -> careerName(id, careers.get(id))).collect(Collectors.joining("；")); }
    private static String codes(String value) { if (value == null) return ""; try { return String.join("；", JSON.parseArray(value, String.class)); } catch (RuntimeException e) { return value; } }
    private static String feedbackText(ZycckFeedbackQuestionView question, String value, boolean submitted) {
        if (!submitted) return "未提交";
        List<String> selected;
        try { selected = value == null ? Collections.emptyList() : value.startsWith("[") ? JSON.parseArray(value, String.class) : Collections.singletonList(value); }
        catch (RuntimeException error) { return value; }
        if (selected == null) return "";
        return selected.stream().map(code -> question.getOptions().stream().filter(option -> option.getCode().equals(code)).map(ZycckFeedbackQuestionView.Option::getLabel).findFirst().orElse(code)).collect(Collectors.joining("；"));
    }

    /** 工作表统一样式、分表和超长文本处理；所有用户文字写为文本，禁止解释为公式。 */
    private static class Tables {
        final SXSSFWorkbook workbook;
        final CellStyle header, text;
        final List<Table> tables = new ArrayList<>();
        Table longText;
        int longTextId;
        Tables(SXSSFWorkbook workbook) {
            this.workbook = workbook;
            header = workbook.createCellStyle(); header.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex()); header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setWrapText(true); Font font = workbook.createFont(); font.setColor(IndexedColors.WHITE.getIndex()); font.setBold(true); header.setFont(font);
            text = workbook.createCellStyle(); text.setVerticalAlignment(VerticalAlignment.TOP); text.setWrapText(true);
        }
        Table table(String name, List<String> headers) { Table table = new Table(this, name, headers); tables.add(table); return table; }
        String overflow(String sheet, int row, String column, String value) {
            if (longText == null) longText = table("长文本", Arrays.asList("长文本索引", "来源工作表", "来源行号", "字段", "分段序号", "内容"));
            String key = "长文本-" + ++longTextId;
            for (int from = 0, part = 1; from < value.length(); part++) {
                int end = Math.min(value.length(), from + 30000);
                if (end < value.length() && Character.isHighSurrogate(value.charAt(end - 1))) end--;
                longText.write(key, sheet, row, column, part, value.substring(from, end)); from = end;
            }
            return "完整内容见长文本工作表：" + key;
        }
        void finish() { for (Table table : tables) table.filter(); }
    }

    /** 单个逻辑表超出 Excel 行上限时自动续表，始终保留全部记录。 */
    private static class Table {
        final Tables owner;
        final String name;
        final List<String> headers;
        Sheet sheet;
        int index = 0;
        Table(Tables owner, String name, List<String> headers) { this.owner = owner; this.name = name; this.headers = headers; nextSheet(); }
        void nextSheet() {
            sheet = owner.workbook.createSheet(index++ == 0 ? name : name + "-" + index);
            sheet.createFreezePane(Math.min(6, headers.size()), 1); sheet.setDefaultColumnWidth(20);
            Row row = sheet.createRow(0); row.setHeightInPoints(40);
            for (int i = 0; i < headers.size(); i++) { Cell cell = row.createCell(i); cell.setCellValue(headers.get(i)); cell.setCellStyle(owner.header); }
        }
        void write(Object... values) {
            if (values.length != headers.size()) throw new IllegalStateException("导出字段与表头数量不一致：" + name);
            if (sheet.getLastRowNum() >= 1048575) { filter(); nextSheet(); }
            Row row = sheet.createRow(sheet.getLastRowNum() + 1); row.setHeightInPoints(32);
            for (int i = 0; i < values.length; i++) {
                Cell cell = row.createCell(i); cell.setCellStyle(owner.text); Object value = values[i];
                if (value == null) continue;
                if (value instanceof Number) cell.setCellValue(((Number) value).doubleValue());
                else {
                    String text = value instanceof Date ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value) : String.valueOf(value);
                    if (text.length() > 32767) text = owner.overflow(sheet.getSheetName(), row.getRowNum() + 1, headers.get(i), text);
                    cell.setCellValue(text);
                }
            }
        }
        void filter() { sheet.setAutoFilter(new CellRangeAddress(0, sheet.getLastRowNum(), 0, headers.size() - 1)); }
    }
}
