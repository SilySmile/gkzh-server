package com.gkzh.app.controller.zycck;

import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.service.ZycckRecordService;
import com.gkzh.zycck.mapper.ZycckCategoryMapper;
import com.gkzh.zycck.mapper.ZycckCareerQuestionMapper;
import com.gkzh.zycck.domain.ZycckCategory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/zycck")
public class ZycckAppController extends FrontBaseController {
    private final ZycckRecordService recordService;
    private final ZycckCategoryMapper categoryMapper;
    private final ZycckCareerQuestionMapper questionMapper;
    public ZycckAppController(ZycckRecordService recordService, ZycckCategoryMapper categoryMapper, ZycckCareerQuestionMapper questionMapper) { this.recordService = recordService; this.categoryMapper = categoryMapper; this.questionMapper = questionMapper; }

    @GetMapping("/catalog")
    public AjaxResult catalog() {
        Map<String,Object> result = new java.util.LinkedHashMap<>();
        result.put("categories", categoryMapper.selectList(new QueryWrapper<ZycckCategory>().eq("status", "0").orderByAsc("sort_order")));
        java.util.List<Map<String,Object>> careers = new java.util.ArrayList<>();
        java.util.List<Map<String,Object>> questions = new java.util.ArrayList<>();
        for (com.gkzh.zycck.domain.ZycckCareerQuestion q : questionMapper.selectList(new QueryWrapper<com.gkzh.zycck.domain.ZycckCareerQuestion>().eq("status", "0").orderByAsc("category_id","sort_order","career_question_id"))) {
            Map<String,Object> safe = new java.util.LinkedHashMap<>(); safe.put("careerQuestionId", q.getCareerQuestionId()); safe.put("careerId", q.getCareerQuestionId()); safe.put("categoryId", q.getCategoryId()); safe.put("careerName", q.getCareerName()); safe.put("hasQuestion", q.getHasQuestion()); safe.put("oneLineIntro", q.getOneLineIntro()); safe.put("mainWork", q.getMainWork()); safe.put("dayExample", q.getDayExample()); safe.put("whyExists", q.getWhyExists()); safe.put("careerImageUrl", q.getCareerImageUrl()); safe.put("questionImageUrl", q.getQuestionImageUrl()); safe.put("optionA", q.getOptionA()); safe.put("optionB", q.getOptionB()); safe.put("optionC", q.getOptionC()); safe.put("optionD", q.getOptionD()); safe.put("drawCandidate", q.getDrawCandidate()); careers.add(safe); if ("1".equals(q.getHasQuestion())) questions.add(safe);
        }
        result.put("careers", careers); result.put("questions", questions);
        return AjaxResult.success(result);
    }

    @PostMapping("/records/enter")
    public AjaxResult enter(@RequestBody Map<String,Object> body) {
        StudentCheckin student = getCurrentStudent();
        return AjaxResult.success(recordService.enter(id(body.get("schoolId")), id(body.get("instanceId")), id(body.get("gameId")), student.getUserId(), student.getStuId(), id(body.get("departmentId")), text(body.get("major")), text(body.get("gender"))));
    }

    @PostMapping("/records/{id}/start")
    public AjaxResult start(@PathVariable Long id) { return AjaxResult.success(recordService.start(id, getCurrentStudent().getUserId())); }

    @PostMapping("/records/{id}/question-start")
    public AjaxResult questionStart(@PathVariable Long id) { return AjaxResult.success(recordService.openQuestion(id, getCurrentStudent().getUserId())); }

    @GetMapping("/records/{id}")
    public AjaxResult record(@PathVariable Long id, @RequestParam(required = false) Long careerId) { ZycckRecord record = recordService.get(id, getCurrentStudent().getUserId()); Map<String,Object> result = new java.util.LinkedHashMap<>(); result.put("record", record); result.put("currentQuestionNo", record.getCurrentQuestionNo()); result.put("stage", record.getStage()); result.put("status", record.getStatus()); if (record.getOptionSnapshotJson() != null) { try { java.util.List<?> questions = com.alibaba.fastjson2.JSON.parseArray(record.getOptionSnapshotJson()); int index = Math.max(0, (record.getCurrentQuestionNo() == null ? 1 : record.getCurrentQuestionNo()) - 1); if (index < questions.size()) result.put("question", questions.get(index)); } catch (Exception ignored) {} } if (careerId != null) { com.gkzh.zycck.domain.ZycckCareerQuestion career = questionMapper.selectById(careerId); if (career != null) result.put("career", career); } return AjaxResult.success(result); }

    @PostMapping("/records/{id}/answers")
    public AjaxResult answer(@PathVariable Long id, @RequestBody Map<String,Object> body) { return AjaxResult.success(recordService.answer(id, getCurrentStudent().getUserId(), body)); }

    @PostMapping("/records/{id}/awareness")
    public AjaxResult awareness(@PathVariable Long id, @RequestBody Map<String,Object> body) { return AjaxResult.success(recordService.awareness(id, getCurrentStudent().getUserId(), body)); }

    @PostMapping("/records/{id}/browse")
    public AjaxResult browse(@PathVariable Long id, @RequestBody Map<String,Object> body) { return AjaxResult.success(recordService.browse(id, getCurrentStudent().getUserId(), id(body.get("careerId")))); }

    @GetMapping("/records/{id}/exploration")
    public AjaxResult exploration(@PathVariable Long id) { return AjaxResult.success(recordService.exploration(id, getCurrentStudent().getUserId())); }

    @PostMapping("/records/{id}/exploration-items")
    public AjaxResult addExploration(@PathVariable Long id, @RequestBody Map<String,Object> body) { return AjaxResult.success(recordService.updateExploration(id, getCurrentStudent().getUserId(), id(body.get("careerId")), false)); }

    @DeleteMapping("/records/{id}/exploration-items/{careerId}")
    public AjaxResult removeExploration(@PathVariable Long id, @PathVariable Long careerId) { return AjaxResult.success(recordService.updateExploration(id, getCurrentStudent().getUserId(), careerId, true)); }

    @PostMapping("/records/{id}/finish")
    public AjaxResult finish(@PathVariable Long id) { return AjaxResult.success(recordService.finish(id, getCurrentStudent().getUserId())); }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam Long recordId) {
        recordService.get(recordId, getCurrentStudent().getUserId());
        byte[] data = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << >> >>\nendobj\n4 0 obj\n<< /Length 0 >>\nstream\n\nendstream\nendobj\nxref\n0 5\n0000000000 65535 f \n0000000009 00000 n \n0000000058 00000 n \n0000000115 00000 n \n0000000252 00000 n \ntrailer\n<< /Size 5 /Root 1 0 R >>\nstartxref\n301\n%%EOF\n".getBytes(StandardCharsets.US_ASCII);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=zycck-report.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(data);
    }

    private static Long id(Object value) { return value == null ? null : Long.valueOf(String.valueOf(value)); }
    private static String text(Object value) { return value == null ? null : String.valueOf(value); }
}
