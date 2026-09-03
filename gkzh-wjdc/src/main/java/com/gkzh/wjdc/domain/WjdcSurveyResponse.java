package com.gkzh.wjdc.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gkzh.common.annotation.Excel;
import com.gkzh.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户答卷对象 wjdc_survey_response
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public class WjdcSurveyResponse extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 答卷ID */
    private Long responseId;

    /** 问卷ID */
    @Excel(name = "问卷ID")
    private Long surveyId;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submittedAt;

    /** 问卷标题 */
    @Excel(name = "问卷标题")
    private String surveyTitle;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** 答案列表 */
    private java.util.List<WjdcSurveyAnswer> answers;

    public void setResponseId(Long responseId) 
    {
        this.responseId = responseId;
    }

    public Long getResponseId() 
    {
        return responseId;
    }
    public void setSurveyId(Long surveyId) 
    {
        this.surveyId = surveyId;
    }

    public Long getSurveyId() 
    {
        return surveyId;
    }
    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }
    public void setSubmittedAt(Date submittedAt) 
    {
        this.submittedAt = submittedAt;
    }

    public Date getSubmittedAt() 
    {
        return submittedAt;
    }

    public String getSurveyTitle() {
        return surveyTitle;
    }

    public void setSurveyTitle(String surveyTitle) {
        this.surveyTitle = surveyTitle;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public java.util.List<WjdcSurveyAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(java.util.List<WjdcSurveyAnswer> answers) {
        this.answers = answers;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("responseId", getResponseId())
            .append("surveyId", getSurveyId())
            .append("userId", getUserId())
            .append("submittedAt", getSubmittedAt())
            .append("surveyTitle", getSurveyTitle())
            .append("userName", getUserName())
            .toString();
    }
} 