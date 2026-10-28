package com.gkzh.nlld.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 能量雷达学生选择明细实体。
 * 每位完成测评的学生固定 12 行（6 关 × 每关 2 件物品），作为计分和统计的事实来源。
 */
@Data
@TableName("gkzh_nlld_answer")
public class NlldAnswer {

    /** 选择明细编号。 */
    @TableId(value = "answer_id", type = IdType.AUTO)
    private Long answerId;

    /** 参与记录编号。 */
    private Long recordId;

    /** 第几关，取值 1-6。 */
    private Integer roundNo;

    /** 物品编号。 */
    private Long optionId;

    /** 选项字母快照：A-E。 */
    private String optionLabel;

    /** 物品名称快照。 */
    private String optionName;

    /** 维度编号快照。 */
    private Long dimensionId;

    /** 维度编码快照。 */
    private String dimensionCode;

    /** 提交时间。 */
    private Date createTime;
}
