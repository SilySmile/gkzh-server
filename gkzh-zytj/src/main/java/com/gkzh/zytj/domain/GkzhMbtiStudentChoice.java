package com.gkzh.zytj.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;
import com.gkzh.common.core.domain.BaseEntity;

/**
 * 职愿探究-学生选择记录对象 gkzh_mbti_student_choice
 * 
 * @author gkzh
 * @date 2026-06-02
 */
public class GkzhMbtiStudentChoice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 选择ID */
    private Long choiceId;

    /** 学生ID */
    @Excel(name = "学生ID")
    private Long studentId;

    /** 学生姓名（冗余字段，方便查询） */
    @Excel(name = "学生姓名", readConverterExp = "冗=余字段，方便查询")
    private String studentName;

    /** 学号（冗余字段，方便查询） */
    @Excel(name = "学号", readConverterExp = "冗=余字段，方便查询")
    private String studentNo;

    /** 活动ID */
    @Excel(name = "活动ID")
    private Long activityId;

    /** 生成的MBTI代码（如ESTJ） */
    @Excel(name = "生成的MBTI代码", readConverterExp = "如=ESTJ")
    private String choiceCode;

    /** 选择的商品ID（逗号分隔，如1,5,8,12） */
    @Excel(name = "选择的商品ID", readConverterExp = "逗=号分隔，如1,5,8,12")
    private String productIds;

    /** 用时（秒） */
    @Excel(name = "用时", readConverterExp = "秒=")
    private Long choiceTime;

    /** 是否已兑换盲盒：0=否 1=是 */
    @Excel(name = "是否已兑换盲盒：0=否 1=是")
    private String isRedeemed;

    /** 兑换时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "兑换时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date redeemTime;

    public void setChoiceId(Long choiceId) 
    {
        this.choiceId = choiceId;
    }

    public Long getChoiceId() 
    {
        return choiceId;
    }

    public void setStudentId(Long studentId) 
    {
        this.studentId = studentId;
    }

    public Long getStudentId() 
    {
        return studentId;
    }

    public void setStudentName(String studentName) 
    {
        this.studentName = studentName;
    }

    public String getStudentName() 
    {
        return studentName;
    }

    public void setStudentNo(String studentNo) 
    {
        this.studentNo = studentNo;
    }

    public String getStudentNo() 
    {
        return studentNo;
    }

    public void setActivityId(Long activityId) 
    {
        this.activityId = activityId;
    }

    public Long getActivityId() 
    {
        return activityId;
    }

    public void setChoiceCode(String choiceCode) 
    {
        this.choiceCode = choiceCode;
    }

    public String getChoiceCode() 
    {
        return choiceCode;
    }

    public void setProductIds(String productIds) 
    {
        this.productIds = productIds;
    }

    public String getProductIds() 
    {
        return productIds;
    }

    public void setChoiceTime(Long choiceTime) 
    {
        this.choiceTime = choiceTime;
    }

    public Long getChoiceTime() 
    {
        return choiceTime;
    }

    public void setIsRedeemed(String isRedeemed) 
    {
        this.isRedeemed = isRedeemed;
    }

    public String getIsRedeemed() 
    {
        return isRedeemed;
    }

    public void setRedeemTime(Date redeemTime) 
    {
        this.redeemTime = redeemTime;
    }

    public Date getRedeemTime() 
    {
        return redeemTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("choiceId", getChoiceId())
            .append("studentId", getStudentId())
            .append("studentName", getStudentName())
            .append("studentNo", getStudentNo())
            .append("activityId", getActivityId())
            .append("choiceCode", getChoiceCode())
            .append("productIds", getProductIds())
            .append("choiceTime", getChoiceTime())
            .append("isRedeemed", getIsRedeemed())
            .append("redeemTime", getRedeemTime())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .toString();
    }
}
