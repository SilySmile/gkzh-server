package com.gkzh.wjdc.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gkzh.common.annotation.Excel;
import com.gkzh.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户答题对象 wjdc_survey_answer
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public class WjdcSurveyAnswer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 答题ID */
    private Long answerId;

    /** 答卷ID */
    @Excel(name = "答卷ID")
    private Long responseId;

    /** 问题ID */
    @Excel(name = "问题ID")
    private Long questionId;

    /** 用户答案 */
    @Excel(name = "用户答案")
    private String answerText;

    /** 问题标题 */
    @Excel(name = "问题标题")
    private String questionTitle;

    /** 问题类型 */
    @Excel(name = "问题类型", readConverterExp = "1=单选题,2=多选题,3=填空题")
    private String questionType;

    /** 选项内容（用于显示） */
    private String optionText;

    public void setAnswerId(Long answerId) 
    {
        this.answerId = answerId;
    }

    public Long getAnswerId() 
    {
        return answerId;
    }
    public void setResponseId(Long responseId) 
    {
        this.responseId = responseId;
    }

    public Long getResponseId() 
    {
        return responseId;
    }
    public void setQuestionId(Long questionId) 
    {
        this.questionId = questionId;
    }

    public Long getQuestionId() 
    {
        return questionId;
    }
    public void setAnswerText(String answerText) 
    {
        this.answerText = answerText;
    }

    public String getAnswerText() 
    {
        return answerText;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("answerId", getAnswerId())
            .append("responseId", getResponseId())
            .append("questionId", getQuestionId())
            .append("answerText", getAnswerText())
            .append("questionTitle", getQuestionTitle())
            .append("questionType", getQuestionType())
            .toString();
    }
} 