package com.gkzh.wjdc.domain;

import com.gkzh.common.annotation.Excel;
import com.gkzh.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 问卷统计对象
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public class WjdcSurveyStatistics extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 问卷ID */
    private Long surveyId;

    /** 问卷标题 */
    @Excel(name = "问卷标题")
    private String surveyTitle;

    /** 答卷总数 */
    @Excel(name = "答卷总数")
    private Integer totalResponses;

    /** 问题ID */
    private Long questionId;

    /** 问题标题 */
    @Excel(name = "问题标题")
    private String questionTitle;

    /** 问题类型 */
    @Excel(name = "问题类型", readConverterExp = "1=单选题,2=多选题,3=填空题,4=打分题,5=选项打分题")
    private String questionType;

    /** 选项ID */
    private Long optionId;

    /** 选项内容 */
    @Excel(name = "选项内容")
    private String optionText;

    /** 选择次数 */
    @Excel(name = "选择次数")
    private Integer selectCount;

    /** 选择比例 */
    @Excel(name = "选择比例")
    private String selectPercentage;

    /** 答案内容（填空题） */
    @Excel(name = "答案内容")
    private String answerText;

    /** 答案出现次数（填空题） */
    @Excel(name = "出现次数")
    private Integer answerCount;
    
    // 为了支持选项打分题的平均分和标准差，我们添加两个新的字段
    /** 平均分（选项打分题专用） */
    private Double averageScore;
    
    /** 标准差（选项打分题专用） */
    private Double standardDeviation;

    public Long getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(Long surveyId) {
        this.surveyId = surveyId;
    }

    public String getSurveyTitle() {
        return surveyTitle;
    }

    public void setSurveyTitle(String surveyTitle) {
        this.surveyTitle = surveyTitle;
    }

    public Integer getTotalResponses() {
        return totalResponses;
    }

    public void setTotalResponses(Integer totalResponses) {
        this.totalResponses = totalResponses;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
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

    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Integer getSelectCount() {
        return selectCount;
    }

    public void setSelectCount(Integer selectCount) {
        this.selectCount = selectCount;
    }

    public String getSelectPercentage() {
        return selectPercentage;
    }

    public void setSelectPercentage(String selectPercentage) {
        this.selectPercentage = selectPercentage;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public Integer getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(Integer answerCount) {
        this.answerCount = answerCount;
    }
    
    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(Double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("surveyId", getSurveyId())
            .append("surveyTitle", getSurveyTitle())
            .append("totalResponses", getTotalResponses())
            .append("questionId", getQuestionId())
            .append("questionTitle", getQuestionTitle())
            .append("questionType", getQuestionType())
            .append("optionId", getOptionId())
            .append("optionText", getOptionText())
            .append("selectCount", getSelectCount())
            .append("selectPercentage", getSelectPercentage())
            .append("answerText", getAnswerText())
            .append("answerCount", getAnswerCount())
            .append("averageScore", getAverageScore())
            .append("standardDeviation", getStandardDeviation())
            .toString();
    }
}