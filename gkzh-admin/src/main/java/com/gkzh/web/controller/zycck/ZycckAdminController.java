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
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** zycck Web 管理接口；查询统一以活动实例和游戏上下文为边界。 */
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

    public ZycckAdminController(ZycckCategoryMapper categoryMapper, ZycckCareerQuestionMapper questionMapper, ZycckRecordMapper recordMapper,
                                GkzhSchoolMapper schoolMapper, GkzhSchoolDepartmentMapper departmentMapper, GkzhStudentMapper studentMapper,
                                IActivityWeekService activityWeekService) {
        this.categoryMapper = categoryMapper; this.questionMapper = questionMapper; this.recordMapper = recordMapper;
        this.schoolMapper = schoolMapper; this.departmentMapper = departmentMapper; this.studentMapper = studentMapper; this.activityWeekService = activityWeekService;
    }

    @GetMapping("/categories")
    public TableDataInfo categories() {
        startPage();
        java.util.List<Map<String,Object>> rows = new java.util.ArrayList<>();
        for (ZycckCategory category : categoryMapper.selectList(new QueryWrapper<ZycckCategory>().orderByAsc("sort_order"))) {
            Map<String,Object> row = new LinkedHashMap<>();
            row.put("categoryId", category.getCategoryId()); row.put("code", category.getCode()); row.put("name", category.getName());
            row.put("description", category.getDescription()); row.put("drawMode", category.getDrawMode()); row.put("sortOrder", category.getSortOrder()); row.put("status", category.getStatus());
            QueryWrapper<ZycckCareerQuestion> all = new QueryWrapper<ZycckCareerQuestion>().eq("category_id", category.getCategoryId()).eq("status", "0");
            QueryWrapper<ZycckCareerQuestion> candidates = new QueryWrapper<ZycckCareerQuestion>().eq("category_id", category.getCategoryId()).eq("has_question", "1").eq("draw_candidate", "1").eq("status", "0");
            QueryWrapper<ZycckCareerQuestion> questions = new QueryWrapper<ZycckCareerQuestion>().eq("category_id", category.getCategoryId()).eq("has_question", "1").eq("status", "0");
            row.put("careerCount", questionMapper.selectCount(all)); row.put("questionCount", questionMapper.selectCount(questions)); row.put("candidateCount", questionMapper.selectCount(candidates)); rows.add(row);
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
        return toAjax(category.getCategoryId() == null ? categoryMapper.insert(category) : categoryMapper.updateById(category));
    }

    @DeleteMapping("/categories/{id}")
    public AjaxResult deleteCategory(@PathVariable Long id) { return toAjax(categoryMapper.deleteById(id)); }

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
            question.setOptionA(null); question.setOptionB(null); question.setOptionC(null); question.setOptionD(null);
            question.setOptionACareerId(null); question.setOptionBCareerId(null); question.setOptionCCareerId(null); question.setOptionDCareerId(null);
            question.setCorrectOptionKey(null);
        }
        return toAjax(question.getCareerQuestionId() == null ? questionMapper.insert(question) : questionMapper.updateById(question));
    }

    @DeleteMapping("/career-questions/{id}")
    public AjaxResult deleteQuestion(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean careerOnly) {
        ZycckCareerQuestion question = questionMapper.selectById(id);
        if (question == null) return AjaxResult.error("职业或题目不存在");
        if (careerOnly) return toAjax(questionMapper.deleteById(id));
        UpdateWrapper<ZycckCareerQuestion> update = new UpdateWrapper<ZycckCareerQuestion>()
                .eq("career_question_id", id)
                .set("has_question", "0")
                .set("draw_candidate", "0")
                .set("option_a", null).set("option_b", null).set("option_c", null).set("option_d", null)
                .set("option_a_career_id", null).set("option_b_career_id", null).set("option_c_career_id", null).set("option_d_career_id", null)
                .set("correct_option_key", null).set("update_time", new java.util.Date());
        return toAjax(questionMapper.update(null, update));
    }

    @GetMapping("/records")
    public TableDataInfo records(@RequestParam(required = false) Long instanceId, @RequestParam(required = false) Long schoolId, @RequestParam(required = false) Long departmentId, @RequestParam(required = false) String major, @RequestParam(required = false) String gender) {
        startPage(); QueryWrapper<ZycckRecord> q = buildRecordQuery(instanceId, schoolId, departmentId, major, gender).orderByDesc("record_id");
        return getDataTable(enrichRecords(recordMapper.selectList(q)));
    }

    @DeleteMapping("/records/{id}")
    public AjaxResult deleteRecord(@PathVariable Long id) {
        ZycckRecord record = recordMapper.selectById(id);
        if (record == null || !"zycck".equals(record.getGameType())) return AjaxResult.error("参与记录不存在");
        return toAjax(recordMapper.deleteById(id));
    }

    @GetMapping("/statistics")
    public AjaxResult statistics(@RequestParam(required = false) Long instanceId, @RequestParam(required = false) Long schoolId, @RequestParam(required = false) Long departmentId, @RequestParam(required = false) String major, @RequestParam(required = false) String gender) {
        QueryWrapper<ZycckRecord> q = buildRecordQuery(instanceId, schoolId, departmentId, major, gender);
        java.util.List<ZycckRecord> rows = recordMapper.selectList(q); Map<String,Object> result = new LinkedHashMap<>();
        result.put("participating", rows.size()); result.put("enteredCount", rows.size()); result.put("finished", rows.stream().filter(x -> "finished".equals(x.getStatus())).count());
        result.put("finishedCount", rows.stream().filter(x -> "finished".equals(x.getStatus())).count()); result.put("records", enrichRecords(rows)); return AjaxResult.success(result);
    }

    /** 查询单个学生在本游戏中的题目选择、正确答案和职业了解程度。 */
    @GetMapping("/statistics/{recordId}")
    public AjaxResult statisticsRecord(@PathVariable Long recordId) {
        ZycckRecord record = recordMapper.selectById(recordId);
        if (record == null || !"zycck".equals(record.getGameType())) return AjaxResult.error("参与记录不存在");
        Map<String,Object> result = enrichRecords(java.util.Collections.singletonList(record)).get(0);
        java.util.List<JSONObject> snapshots = parseJsonArray(record.getOptionSnapshotJson());
        java.util.Map<Integer, JSONObject> answers = indexByQuestionNo(parseJsonArray(record.getAnswerJson()));
        java.util.Map<Integer, JSONObject> awareness = indexByQuestionNo(parseJsonArray(record.getAwarenessJson()));
        java.util.List<Map<String,Object>> choices = new java.util.ArrayList<>();
        for (JSONObject snapshot : snapshots) {
            int no = snapshot.getIntValue("questionNo");
            JSONObject answer = answers.get(no);
            JSONObject aware = awareness.get(no);
            String selectedKey = answer == null ? null : answer.getString("optionKey");
            ZycckCareerQuestion question = snapshot.getLong("questionId") == null ? null : questionMapper.selectById(snapshot.getLong("questionId"));
            String correctKey = question == null ? null : question.getCorrectOptionKey();
            Map<String,Object> choice = new LinkedHashMap<>();
            choice.put("questionNo", no); choice.put("scenarioCareerName", snapshot.getString("careerName"));
            choice.put("selectedOptionKey", selectedKey); choice.put("selectedCareerName", optionText(snapshot, selectedKey));
            choice.put("correctOptionKey", correctKey); choice.put("correctCareerName", optionText(snapshot, correctKey));
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
        try { return JSON.parseArray(value, JSONObject.class); } catch (Exception e) { return new java.util.ArrayList<>(); }
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

    private QueryWrapper<ZycckRecord> buildRecordQuery(Long instanceId, Long schoolId, Long departmentId, String major, String gender) {
        QueryWrapper<ZycckRecord> q = new QueryWrapper<ZycckRecord>().eq("game_type", "zycck");
        if (instanceId != null) q.eq("instance_id", instanceId); if (schoolId != null) q.eq("school_id", schoolId); if (departmentId != null) q.eq("department_id", departmentId);
        if (major != null && !major.trim().isEmpty()) q.like("major", major.trim()); if (gender != null && !gender.trim().isEmpty()) q.eq("gender", gender.trim()); return q;
    }

    private java.util.List<Map<String,Object>> enrichRecords(java.util.List<ZycckRecord> records) {
        java.util.List<Map<String,Object>> result = new java.util.ArrayList<>();
        java.util.Map<Long, String> instanceNames = new java.util.HashMap<>();
        for (GkzhActivityWeekInstance instance : activityWeekService.listInstances(null)) instanceNames.put(instance.getInstanceId(), instance.getTitle());
        for (ZycckRecord record : records) {
            Map<String,Object> row = new LinkedHashMap<>(); row.put("recordId", record.getRecordId()); row.put("instanceId", record.getInstanceId()); row.put("instanceName", instanceNames.get(record.getInstanceId())); row.put("gameId", record.getGameId()); row.put("userId", record.getUserId()); row.put("studentId", record.getStudentId()); row.put("schoolId", record.getSchoolId()); row.put("departmentId", record.getDepartmentId()); row.put("major", record.getMajor()); row.put("gender", record.getGender()); row.put("gameType", record.getGameType()); row.put("status", record.getStatus()); row.put("stage", record.getStage()); row.put("scanTime", record.getScanTime()); row.put("finishTime", record.getFinishTime()); row.put("createTime", record.getCreateTime()); row.put("updateTime", record.getUpdateTime());
            GkzhStudent student = record.getStudentId() == null ? null : studentMapper.selectGkzhStudentByStudentId(record.getStudentId());
            if (student == null && record.getUserId() != null) student = studentMapper.selectOne(new QueryWrapper<GkzhStudent>().eq("user_id", record.getUserId()).last("limit 1"));
            if (student != null) { row.put("studentName", student.getStudentName()); row.put("studentNo", student.getStudentNo()); if (row.get("major") == null) row.put("major", student.getDepartmentName()); if (row.get("gender") == null) row.put("gender", genderText(student.getGender())); }
            GkzhSchool school = record.getSchoolId() == null ? null : schoolMapper.selectGkzhSchoolBySchoolId(record.getSchoolId()); if (school != null) row.put("schoolName", school.getTitle());
            GkzhSchoolDepartment dept = record.getDepartmentId() == null ? null : departmentMapper.selectDepartmentById(record.getDepartmentId()); if (dept != null) row.put("departmentName", dept.getTitle());
            result.add(row);
        }
        return result;
    }

    private String genderText(String gender) {
        if ("0".equals(gender)) return "男";
        if ("1".equals(gender)) return "女";
        return gender;
    }

    @GetMapping("/statistics/pdf")
    public ResponseEntity<byte[]> statisticsPdf() { byte[] data = "%PDF-1.4\n% zycck statistics\n%%EOF".getBytes(StandardCharsets.US_ASCII); return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=zycck-statistics.pdf").contentType(MediaType.APPLICATION_PDF).body(data); }
}
