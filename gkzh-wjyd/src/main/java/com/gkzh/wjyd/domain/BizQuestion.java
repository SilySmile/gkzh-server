package com.gkzh.wjyd.domain;

import com.gkzh.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

/**
 * 职场危机对象 biz_question
 * 
 * @author gkzh
 * @date 2025-10-13
 */
public class BizQuestion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID，自增 */
    private Long id;

    /** 题目内容 */
    @Excel(name = "题目内容")
    private String questionText;

    /** 题目图片URL */
    @Excel(name = "题目图片URL")
    private String questionImage;

    /** 选项A内容 */
    private String optionA;

    /** 选项B内容 */
    private String optionB;

    /** 选项C内容 */
    private String optionC;

    /** 正确选项的键 (A, B, C) */
    private String correctOptionKey;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setQuestionText(String questionText)
    {
        this.questionText = questionText;
    }

    public String getQuestionText()
    {
        return questionText;
    }

    public void setQuestionImage(String questionImage)
    {
        this.questionImage = questionImage;
    }

    public String getQuestionImage()
    {
        return questionImage;
    }

    public void setOptionA(String optionA)
    {
        this.optionA = optionA;
    }

    public String getOptionA()
    {
        return optionA;
    }

    public void setOptionB(String optionB)
    {
        this.optionB = optionB;
    }

    public String getOptionB()
    {
        return optionB;
    }

    public void setOptionC(String optionC)
    {
        this.optionC = optionC;
    }

    public String getOptionC()
    {
        return optionC;
    }

    public void setCorrectOptionKey(String correctOptionKey)
    {
        this.correctOptionKey = correctOptionKey;
    }

    public String getCorrectOptionKey()
    {
        return correctOptionKey;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("questionText", getQuestionText())
            .append("questionImage", getQuestionImage())
            .append("optionA", getOptionA())
            .append("optionB", getOptionB())
            .append("optionC", getOptionC())
            .append("correctOptionKey", getCorrectOptionKey())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
