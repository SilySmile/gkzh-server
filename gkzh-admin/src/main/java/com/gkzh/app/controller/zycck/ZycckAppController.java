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
        result.put("questions", questionMapper.selectList(new QueryWrapper<com.gkzh.zycck.domain.ZycckCareerQuestion>().eq("status", "0").orderByAsc("category_id","question_id")));
        return AjaxResult.success(result);
    }

    @PostMapping("/records/enter")
    public AjaxResult enter(@RequestBody Map<String,Object> body) {
        StudentCheckin student = getCurrentStudent();
        return AjaxResult.success(recordService.enter(id(body.get("schoolId")), id(body.get("instanceId")), id(body.get("gameId")), student.getUserId()));
    }

    @PostMapping("/records/{id}/start")
    public AjaxResult start(@PathVariable Long id) { return AjaxResult.success(recordService.start(id, getCurrentStudent().getUserId())); }

    @GetMapping("/records/{id}")
    public AjaxResult record(@PathVariable Long id) { ZycckRecord record = recordService.get(id, getCurrentStudent().getUserId()); Map<String,Object> result = new java.util.LinkedHashMap<>(); result.put("record", record); result.put("currentQuestionNo", record.getCurrentQuestionNo()); result.put("stage", record.getStage()); result.put("status", record.getStatus()); if (record.getOptionSnapshotJson() != null) { try { java.util.List<?> questions = com.alibaba.fastjson2.JSON.parseArray(record.getOptionSnapshotJson()); int index = Math.max(0, (record.getCurrentQuestionNo() == null ? 1 : record.getCurrentQuestionNo()) - 1); if (index < questions.size()) result.put("question", questions.get(index)); } catch (Exception ignored) {} } return AjaxResult.success(result); }

    @PostMapping("/records/{id}/answers")
    public AjaxResult answer(@PathVariable Long id, @RequestBody Map<String,Object> body) { return AjaxResult.success(recordService.answer(id, getCurrentStudent().getUserId(), body)); }

    @PostMapping("/records/{id}/finish")
    public AjaxResult finish(@PathVariable Long id) { return AjaxResult.success(recordService.finish(id, getCurrentStudent().getUserId())); }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam Long recordId) {
        recordService.get(recordId, getCurrentStudent().getUserId());
        byte[] data = "%PDF-1.4\n% zycck report\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=zycck-report.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(data);
    }

    private static Long id(Object value) { return value == null ? null : Long.valueOf(String.valueOf(value)); }
}
