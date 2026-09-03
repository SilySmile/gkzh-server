package com.gkzh.activity.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gkzh.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

/**
 * 活动参与记录对象 gkzh_activity_participation_record
 * 
 * @author gkzh
 * @date 2025-06-27
 */
public class GkzhActivityParticipationRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 活动ID */
    @Excel(name = "活动ID")
    private Long activityId;

    /** 环节ID */
    @Excel(name = "环节ID")
    private Long moduleId;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户姓名 */
    @Excel(name = "用户姓名")
    private String userName;

    /** 用户学号/工号 */
    @Excel(name = "用户学号/工号")
    private String userCode;

    /** 参与类型（1-签到，2-签退，3-抽奖，4-心愿，5-问卷） */
    @Excel(name = "参与类型", readConverterExp = "1=-签到，2-签退，3-抽奖，4-心愿，5-问卷")
    private Integer participationType;

    /** 参与时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "参与时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date participationTime;

    /** 参与结果 */
    @Excel(name = "参与结果")
    private String result;

    /** 状态（0-无效，1-有效） */
    @Excel(name = "状态", readConverterExp = "0=-无效，1-有效")
    private Integer status;

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public Long getRecordId() 
    {
        return recordId;
    }

    public void setActivityId(Long activityId) 
    {
        this.activityId = activityId;
    }

    public Long getActivityId() 
    {
        return activityId;
    }

    public void setModuleId(Long moduleId) 
    {
        this.moduleId = moduleId;
    }

    public Long getModuleId() 
    {
        return moduleId;
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

    public void setUserCode(String userCode) 
    {
        this.userCode = userCode;
    }

    public String getUserCode() 
    {
        return userCode;
    }

    public void setParticipationType(Integer participationType) 
    {
        this.participationType = participationType;
    }

    public Integer getParticipationType() 
    {
        return participationType;
    }

    public void setParticipationTime(Date participationTime) 
    {
        this.participationTime = participationTime;
    }

    public Date getParticipationTime() 
    {
        return participationTime;
    }

    public void setResult(String result) 
    {
        this.result = result;
    }

    public String getResult() 
    {
        return result;
    }

    public void setStatus(Integer status) 
    {
        this.status = status;
    }

    public Integer getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("activityId", getActivityId())
            .append("moduleId", getModuleId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("userCode", getUserCode())
            .append("participationType", getParticipationType())
            .append("participationTime", getParticipationTime())
            .append("result", getResult())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
