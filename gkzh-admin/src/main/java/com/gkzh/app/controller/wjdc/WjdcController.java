package com.gkzh.app.controller.wjdc;

import com.gkzh.app.dto.SubmitSurveyRequest;
import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.wjdc.domain.WjdcSurvey;
import com.gkzh.wjdc.domain.WjdcSurveyResponse;
import com.gkzh.wjdc.service.IWjdcSurveyResponseService;
import com.gkzh.wjdc.service.IWjdcSurveyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api("问卷调查")
@RestController
@RequestMapping("/api/wjdc")
public class WjdcController extends FrontBaseController
{
    @Autowired
    private IWjdcSurveyService wjdcSurveyService;

    @Autowired
    private IWjdcSurveyResponseService wjdcSurveyResponseService;

    /**
     * 获取问卷详情（含题目和选项）
     */
    @ApiOperation("获取问卷详情")
    @GetMapping("/detail/{surveyId}")
    public AjaxResult getSurveyDetail(@PathVariable Long surveyId) {
        WjdcSurvey survey = wjdcSurveyService.selectWjdcSurveyWithQuestions(surveyId);
        if (survey == null) {
            return AjaxResult.error("问卷不存在");
        }
        return AjaxResult.success(survey);
    }

    /**
     * 提交答卷
     */
    @ApiOperation("提交答案")
    @PostMapping("/submit")
    public AjaxResult submitSurvey(@RequestBody SubmitSurveyRequest req) {
        StudentCheckin checkin = getCurrentStudent();
        // 检查是否已提交过
        boolean already = wjdcSurveyResponseService.hasUserSubmitted(req.getSurveyId(), checkin.getUserId());
        if (already) {
            return AjaxResult.error("您已提交过本问卷");
        }
        // 保存答卷
        WjdcSurveyResponse response = new WjdcSurveyResponse();
        response.setSurveyId(req.getSurveyId());
        response.setUserId(checkin.getUserId());
        response.setUserName(checkin.getStuName());
        response.setSubmittedAt(DateUtils.getNowDate());
        response.setAnswers(req.getAnswers());
        wjdcSurveyResponseService.saveSurveyResponse(checkin,req.getActivityId(),response);
        return AjaxResult.success("提交成功");
    }

}
