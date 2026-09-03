package com.gkzh.lottery.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gkzh.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

/**
 * 完成记录对象 lottery_task_log
 * 
 * @author gkzh
 * @date 2025-06-17
 */
public class LotteryTaskLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long logId;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名称 */
    @Excel(name = "用户名称")
    private String userName;

    /** 所属活动ID */
    @Excel(name = "所属活动ID")
    private Long activityId;

    /** 活动名称 */
    @Excel(name = "活动名称")
    private String activityTitle;

    /** 完成任务ID */
    @Excel(name = "完成任务ID")
    private Long taskId;

    /** 任务标题 */
    @Excel(name = "任务标题")
    private String taskTitle;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "完成时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date completedAt;

    /** 额外信息 */
    @Excel(name = "额外信息")
    private String extraData;

    public void setLogId(Long logId) 
    {
        this.logId = logId;
    }

    public Long getLogId() 
    {
        return logId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setUserName(String userName) 
    {
        this.userName = userName;
    }

    public String getUserName() 
    {
        return userName;
    }

    public void setActivityId(Long activityId) 
    {
        this.activityId = activityId;
    }

    public Long getActivityId() 
    {
        return activityId;
    }

    public void setActivityTitle(String activityTitle) 
    {
        this.activityTitle = activityTitle;
    }

    public String getActivityTitle() 
    {
        return activityTitle;
    }

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setTaskTitle(String taskTitle) 
    {
        this.taskTitle = taskTitle;
    }

    public String getTaskTitle() 
    {
        return taskTitle;
    }

    public void setCompletedAt(Date completedAt) 
    {
        this.completedAt = completedAt;
    }

    public Date getCompletedAt() 
    {
        return completedAt;
    }

    public void setExtraData(String extraData) 
    {
        this.extraData = extraData;
    }

    public String getExtraData() 
    {
        return extraData;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("activityId", getActivityId())
            .append("activityTitle", getActivityTitle())
            .append("taskId", getTaskId())
            .append("taskTitle", getTaskTitle())
            .append("completedAt", getCompletedAt())
            .append("extraData", getExtraData())
            .toString();
    }
}
