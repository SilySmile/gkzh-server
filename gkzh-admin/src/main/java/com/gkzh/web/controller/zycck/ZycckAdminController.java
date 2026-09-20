package com.gkzh.web.controller.zycck;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.zycck.domain.ZycckCareerQuestion;
import com.gkzh.zycck.domain.ZycckCategory;
import com.gkzh.zycck.domain.ZycckRecord;
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
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
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

    public ZycckAdminController(ZycckCategoryMapper categoryMapper, ZycckCareerQuestionMapper questionMapper, ZycckRecordMapper recordMapper,
                                GkzhSchoolMapper schoolMapper, GkzhSchoolDepartmentMapper departmentMapper, GkzhStudentMapper studentMapper,
                                IActivityWeekService activityWeekService, ZycckCatalogCacheService catalogCacheService) {
        this.categoryMapper = categoryMapper;
        this.questionMapper = questionMapper;
        this.recordMapper = recordMapper;
        this.schoolMapper = schoolMapper;
        this.departmentMapper = departmentMapper;
        this.studentMapper = studentMapper;
        this.activityWeekService = activityWeekService;
        this.catalogCacheService = catalogCacheService;
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
    public TableDataInfo records(@RequestParam(required = false) Long instanceId, @RequestParam(required = false) Long schoolId, @RequestParam(required = false) Long departmentId, @RequestParam(required = false) String major, @RequestParam(required = false) String gender, @RequestParam(required = false) String studentNo) {
        QueryWrapper<ZycckRecord> q = buildRecordQuery(instanceId, schoolId, departmentId, major, gender, studentNo).orderByDesc("record_id");
        startPage();
        java.util.List<ZycckRecord> records = recordMapper.selectList(q);
        // 在转换为普通列表之前保留 PageHelper 查询的总数。
        TableDataInfo table = getDataTable(records);
        table.setRows(enrichRecords(records));
        return table;
    }

    @DeleteMapping("/records/{id}")
    public AjaxResult deleteRecord(@PathVariable Long id) {
        ZycckRecord record = recordMapper.selectById(id);
        if (record == null || !"zycck".equals(record.getGameType())) return AjaxResult.error("参与记录不存在");
        return toAjax(recordMapper.deleteById(id));
    }

    @GetMapping("/statistics")
    public AjaxResult statistics(@RequestParam(required = false) Long instanceId, @RequestParam(required = false) Long schoolId, @RequestParam(required = false) Long departmentId, @RequestParam(required = false) String major, @RequestParam(required = false) String gender, @RequestParam(required = false) String studentNo) {
        // 汇总在数据库中计算，不加载全部记录，也不受列表页码影响。
        long entered = recordMapper.selectCount(buildRecordQuery(instanceId, schoolId, departmentId, major, gender, studentNo));
        long finished = recordMapper.selectCount(buildRecordQuery(instanceId, schoolId, departmentId, major, gender, studentNo).eq("status", "finished"));
        TableDataInfo page = records(instanceId, schoolId, departmentId, major, gender, studentNo);
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
     * 查询单个学生在本游戏中的题目选择、正确答案和职业了解程度。
     */
    @GetMapping("/statistics/{recordId}")
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
        return AjaxResult.success(result);
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

    private QueryWrapper<ZycckRecord> buildRecordQuery(Long instanceId, Long schoolId, Long departmentId, String major, String gender, String studentNo) {
        QueryWrapper<ZycckRecord> q = new QueryWrapper<ZycckRecord>().eq("game_type", "zycck");
        if (instanceId != null) q.eq("instance_id", instanceId);
        if (schoolId != null) q.eq("school_id", schoolId);
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
            if (dept != null) row.put("departmentName", dept.getTitle());
            row.put("major", firstNonBlank(record.getMajor(), dept == null ? null : dept.getTitle()));
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

    @GetMapping("/statistics/pdf")
    public ResponseEntity<byte[]> statisticsPdf() {
        byte[] data = "%PDF-1.4\n% zycck statistics\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=zycck-statistics.pdf").contentType(MediaType.APPLICATION_PDF).body(data);
    }
}
