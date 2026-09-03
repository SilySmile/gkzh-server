package com.gkzh.wjdc.domain;

import com.gkzh.common.annotation.Excel;
import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 问卷选项对象 wjdc_survey_option
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@Data
public class WjdcSurveyOption extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 选项ID */
    @Excel(name = "选项ID", cellType = Excel.ColumnType.NUMERIC)
    private Integer id;

    /** 问题ID */
    @Excel(name = "问题ID")
    private Integer questionId;

    /** 选项内容 */
    @Excel(name = "选项内容")
    private String optionText;

    /** 显示排序 */
    @Excel(name = "显示排序")
    private Integer sortOrder;


} 