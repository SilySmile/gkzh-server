package com.gkzh.xycc.domain;

import com.gkzh.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

/**
 * 职业方向对象 xycc_career
 * 
 * @author gkzh
 * @date 2025-06-15
 */
public class Career extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long careerId;

    /** 职业名称，如”工程师“ */
    @Excel(name = "职业名称")
    private String title;

    public void setCareerId(Long careerId) 
    {
        this.careerId = careerId;
    }

    public Long getCareerId() 
    {
        return careerId;
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
            .append("careerId", getCareerId())
            .append("title", getTitle())
            .toString();
    }
}
