package com.gkzh.zcyltzj.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/** 骰子局点数试题实体：每个骰子点数拥有一套固定试题。 */
@Data
@TableName("gkzh_zcyltzj_question")
public class ZcyltzjQuestion {
    /** 试题编号。 */
    @TableId(value = "question_id", type = IdType.AUTO)
    private Long questionId;
    /** 所属骰子点数：1-6。 */
    private Integer pointNo;
    /** 点数内题号，从 1 开始。 */
    private Integer questionNo;
    /** 题干。 */
    private String content;
    /** A 选项文字。 */
    private String optionA;
    /** B 选项文字。 */
    private String optionB;
    /** C 选项文字。 */
    private String optionC;
    /** D 选项文字。 */
    private String optionD;
    /** 显示顺序，数值越小越靠前。 */
    private Integer sortOrder;
    /** 状态：0启用，1停用。 */
    private String status;
    /** 创建时间。 */
    private Date createTime;
    /** 最后更新时间。 */
    private Date updateTime;
}
