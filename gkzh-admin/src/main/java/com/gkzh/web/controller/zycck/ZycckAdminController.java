package com.gkzh.web.controller.zycck;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zycck.domain.ZycckCareerQuestion;
import com.gkzh.zycck.domain.ZycckCategory;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.domain.ZycckPrintTask;
import com.gkzh.zycck.dto.ZycckPrintTaskView;
import com.gkzh.zycck.dto.ZycckBeaconSettingsView;
import com.gkzh.zycck.dto.ZycckPrinterView;
import com.gkzh.zycck.mapper.ZycckCareerQuestionMapper;
import com.gkzh.zycck.mapper.ZycckCategoryMapper;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.app.service.ZycckCatalogCacheService;
import com.gkzh.app.service.ZycckPrintService;
import com.gkzh.app.service.ZycckBeaconConfigService;
import com.gkzh.zycck.service.ZycckPrinterService;
import com.gkzh.zycck.service.ZycckActivityFeedbackService;
import com.gkzh.zycck.dto.ZycckCareerBriefView;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * zycck Web 管理接口；查询统一以活动实例和游戏上下文为边界。
 */
@RestController
@RequestMapping("/zycck/admin")
public class ZycckAdminController extends BaseController {
    private final ZycckCategoryMapper categoryMapper;
    private final ZycckCareerQuestionMapper questionMapper;
    private final ZycckRecordMapper recordMapper;
    private final GkzhSchoolMapper schoolMapper;
    private final GkzhSchoolDepartmentMapper departmentMapper;
    private final GkzhStudentMapper studentMapper;
    private final IActivityWeekService activityWeekService;
    private final ZycckCatalogCacheService catalogCacheService;
    private final ZycckPrinterService printerService;
    private final ZycckPrintService printService;
    private final ZycckBeaconConfigService beaconConfigService;
    private final ZycckActivityFeedbackService activityFeedbackService;

    public ZycckAdminController(ZycckCategoryMapper categoryMapper, ZycckCareerQuestionMapper questionMapper, ZycckRecordMapper recordMapper,
                                GkzhSchoolMapper schoolMapper, GkzhSchoolDepartmentMapper departmentMapper, GkzhStudentMapper studentMapper,
                                IActivityWeekService activityWeekService, ZycckCatalogCacheService catalogCacheService,
                                ZycckPrinterService printerService, ZycckPrintService printService,
                                ZycckBeaconConfigService beaconConfigService, ZycckActivityFeedbackService activityFeedbackService) {
        this.categoryMapper = categoryMapper;
        this.questionMapper = questionMapper;
        this.recordMapper = recordMapper;
        this.schoolMapper = schoolMapper;
        this.departmentMapper = departmentMapper;
        this.studentMapper = studentMapper;
        this.activityWeekService = activityWeekService;
        this.catalogCacheService = catalogCacheService;
        this.printerService = printerService;
        this.printService = printService;
        this.beaconConfigService = beaconConfigService;
        this.activityFeedbackService = activityFeedbackService;
    }

    @GetMapping("/categories")
    public TableDataInfo categories() {
        startPage();
        java.util.List<Map<String, Object>> rows = new java.util.ArrayList<>();
        for (ZycckCategory category : categoryMapper.selectList(new QueryWrapper<ZycckCategory>().orderByAsc("sort_order"))) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("categoryId", category.getCategoryId());
            row.put("code", category.getCode());
            row.put("name", category.getName());
            row.put("description", category.getDescription());
            row.put("drawMode", category.getDrawMode());
            row.put("sortOrder", category.getSortOrder());
            row.put("status", category.getStatus());
            QueryWrapper<ZycckCareerQuestion> all = new QueryWrapper<ZycckCareerQuestion>().eq("category_id", category.getCategoryId()).eq("status", "0");
            QueryWrapper<ZycckCareerQuestion> candidates = new QueryWrapper<ZycckCareerQuestion>().eq("category_id", category.getCategoryId()).eq("has_question", "1").eq("draw_candidate", "1").eq("status", "0");
            QueryWrapper<ZycckCareerQuestion> questions = new QueryWrapper<ZycckCareerQuestion>().eq("category_id", category.getCategoryId()).eq("has_question", "1").eq("status", "0");
            row.put("careerCount", questionMapper.selectCount(all));
            row.put("questionCount", questionMapper.selectCount(questions));
            row.put("candidateCount", questionMapper.selectCount(candidates));
            rows.add(row);
        }
        return getDataTable(rows);
    }

    @PostMapping("/categories")
    public AjaxResult saveCategory(@RequestBody ZycckCategory category) {
        if (category.getCode() == null || category.getCode().trim().isEmpty()) {
            if (category.getCategoryId() != null) {
                ZycckCategory current = categoryMapper.selectById(category.getCategoryId());
                if (current != null && current.getCode() != null && !current.getCode().trim().isEmpty()) {
                    category.setCode(current.getCode());
                }
            }
            if (category.getCode() == null || category.getCode().trim().isEmpty()) {
                category.setCode("category_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            }
        } else {
            category.setCode(category.getCode().trim());
        }
        int rows = category.getCategoryId() == null ? categoryMapper.insert(category) : categoryMapper.updateById(category);
        if (rows > 0) catalogCacheService.evict();
        return toAjax(rows);
    }

    @DeleteMapping("/categories/{id}")
    public AjaxResult deleteCategory(@PathVariable Long id) {
        int rows = categoryMapper.deleteById(id);
        if (rows > 0) catalogCacheService.evict();
        return toAjax(rows);
    }

    @GetMapping("/career-questions")
    public TableDataInfo questions(@RequestParam(required = false) Long categoryId, @RequestParam(required = false) Integer hasQuestion) {
        startPage();
        QueryWrapper<ZycckCareerQuestion> q = new QueryWrapper<ZycckCareerQuestion>().orderByAsc("category_id", "sort_order", "career_question_id");
        if (categoryId != null) q.eq("category_id", categoryId);
        if (hasQuestion != null) q.eq("has_question", hasQuestion == 1 ? "1" : "0");
        return getDataTable(questionMapper.selectList(q));
    }

    @PostMapping("/career-questions")
    public AjaxResult saveQuestion(@RequestBody ZycckCareerQuestion question) {
        if (question.getHasQuestion() == null) question.setHasQuestion("1");
        if (!"1".equals(question.getHasQuestion())) {
            question.setHasQuestion("0");
            question.setDrawCandidate("0");
            question.setOptionA(null);
            question.setOptionB(null);
            question.setOptionC(null);
            question.setOptionD(null);
            question.setOptionACareerId(null);
            question.setOptionBCareerId(null);
            question.setOptionCCareerId(null);
            question.setOptionDCareerId(null);
            question.setCorrectOptionKey(null);
        } else {
            // 题目选项均为文本，只有正确选项允许绑定本题职业。
            String correct = question.getCorrectOptionKey();
            question.setOptionACareerId("A".equalsIgnoreCase(correct) ? question.getCareerQuestionId() : null);
            question.setOptionBCareerId("B".equalsIgnoreCase(correct) ? question.getCareerQuestionId() : null);
            question.setOptionCCareerId("C".equalsIgnoreCase(correct) ? question.getCareerQuestionId() : null);
            question.setOptionDCareerId("D".equalsIgnoreCase(correct) ? question.getCareerQuestionId() : null);
        }
        int rows = question.getCareerQuestionId() == null ? questionMapper.insert(question) : questionMapper.updateById(question);
        if (rows > 0) catalogCacheService.evict();
        return toAjax(rows);
    }

    @DeleteMapping("/career-questions/{id}")
    public AjaxResult deleteQuestion(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean careerOnly) {
        ZycckCareerQuestion question = questionMapper.selectById(id);
        if (question == null) return AjaxResult.error("职业或题目不存在");
        if (careerOnly) {
            int rows = questionMapper.deleteById(id);
            if (rows > 0) catalogCacheService.evict();
            return toAjax(rows);
        }
        UpdateWrapper<ZycckCareerQuestion> update = new UpdateWrapper<ZycckCareerQuestion>()
                .eq("career_question_id", id)
                .set("has_question", "0")
                .set("draw_candidate", "0")
                .set("option_a", null).set("option_b", null).set("option_c", null).set("option_d", null)
                .set("option_a_career_id", null).set("option_b_career_id", null).set("option_c_career_id", null).set("option_d_career_id", null)
                .set("correct_option_key", null).set("update_time", new java.util.Date());
        int rows = questionMapper.update(null, update);
        if (rows > 0) catalogCacheService.evict();
        return toAjax(rows);
    }

    @GetMapping("/records")
    public TableDataInfo records(@RequestParam(required = false) Long instanceId, @RequestParam(required = false) Long schoolId, @RequestParam(required = false) Long departmentId, @RequestParam(required = false) String major, @RequestParam(required = false) String gender, @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String statusFilter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        QueryWrapper<ZycckRecord> q = buildRecordQuery(instanceId, schoolId, departmentId, major, gender, studentNo, statusFilter, startDate, endDate).orderByDesc("record_id");
        startPage();
        java.util.List<ZycckRecord> records = recordMapper.selectList(q);
        // 在转换为普通列表之前保留 PageHelper 查询的总数。
        TableDataInfo table = getDataTable(records);
        table.setRows(enrichRecords(records));
        return table;
    }

    @DeleteMapping("/records/{id}")
    @org.springframework.transaction.annotation.Transactional
    public AjaxResult deleteRecord(@PathVariable Long id) {
        // 与反馈提交锁定同一行，保证删除和反馈完成不能交叉留下孤立数据。
        ZycckRecord record = recordMapper.selectOne(new QueryWrapper<ZycckRecord>()
                .eq("record_id", id).last("FOR UPDATE"));
        if (record == null || !"zycck".equals(record.getGameType())) return AjaxResult.error("参与记录不存在");
        // 三张表在同一事务清理；范围来自数据库记录，不能仅按用户删除其他活动或游戏。
        activityWeekService.deleteGameParticipations(record.getInstanceId(), record.getGameId(), record.getUserId());
        activityFeedbackService.deleteForRecord(id);
        int rows = recordMapper.deleteById(id);
        if (rows != 1) throw new ServiceException("参与记录删除失败，请重试");
        return toAjax(rows);
    }

    @GetMapping("/statistics")
    public AjaxResult statistics(@RequestParam(required = false) Long instanceId, @RequestParam(required = false) Long schoolId, @RequestParam(required = false) Long departmentId, @RequestParam(required = false) String major, @RequestParam(required = false) String gender, @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String statusFilter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // 汇总在数据库中计算，不加载全部记录，也不受列表页码影响。
        long entered = recordMapper.selectCount(buildRecordQuery(instanceId, schoolId, departmentId, major, gender, studentNo, statusFilter, startDate, endDate));
        long finished = recordMapper.selectCount(buildRecordQuery(instanceId, schoolId, departmentId, major, gender, studentNo, statusFilter, startDate, endDate).eq("status", "finished"));
        TableDataInfo page = records(instanceId, schoolId, departmentId, major, gender, studentNo, statusFilter, startDate, endDate);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("participating", entered);
        result.put("enteredCount", entered);
        result.put("finished", finished);
        result.put("finishedCount", finished);
        result.put("total", page.getTotal());
        result.put("records", page.getRows());
        return AjaxResult.success(result);
    }

    /**
     * 查询单个学生的答题、浏览职业、当前探索清单和活动反馈，供后续总结报告取数。
     */
    // 详情路径只接受数字记录编号，避免把 /statistics/excel 等固定功能地址误识别为记录编号。
    @GetMapping("/statistics/{recordId:\\d+}")
    public AjaxResult statisticsRecord(@PathVariable Long recordId) {
        ZycckRecord record = recordMapper.selectById(recordId);
        if (record == null || !"zycck".equals(record.getGameType())) return AjaxResult.error("参与记录不存在");
        Map<String, Object> result = enrichRecords(java.util.Collections.singletonList(record)).get(0);
        java.util.List<JSONObject> snapshots = parseJsonArray(record.getOptionSnapshotJson());
        java.util.Map<Integer, JSONObject> answers = indexByQuestionNo(parseJsonArray(record.getAnswerJson()));
        java.util.Map<Integer, JSONObject> awareness = indexByQuestionNo(parseJsonArray(record.getAwarenessJson()));
        java.util.List<Map<String, Object>> choices = new java.util.ArrayList<>();
        for (JSONObject snapshot : snapshots) {
            int no = snapshot.getIntValue("questionNo");
            JSONObject answer = answers.get(no);
            JSONObject aware = awareness.get(no);
            String selectedKey = answer == null ? null : answer.getString("optionKey");
            ZycckCareerQuestion question = snapshot.getLong("questionId") == null ? null : questionMapper.selectById(snapshot.getLong("questionId"));
            String correctKey = question == null ? null : question.getCorrectOptionKey();
            Map<String, Object> choice = new LinkedHashMap<>();
            choice.put("questionNo", no);
            choice.put("scenarioCareerName", snapshot.getString("careerName"));
            choice.put("selectedOptionKey", selectedKey);
            choice.put("selectedCareerName", optionText(snapshot, selectedKey));
            choice.put("correctOptionKey", correctKey);
            choice.put("correctCareerName", optionText(snapshot, correctKey));
            choice.put("correct", selectedKey != null && correctKey != null && selectedKey.equalsIgnoreCase(correctKey));
            choice.put("timeout", answer != null && answer.getBooleanValue("timeout"));
            choice.put("awareness", aware == null ? null : (aware.getString("level") != null ? aware.getString("level") : aware.getString("awareness")));
            choices.add(choice);
        }
        result.put("choices", choices);
        // viewedCareers：看过的职业；explorationCareers：当前保留在探索清单中的职业。
        result.put("viewedCareers", careerBriefs(record.getViewedCareerIds()));
        result.put("explorationCareers", careerBriefs(record.getExplorationCareerIds()));
        // activityFeedback：题目定义、稳定选项编码、补充文字及反馈提交时间。
        result.put("activityFeedback", activityFeedbackService.view(record));
        return AjaxResult.success(result);
    }

    /** 按记录中的编号顺序返回职业，原职业被删除时也保留编号，避免统计漏项。 */
    private java.util.List<ZycckCareerBriefView> careerBriefs(String value) {
        java.util.List<Long> ids = value == null || value.trim().isEmpty()
                ? java.util.Collections.emptyList() : JSON.parseArray(value, Long.class);
        java.util.List<ZycckCareerBriefView> result = new java.util.ArrayList<>();
        if (ids.isEmpty()) return result;
        java.util.Map<Long, ZycckCareerQuestion> careers = new java.util.HashMap<>();
        for (ZycckCareerQuestion career : questionMapper.selectBatchIds(ids)) careers.put(career.getCareerQuestionId(), career);
        for (Long id : new java.util.LinkedHashSet<>(ids)) {
            ZycckCareerQuestion career = careers.get(id);
            ZycckCareerBriefView item = new ZycckCareerBriefView();
            item.setCareerId(id);
            item.setCareerName(career == null ? "职业已删除（编号：" + id + "）" : career.getCareerName());
            item.setOneLineIntro(career == null ? null : career.getOneLineIntro());
            result.add(item);
        }
        return result;
    }

    private java.util.List<JSONObject> parseJsonArray(String value) {
        if (value == null || value.trim().isEmpty()) return new java.util.ArrayList<>();
        try {
            return JSON.parseArray(value, JSONObject.class);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    private java.util.Map<Integer, JSONObject> indexByQuestionNo(java.util.List<JSONObject> values) {
        java.util.Map<Integer, JSONObject> result = new java.util.HashMap<>();
        for (JSONObject value : values) result.put(value.getIntValue("questionNo"), value);
        return result;
    }

    private String optionText(JSONObject snapshot, String key) {
        if (key == null || key.trim().isEmpty()) return null;
        return snapshot.getString("option" + key.trim().toUpperCase());
    }

    // 与列表补全规则一致：优先关联记录中的学生，旧记录再按同校用户查找学生。
    private static final String STUDENT_ID_SQL = "COALESCE("
            + "(SELECT s.student_id FROM gkzh_student s WHERE s.student_id = gkzh_zycck_record.student_id AND s.del_flag = '0'), "
            + "(SELECT s.student_id FROM gkzh_student s WHERE s.user_id = gkzh_zycck_record.user_id "
            + "AND s.school_id = gkzh_zycck_record.school_id AND s.del_flag = '0' ORDER BY s.student_id LIMIT 1))";

    private static String studentColumn(String column) {
        return "(SELECT s." + column + " FROM gkzh_student s WHERE s.student_id = " + STUDENT_ID_SQL + ")";
    }

    private QueryWrapper<ZycckRecord> buildRecordQuery(Long instanceId, Long schoolId, Long departmentId, String major, String gender, String studentNo,
            String statusFilter, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate))
            throw new ServiceException("开始日期不能晚于结束日期");
        QueryWrapper<ZycckRecord> q = new QueryWrapper<ZycckRecord>().eq("game_type", "zycck");
        if (instanceId != null) q.eq("instance_id", instanceId);
        if (schoolId != null) q.eq("school_id", schoolId);
        // 参与日期以扫码时间为准，结束日的 23:59:59 也包含在内；不对列做 DATE() 运算。
        if (startDate != null) q.ge("scan_time", startDate.atStartOfDay());
        if (endDate != null) q.lt("scan_time", endDate.plusDays(1).atStartOfDay());
        String department = "COALESCE(gkzh_zycck_record.department_id, " + studentColumn("department_id") + ")";
        if (departmentId != null) {
            q.apply("(" + department + " = {0} OR EXISTS (SELECT 1 FROM gkzh_school_department d WHERE d.department_id = "
                    + department + " AND FIND_IN_SET({0}, d.ancestors)))", departmentId);
        }
        if (major != null && !major.trim().isEmpty()) {
            q.apply("COALESCE(NULLIF(TRIM(gkzh_zycck_record.major), ''), "
                    + "(SELECT d.title FROM gkzh_school_department d WHERE d.department_id = "
                    + department + ")) LIKE {0}", "%" + major.trim() + "%");
        }
        if (gender != null && !gender.trim().isEmpty()) {
            String effectiveGender = "COALESCE(NULLIF(TRIM(gkzh_zycck_record.gender), ''), "
                    + "NULLIF(TRIM(" + studentColumn("gender") + "), ''), '其他')";
            String value = genderText(gender);
            if ("男".equals(value)) q.apply(effectiveGender + " IN ({0}, {1})", "男", "0");
            else if ("女".equals(value)) q.apply(effectiveGender + " IN ({0}, {1})", "女", "1");
            else q.apply(effectiveGender + " NOT IN ({0}, {1}, {2}, {3})", "男", "0", "女", "1");
        }
        if (studentNo != null && !studentNo.trim().isEmpty()) {
            q.apply(studentColumn("student_no") + " LIKE {0}", "%" + studentNo.trim() + "%");
        }
        // 页面状态不是简单的 status 字段：待活动反馈表示已进入反馈阶段但还未完成。
        String status = statusFilter == null ? null : statusFilter.trim();
        if (status == null || status.isEmpty()) return q;
        if ("finished".equals(status)) q.eq("status", "finished");
        else if ("pendingFeedback".equals(status))
            q.apply("(status IS NULL OR status <> {0}) AND stage = {1}", "finished", "activity_feedback");
        else if ("inProgress".equals(status))
            q.apply("(status IS NULL OR status <> {0}) AND (stage IS NULL OR stage <> {1})", "finished", "activity_feedback");
        else throw new ServiceException("参与状态筛选值无效");
        return q;
    }

    private java.util.List<Map<String, Object>> enrichRecords(java.util.List<ZycckRecord> records) {
        java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
        if (records.isEmpty()) return result;
        java.util.Map<Long, String> instanceNames = new java.util.HashMap<>();
        for (GkzhActivityWeekInstance instance : activityWeekService.listInstances(null))
            instanceNames.put(instance.getInstanceId(), instance.getTitle());
        for (ZycckRecord record : records) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("recordId", record.getRecordId());
            row.put("instanceId", record.getInstanceId());
            row.put("instanceName", instanceNames.get(record.getInstanceId()));
            row.put("gameId", record.getGameId());
            row.put("userId", record.getUserId());
            row.put("studentId", record.getStudentId());
            row.put("schoolId", record.getSchoolId());
            row.put("departmentId", record.getDepartmentId());
            row.put("major", record.getMajor());
            row.put("gender", record.getGender());
            row.put("gameType", record.getGameType());
            row.put("status", record.getStatus());
            row.put("stage", record.getStage());
            row.put("scanTime", record.getScanTime());
            row.put("finishTime", record.getFinishTime());
            row.put("createTime", record.getCreateTime());
            row.put("updateTime", record.getUpdateTime());
            GkzhStudent student = record.getStudentId() == null ? null : studentMapper.selectGkzhStudentByStudentId(record.getStudentId());
            if (student != null && (!record.getSchoolId().equals(student.getSchoolId())
                    || !record.getUserId().equals(student.getUserId()))) student = null;
            if (student == null && record.getUserId() != null) {
                GkzhStudent fallback = studentMapper.selectOne(new QueryWrapper<GkzhStudent>()
                        .eq("user_id", record.getUserId()).eq("school_id", record.getSchoolId())
                        .eq("del_flag", "0").orderByAsc("student_id").last("limit 1"));
                if (fallback != null) student = studentMapper.selectGkzhStudentByStudentId(fallback.getStudentId());
            }
            if (student != null) {
                row.put("studentName", student.getStudentName());
                row.put("studentNo", student.getStudentNo());
                if (row.get("departmentId") == null) row.put("departmentId", student.getDepartmentId());
            }
            row.put("gender", genderText(firstNonBlank(record.getGender(), student == null ? null : student.getGender())));
            GkzhSchool school = record.getSchoolId() == null ? null : schoolMapper.selectGkzhSchoolBySchoolId(record.getSchoolId());
            if (school != null) row.put("schoolName", school.getTitle());
            Long effectiveDepartmentId = (Long) row.get("departmentId");
            GkzhSchoolDepartment dept = effectiveDepartmentId == null ? null : departmentMapper.selectDepartmentById(effectiveDepartmentId);
            if (dept != null && record.getSchoolId().equals(dept.getSchoolId())) {
                // 学生 department_id 指向专业；页面“院系”展示专业的父部门。
                GkzhSchoolDepartment college = dept.getParentId() == null || dept.getParentId() <= 0
                        ? dept : departmentMapper.selectDepartmentById(dept.getParentId());
                if (college != null && record.getSchoolId().equals(college.getSchoolId())) row.put("departmentName", college.getTitle());
                row.put("major", firstNonBlank(record.getMajor(), dept.getParentId() != null && dept.getParentId() > 0 ? dept.getTitle() : null));
            } else row.put("major", firstNonBlank(record.getMajor(), null));
            result.add(row);
        }
        return result;
    }

    private String genderText(String gender) {
        String value = gender == null ? "" : gender.trim();
        if ("0".equals(value) || "男".equals(value)) return "男";
        if ("1".equals(value) || "女".equals(value)) return "女";
        return "其他";
    }

    private String firstNonBlank(String value, String fallback) {
        if (value != null && !value.trim().isEmpty()) return value.trim();
        return fallback == null ? null : fallback.trim();
    }

    @GetMapping("/printers")
    @PreAuthorize("@ss.hasPermi('zycck:printer:list')")
    public AjaxResult printers() {
        return AjaxResult.success(printerService.listAllPrinters().stream()
                .map(ZycckPrinterView::from).collect(java.util.stream.Collectors.toList()));
    }

    /** 读取独立信标管理菜单中的多信标配置。 */
    @GetMapping("/beacons/config")
    @PreAuthorize("@ss.hasPermi('zycck:beacon:list')")
    public AjaxResult beaconConfig() {
        return AjaxResult.success(beaconConfigService.get());
    }

    /** 保存后立即用于学生端展示及服务端打印校验。 */
    @PutMapping("/beacons/config")
    @PreAuthorize("@ss.hasPermi('zycck:beacon:edit')")
    public AjaxResult saveBeaconConfig(@RequestBody ZycckBeaconSettingsView body) {
        return AjaxResult.success(beaconConfigService.save(body));
    }

    @PostMapping("/printers/sync")
    @PreAuthorize("@ss.hasPermi('zycck:printer:sync')")
    public AjaxResult syncPrinters() {
        return AjaxResult.success(printerService.syncCloudPrinters().stream()
                .map(ZycckPrinterView::from).collect(java.util.stream.Collectors.toList()));
    }

    @PostMapping("/printers/bind")
    @PreAuthorize("@ss.hasPermi('zycck:printer:bind')")
    public AjaxResult bindPrinter(@RequestBody Map<String, Object> body) {
        return AjaxResult.success(ZycckPrinterView.from(printerService.bindPrinter(
                text(body.get("equipmentSn")), text(body.get("equipmentSecret")), text(body.get("printerName")))));
    }

    @PostMapping("/printers/{id}/name")
    @PreAuthorize("@ss.hasPermi('zycck:printer:edit')")
    public AjaxResult renamePrinter(@PathVariable Long id, @RequestParam String printerName) {
        return AjaxResult.success(ZycckPrinterView.from(printerService.renamePrinter(id, printerName)));
    }

    @PostMapping("/printers/{id}/status")
    @PreAuthorize("@ss.hasPermi('zycck:printer:edit')")
    public AjaxResult refreshPrinterStatus(@PathVariable Long id) {
        return AjaxResult.success(ZycckPrinterView.from(printerService.refreshPrinterStatus(id)));
    }

    @DeleteMapping("/printers/{id}")
    @PreAuthorize("@ss.hasPermi('zycck:printer:remove')")
    public AjaxResult unbindPrinter(@PathVariable Long id) {
        return AjaxResult.success(ZycckPrinterView.from(printerService.unbindPrinter(id)));
    }

    /** 打印任务分页列表，支持按快照字段查询历史打印人员和活动。 */
    @GetMapping("/print-tasks")
    @PreAuthorize("@ss.hasPermi('zycck:print-task:list')")
    public TableDataInfo printTasks(@RequestParam(required = false) Long printerId,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) Long recordId,
                                    @RequestParam(required = false) String schoolName,
                                    @RequestParam(required = false) String studentNo,
                                    @RequestParam(required = false) String studentName,
                                    @RequestParam(required = false) String activityName,
                                    @RequestParam(required = false) String gameName) {
        startPage();
        java.util.List<ZycckPrintTask> tasks = printService.listTasks(
                printerId, status, recordId, schoolName, studentNo, studentName, activityName, gameName);
        TableDataInfo table = getDataTable(tasks);
        table.setRows(tasks.stream().map(ZycckPrintTaskView::from)
                .collect(java.util.stream.Collectors.toList()));
        return table;
    }

    @PostMapping("/print-tasks/{taskId}/reprint")
    @PreAuthorize("@ss.hasPermi('zycck:print-task:reprint')")
    public AjaxResult reprint(@PathVariable Long taskId) throws IOException {
        return AjaxResult.success(ZycckPrintTaskView.from(printService.reprint(taskId)));
    }

    @PostMapping("/print-tasks/{taskId}/status")
    @PreAuthorize("@ss.hasPermi('zycck:print-task:query')")
    public AjaxResult refreshPrintTask(@PathVariable Long taskId) {
        return AjaxResult.success(ZycckPrintTaskView.from(printService.refreshTask(taskId)));
    }

    /** 管理端取消打印；服务层会先核对汉印云状态再执行。 */
    @PostMapping("/print-tasks/{taskId}/cancel")
    @PreAuthorize("@ss.hasPermi('zycck:print-task:cancel')")
    public AjaxResult cancelPrintTask(@PathVariable Long taskId) {
        return AjaxResult.success(ZycckPrintTaskView.from(printService.cancelTask(taskId)));
    }

    /** 管理端删除本地打印记录；进行中的任务必须先取消。 */
    @DeleteMapping("/print-tasks/{taskId}")
    @PreAuthorize("@ss.hasPermi('zycck:print-task:remove')")
    public AjaxResult deletePrintTask(@PathVariable Long taskId) {
        printService.deleteTask(taskId);
        return AjaxResult.success("打印任务记录已删除");
    }

    private String text(Object value) {
        if (value == null) return null;
        String result = String.valueOf(value).trim();
        return result.isEmpty() ? null : result;
    }

}
