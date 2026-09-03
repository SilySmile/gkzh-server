package com.gkzh.xycc.domain;

import com.gkzh.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

/**
 * 编码组合 - 工作环境偏好 关联对象 xycc_pattern_combo_env
 * 
 * @author gkzh
 * @date 2025-06-15
 */
public class PatternComboEnv extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 编码组合表ID */
    private Long comboId;

    /** 工作环境偏好表ID */
    private Long envId;

    public void setComboId(Long comboId) 
    {
        this.comboId = comboId;
    }

    public Long getComboId() 
    {
        return comboId;
    }
    public void setEnvId(Long envId) 
    {
        this.envId = envId;
    }

    public Long getEnvId() 
    {
        return envId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("comboId", getComboId())
            .append("envId", getEnvId())
            .toString();
    }
}
