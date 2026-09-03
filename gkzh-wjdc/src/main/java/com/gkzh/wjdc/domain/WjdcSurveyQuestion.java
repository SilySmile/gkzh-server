package com.gkzh.wjdc.domain;

import java.util.List;
import com.gkzh.common.annotation.Excel;
import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 问卷问题对象 wjdc_survey_question
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@Data
public class WjdcSurveyQuestion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 问题ID */
    @Excel(name = "问题ID", cellType = Excel.ColumnType.NUMERIC)
    private Integer id;

    /** 问卷ID */
    @Excel(name = "问卷ID")
    private Integer surveyId;

    /** 问题标题 */
    @Excel(name = "问题标题")
    private String questionTitle;

    /** 问题类型 1=单选题 2=多选题 3=填空题 */
    @Excel(name = "问题类型", readConverterExp = "1=单选题,2=多选题,3=填空题")
    private String questionType;

    /** 是否必填 0=否, 1=是 */
    @Excel(name = "是否必填", readConverterExp = "0=否,1=是")
    private String required;

    /** 显示排序 */
    @Excel(name = "显示排序")
    private Integer sortOrder;

    /** 选项列表 */
    private List<WjdcSurveyOption> options;
    /* 分值范围 */
    private String scoreRange;

    private Boolean enableOptionScoring; // 是否开启选项打分，默认 false
}
