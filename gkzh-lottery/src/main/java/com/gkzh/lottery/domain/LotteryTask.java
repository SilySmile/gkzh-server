package com.gkzh.lottery.domain;

import com.gkzh.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

/**
 *  前置任务对象 lottery_task
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public class LotteryTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long taskId;

    /** 活动ID */
    private Long activityId;

    /** 活动名称 */
    @Excel(name = "活动名称")
    private String activityTitle;

    /** 步骤顺序（如：1=签到，2=心愿橱窗，3=问卷） */
    @Excel(name = "步骤顺序", readConverterExp = "如=：1=签到，2=心愿橱窗，3=问卷")
    private Long stepOrder;

    /** 任务标识（如：sign_in, wish, survey） */
    @Excel(name = "任务标识", readConverterExp = "如=：sign_in,,w=ish,,s=urvey")
    private String taskKey;


    /** 步骤标题（前端展示） */
    @Excel(name = "步骤标题", readConverterExp = "前=端展示")
    private String title;

    /** 步骤说明 */
    @Excel(name = "步骤说明")
    private String description;

    /** 是否必须完成（0否1是） */
    @Excel(name = "是否必须完成", readConverterExp = "0=否1是")
    private Integer isRequired;

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setActivityId(Long activityId) 
    {
        this.activityId = activityId;
    }

    public Long getActivityId() 
    {
        return activityId;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setStepOrder(Long stepOrder) 
    {
        this.stepOrder = stepOrder;
    }

    public Long getStepOrder() 
    {
        return stepOrder;
    }

    public void setTaskKey(String taskKey) 
    {
        this.taskKey = taskKey;
    }

    public String getTaskKey() 
    {
        return taskKey;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setIsRequired(Integer isRequired) 
    {
        this.isRequired = isRequired;
    }

    public Integer getIsRequired() 
    {
        return isRequired;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("activityId", getActivityId())
            .append("activityTitle", getActivityTitle())
            .append("stepOrder", getStepOrder())
            .append("taskKey", getTaskKey())
            .append("title", getTitle())
            .append("description", getDescription())
            .append("isRequired", getIsRequired())
            .toString();
    }
}
