package com.gkzh.cyzs.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

import java.util.Date;

/**
 * 职场危机对象 biz_question
 * 
 * @author gkzh
 * @date 2025-10-13
 */
@Data
public class CyzsQuestion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题目内容 */
    @Excel(name = "题目内容")
    private String questionText;

    /** 问题类型 */
    @Excel(name = "类型", dictType = "cyzs_question_type")
    private String type;

    /** 选项A内容 */
    @Excel(name = "选项A")
    private String optionA;

    /** 选项B内容 */
    @Excel(name = "选项B")
    private String optionB;

    /** 选项C内容 */
    @Excel(name = "选项C")
    private String optionC;

    @Excel(name = "选项D")
    private String optionD;

    /** 正确选项的键 (A, B, C) */
    @Excel(name = "答案")
    private String correctOptionKey;

    /** 状态（0正常 1停用） */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
