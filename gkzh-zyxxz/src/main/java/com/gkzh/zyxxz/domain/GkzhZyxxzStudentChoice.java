package com.gkzh.zyxxz.domain;

import com.gkzh.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 职业信息站-学生选择记录对象 gkzh_zyxxz_student_choice
 * 
 * @author gkzh
 * @date 2026-06-04
 */
public class GkzhZyxxzStudentChoice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 选择ID */
    private Long choiceId;

    /** 学生ID */
    @Excel(name = "学生ID")
    private Long studentId;

    /** 学生姓名 */
    @Excel(name = "学生姓名")
    private String studentName;

    /** 学号 */
    @Excel(name = "学号")
    private String studentNo;

    /** 活动ID */
    @Excel(name = "活动ID")
    private Long activityId;

    /** 最常用求职信息渠道 */
    @Excel(name = "最常用求职信息渠道")
    private String commonChannel;

    /** 最信任求职信息渠道 */
    @Excel(name = "最信任求职信息渠道")
    private String trustedChannel;

    /** 获取障碍（逗号分隔） */
    @Excel(name = "获取障碍", readConverterExp = "逗=号分隔")
    private String obstacles;

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

    public void setCommonChannel(String commonChannel) 
    {
        this.commonChannel = commonChannel;
    }

    public String getCommonChannel() 
    {
        return commonChannel;
    }

    public void setTrustedChannel(String trustedChannel) 
    {
        this.trustedChannel = trustedChannel;
    }

    public String getTrustedChannel() 
    {
        return trustedChannel;
    }

    public void setObstacles(String obstacles) 
    {
        this.obstacles = obstacles;
    }

    public String getObstacles() 
    {
        return obstacles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("choiceId", getChoiceId())
            .append("studentId", getStudentId())
            .append("studentName", getStudentName())
            .append("studentNo", getStudentNo())
            .append("activityId", getActivityId())
            .append("commonChannel", getCommonChannel())
            .append("trustedChannel", getTrustedChannel())
            .append("obstacles", getObstacles())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .toString();
    }
    
}
