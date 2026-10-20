package com.gkzh.app.controller.zycck;

import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.service.ZycckRecordService;
import com.gkzh.zycck.service.ZycckActivityFeedbackService;
import com.gkzh.zycck.dto.ZycckActivityFeedbackRequest;
import com.gkzh.zycck.mapper.ZycckCategoryMapper;
import com.gkzh.zycck.mapper.ZycckCareerQuestionMapper;
import com.gkzh.zycck.domain.ZycckCategory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.gkzh.app.service.ZycckReportPdfService;
import com.gkzh.app.service.ZycckCatalogCacheService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/zycck")
public class ZycckAppController extends FrontBaseController {
    private final ZycckRecordService recordService;
    private final ZycckCategoryMapper categoryMapper;
    private final ZycckCareerQuestionMapper questionMapper;
    private final ZycckReportPdfService reportPdfService;
    private final ZycckCatalogCacheService catalogCacheService;
    private final ZycckActivityFeedbackService activityFeedbackService;
    /** 学生资料以服务端数据库为准，不能依赖小程序提交的专业和院系字段。 */
    private final GkzhStudentMapper studentMapper;

    public ZycckAppController(ZycckRecordService recordService, ZycckCategoryMapper categoryMapper, ZycckCareerQuestionMapper questionMapper, ZycckReportPdfService reportPdfService, ZycckCatalogCacheService catalogCacheService, ZycckActivityFeedbackService activityFeedbackService, GkzhStudentMapper studentMapper) {
        this.recordService = recordService;
        this.categoryMapper = categoryMapper;
        this.questionMapper = questionMapper;
        this.reportPdfService = reportPdfService;
        this.catalogCacheService = catalogCacheService;
        this.activityFeedbackService = activityFeedbackService;
        this.studentMapper = studentMapper;
    }

    @GetMapping("/catalog")
    public AjaxResult catalog() {
        return AjaxResult.success(catalogCacheService.getCatalog());
    }

    @PostMapping("/records/enter")
    public AjaxResult enter(@RequestBody Map<String, Object> body) {
        StudentCheckin student = getCurrentStudent();
        Long schoolId = id(body.get("schoolId"));
        GkzhStudent profile = schoolId == null || student == null || student.getStuId() == null ? null
                : studentMapper.selectGkzhStudentByStudentId(student.getStuId());
        if (profile == null || !schoolId.equals(profile.getSchoolId()) || student.getUserId() == null
                || !student.getUserId().equals(profile.getUserId())) {
            throw new ServiceException("学生档案与当前学校不一致，请重新登录后扫码");
        }
        // 学生表的 department_id 是专业（二级部门）ID；院系是该专业的父部门。
        // 专业名称已由学生查询关联部门表给出，忽略客户端传来的 departmentId/major，避免空值或伪造。
        return AjaxResult.success(recordService.enter(schoolId, id(body.get("instanceId")), id(body.get("gameId")),
                student.getUserId(), profile.getStudentId(), profile.getDepartmentId(), profile.getDepartmentName(), profile.getGender()));
    }

    @PostMapping("/records/{id}/start")
    public AjaxResult start(@PathVariable Long id) {
        return AjaxResult.success(recordService.start(id, getCurrentStudent().getUserId()));
    }

    @PostMapping("/records/{id}/question-start")
    public AjaxResult questionStart(@PathVariable Long id) {
        return AjaxResult.success(recordService.openQuestion(id, getCurrentStudent().getUserId()));
    }

    @GetMapping("/records/{id}")
    public AjaxResult record(@PathVariable Long id, @RequestParam(required = false) Long careerId) {
        ZycckRecord record = recordService.get(id, getCurrentStudent().getUserId());
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("record", record);
        result.put("currentQuestionNo", record.getCurrentQuestionNo());
        result.put("stage", record.getStage());
        result.put("status", record.getStatus());
        if (record.getOptionSnapshotJson() != null) {
            try {
                java.util.List<?> questions = com.alibaba.fastjson2.JSON.parseArray(record.getOptionSnapshotJson());
                int index = Math.max(0, (record.getCurrentQuestionNo() == null ? 1 : record.getCurrentQuestionNo()) - 1);
                if (index < questions.size()) result.put("question", questions.get(index));
            } catch (Exception ignored) {
            }
        }
        Map<String, Object> feedback = recordService.feedbackView(record);
        if (!feedback.isEmpty()) result.putAll(feedback);
        if (careerId != null) {
            com.gkzh.zycck.domain.ZycckCareerQuestion career = questionMapper.selectById(careerId);
            if (career != null) result.put("career", career);
        }
        return AjaxResult.success(result);
    }

    @PostMapping("/records/{id}/answers")
    public AjaxResult answer(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(recordService.answer(id, getCurrentStudent().getUserId(), body));
    }

    @PostMapping("/records/{id}/awareness")
    public AjaxResult awareness(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(recordService.awareness(id, getCurrentStudent().getUserId(), body));
    }

    @PostMapping("/records/{id}/browse")
    public AjaxResult browse(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(recordService.browse(id, getCurrentStudent().getUserId(), id(body.get("careerId"))));
    }

    @GetMapping("/records/{id}/exploration")
    public AjaxResult exploration(@PathVariable Long id) {
        return AjaxResult.success(recordService.exploration(id, getCurrentStudent().getUserId()));
    }

    @PostMapping("/records/{id}/exploration-items")
    public AjaxResult addExploration(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(recordService.updateExploration(id, getCurrentStudent().getUserId(), id(body.get("careerId")), false));
    }

    @DeleteMapping("/records/{id}/exploration-items/{careerId}")
    public AjaxResult removeExploration(@PathVariable Long id, @PathVariable Long careerId) {
        return AjaxResult.success(recordService.updateExploration(id, getCurrentStudent().getUserId(), careerId, true));
    }

    @PostMapping("/records/{id}/finish")
    public AjaxResult finish(@PathVariable Long id) {
        return AjaxResult.success(recordService.finish(id, getCurrentStudent().getUserId()));
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam Long recordId) throws IOException {
        byte[] data = reportPdfService.create(recordId, getCurrentStudent().getUserId());
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=zycck-report.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(data);
    }

    /** 获取当前活动游戏的问卷、适用题目及本人已经保存的反馈。 */
    @GetMapping("/records/{id}/activity-feedback")
    public AjaxResult activityFeedback(@PathVariable Long id, @RequestParam Long instanceId, @RequestParam Long gameId) {
        return AjaxResult.success(activityFeedbackService.get(id, getCurrentStudent().getUserId(), instanceId, gameId));
    }

    /** 清单确认后保存活动反馈并结束游戏，答案和结束状态在同一事务提交。 */
    @PostMapping("/records/{id}/activity-feedback")
    public AjaxResult saveActivityFeedback(@PathVariable Long id, @RequestParam Long instanceId,
                                           @RequestParam Long gameId, @RequestBody ZycckActivityFeedbackRequest body) {
        return AjaxResult.success(activityFeedbackService.save(id, getCurrentStudent().getUserId(), instanceId, gameId, body));
    }

    private static Long id(Object value) {
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

}
