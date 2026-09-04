package com.gkzh.web.controller.zycck;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.zycck.domain.ZycckCareerQuestion;
import com.gkzh.zycck.domain.ZycckCategory;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.mapper.ZycckCareerQuestionMapper;
import com.gkzh.zycck.mapper.ZycckCategoryMapper;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
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

    public ZycckAdminController(ZycckCategoryMapper categoryMapper, ZycckCareerQuestionMapper questionMapper, ZycckRecordMapper recordMapper) {
        this.categoryMapper = categoryMapper; this.questionMapper = questionMapper; this.recordMapper = recordMapper;
    }

    @GetMapping("/categories")
    public TableDataInfo categories() {
        startPage();
        java.util.List<Map<String,Object>> rows = new java.util.ArrayList<>();
        for (ZycckCategory category : categoryMapper.selectList(new QueryWrapper<ZycckCategory>().orderByAsc("sort_order"))) {
            Map<String,Object> row = new LinkedHashMap<>();
            row.put("categoryId", category.getCategoryId()); row.put("code", category.getCode()); row.put("name", category.getName());
            row.put("description", category.getDescription()); row.put("drawMode", category.getDrawMode()); row.put("sortOrder", category.getSortOrder()); row.put("status", category.getStatus());
            QueryWrapper<ZycckCareerQuestion> all = new QueryWrapper<ZycckCareerQuestion>().eq("category_id", category.getCategoryId());
            QueryWrapper<ZycckCareerQuestion> candidates = new QueryWrapper<ZycckCareerQuestion>().eq("category_id", category.getCategoryId()).eq("draw_candidate", "1");
            row.put("questionCount", questionMapper.selectCount(all)); row.put("candidateCount", questionMapper.selectCount(candidates)); rows.add(row);
        }
        return getDataTable(rows);
    }

    @PostMapping("/categories")
    public AjaxResult saveCategory(@RequestBody ZycckCategory category) { return toAjax(category.getCategoryId() == null ? categoryMapper.insert(category) : categoryMapper.updateById(category)); }

    @DeleteMapping("/categories/{id}")
    public AjaxResult deleteCategory(@PathVariable Long id) { return toAjax(categoryMapper.deleteById(id)); }

    @GetMapping("/career-questions")
    public TableDataInfo questions(@RequestParam(required = false) Long categoryId) { startPage(); QueryWrapper<ZycckCareerQuestion> q = new QueryWrapper<ZycckCareerQuestion>().orderByAsc("category_id", "career_question_id"); if (categoryId != null) q.eq("category_id", categoryId); return getDataTable(questionMapper.selectList(q)); }

    @PostMapping("/career-questions")
    public AjaxResult saveQuestion(@RequestBody ZycckCareerQuestion question) { return toAjax(question.getCareerQuestionId() == null ? questionMapper.insert(question) : questionMapper.updateById(question)); }

    @DeleteMapping("/career-questions/{id}")
    public AjaxResult deleteQuestion(@PathVariable Long id) { return toAjax(questionMapper.deleteById(id)); }

    @GetMapping("/records")
    public TableDataInfo records(@RequestParam(required = false) Long instanceId, @RequestParam(required = false) Long gameId, @RequestParam(required = false) Long schoolId, @RequestParam(required = false) Long departmentId, @RequestParam(required = false) String major, @RequestParam(required = false) String gender) {
        startPage(); QueryWrapper<ZycckRecord> q = new QueryWrapper<ZycckRecord>().orderByDesc("record_id"); if (instanceId != null) q.eq("instance_id", instanceId); if (gameId != null) q.eq("game_id", gameId); if (schoolId != null) q.eq("school_id", schoolId); if (departmentId != null) q.eq("department_id", departmentId); if (major != null && !major.trim().isEmpty()) q.like("major", major.trim()); if (gender != null && !gender.trim().isEmpty()) q.eq("gender", gender.trim()); return getDataTable(recordMapper.selectList(q));
    }

    @GetMapping("/statistics")
    public AjaxResult statistics(@RequestParam(required = false) Long instanceId, @RequestParam(required = false) Long gameId, @RequestParam(required = false) Long schoolId, @RequestParam(required = false) Long departmentId, @RequestParam(required = false) String major, @RequestParam(required = false) String gender) {
        QueryWrapper<ZycckRecord> q = new QueryWrapper<>(); if (instanceId != null) q.eq("instance_id", instanceId); if (gameId != null) q.eq("game_id", gameId); if (schoolId != null) q.eq("school_id", schoolId); if (departmentId != null) q.eq("department_id", departmentId); if (major != null && !major.trim().isEmpty()) q.like("major", major.trim()); if (gender != null && !gender.trim().isEmpty()) q.eq("gender", gender.trim());
        java.util.List<ZycckRecord> rows = recordMapper.selectList(q); Map<String,Object> result = new LinkedHashMap<>(); result.put("participating", rows.size()); result.put("finished", rows.stream().filter(x -> "finished".equals(x.getStatus())).count()); result.put("records", rows); return AjaxResult.success(result);
    }

    @GetMapping("/statistics/pdf")
    public ResponseEntity<byte[]> statisticsPdf() { byte[] data = "%PDF-1.4\n% zycck statistics\n%%EOF".getBytes(StandardCharsets.US_ASCII); return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=zycck-statistics.pdf").contentType(MediaType.APPLICATION_PDF).body(data); }
}
