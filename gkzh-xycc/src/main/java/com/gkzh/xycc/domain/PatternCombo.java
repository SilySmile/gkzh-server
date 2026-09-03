package com.gkzh.xycc.domain;

import java.util.List;

import com.gkzh.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

/**
 * 编码组合对象 xycc_pattern_combo
 * 
 * @author gkzh
 * @date 2025-06-15
 */
public class PatternCombo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long patternComboId;

    /** 组合编码，如"RIA" */
    @Excel(name = "组合编码")
    private String code;

    /** 编码组合 - 职业方向 关联信息 */
    private List<Long> careerIds;

    /** 工作环境偏好 关联信息*/
    private List<Long> envIds;

    /** 职业方向名称列表 */
    @Excel(name = "职业方向")
    private String careerTitles;

    /** 工作环境偏好名称列表 */
    @Excel(name = "工作环境偏好")
    private String envTitles;

    public String getCareerTitles() {
        return careerTitles;
    }

    public void setCareerTitles(String careerTitles) {
        this.careerTitles = careerTitles;
    }

    public String getEnvTitles() {
        return envTitles;
    }

    public void setEnvTitles(String envTitles) {
        this.envTitles = envTitles;
    }

    public List<Long> getEnvIds() {
        return envIds;
    }

    public void setEnvIds(List<Long> envIds) {
        this.envIds = envIds;
    }

    public void setPatternComboId(Long patternComboId)
    {
        this.patternComboId = patternComboId;
    }

    public Long getPatternComboId() 
    {
        return patternComboId;
    }

    public void setCode(String code) 
    {
        this.code = code;
    }

    public String getCode() 
    {
        return code;
    }

    public List<Long> getCareerIds() {
        return careerIds;
    }

    public void setCareerIds(List<Long> careerIds) {
        this.careerIds = careerIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("patternComboId", getPatternComboId())
            .append("code", getCode())
            .append("careerIds", getCareerIds())
            .append("envIds", getEnvIds())
            .append("careerTitles", getCareerTitles())
            .append("envTitles", getEnvTitles())
            .toString();
    }
}
