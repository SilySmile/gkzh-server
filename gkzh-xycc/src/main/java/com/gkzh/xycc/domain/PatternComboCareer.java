package com.gkzh.xycc.domain;

import com.gkzh.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

/**
 * 编码组合 - 职业方向 关联对象 xycc_pattern_combo_career
 * 
 * @author gkzh
 * @date 2025-06-15
 */
public class PatternComboCareer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 编码组合表ID */
    @Excel(name = "编码组合表ID")
    private Long comboId;

    /** 职业方向表ID */
    @Excel(name = "职业方向表ID")
    private Long careerId;

    public void setComboId(Long comboId)
    {
        this.comboId = comboId;
    }

    public Long getComboId() 
    {
        return comboId;
    }
    public void setCareerId(Long careerId) 
    {
        this.careerId = careerId;
    }

    public Long getCareerId() 
    {
        return careerId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("comboId", getComboId())
            .append("careerId", getCareerId())
            .toString();
    }
}
