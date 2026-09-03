package com.gkzh.app.controller.wjyd;

import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.wjyd.service.IBizQuestionService;
import com.gkzh.wjyd.vo.AnswerResult;
import com.gkzh.wjyd.vo.QuestionVO;
import com.gkzh.wjyd.vo.SubmitAnswerRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api("危机应对")
@RestController
@RequestMapping("/api/wjyd")
public class WjydController extends FrontBaseController {
    @Autowired
    private IBizQuestionService questionService;

    /**
     * 获取随机题目
     */
    @ApiOperation("获取题目")
    @GetMapping("/questions")
    public AjaxResult getRandomQuestions() {
        try {
            List<QuestionVO> questions = questionService.getRandomQuestions(3);
            return AjaxResult.success(questions);
        } catch (Exception e) {
            return AjaxResult.error("获取题目失败：" + e.getMessage());
        }
    }

    /**
     * 提交答案
     */
    @PostMapping("/submit/{activityId}")
    public AjaxResult submitAnswers(@Valid @RequestBody SubmitAnswerRequest request, @PathVariable Long activityId) {
        try {
            // 获取当前登录用户
            StudentCheckin currentStudent = getCurrentStudent();
            AnswerResult result = questionService.submitAnswers(currentStudent, request, activityId);
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("提交答案失败：" + e.getMessage());
        }
    }


}
