package com.gkzh.app.dto;

import com.gkzh.wjdc.domain.WjdcSurveyAnswer;

import java.util.List;

public class SubmitSurveyRequest {
    private Long surveyId;
    private Long activityId;
    private List<WjdcSurveyAnswer> answers;
    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }
    public List<WjdcSurveyAnswer> getAnswers() { return answers; }
    public void setAnswers(List<WjdcSurveyAnswer> answers) { this.answers = answers; }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }
}
