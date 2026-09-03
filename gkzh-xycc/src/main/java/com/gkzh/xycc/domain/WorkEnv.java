package com.gkzh.xycc.domain;

import com.gkzh.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

/**
 * 工作环境偏好对象 xycc_work_env
 * 
 * @author gkzh
 * @date 2025-06-15
 */
public class WorkEnv extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long workEnvId;

    /** 环境名称，如”工程现场“ */
    @Excel(name = "工作环境偏好")
    private String title;

    public void setWorkEnvId(Long workEnvId) 
    {
        this.workEnvId = workEnvId;
    }

    public Long getWorkEnvId() 
    {
        return workEnvId;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("workEnvId", getWorkEnvId())
            .append("title", getTitle())
            .toString();
    }
}
