package com.gkzh.zyfxq.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职业飞行棋题目选项实体。
 * 每题固定 A-D 四个选项，is_correct 为 1 的选项有且只有一个。
 */
@Data
@TableName("gkzh_zyfxq_option")
public class ZyfxqOption {

    /** 选项编号。 */
    @TableId(value = "option_id", type = IdType.AUTO)
    private Long optionId;

    /** 所属题目编号。 */
    private Long questionId;

    /** 选项字母：A、B、C、D。 */
    private String optionLabel;

    /** 选项内容。 */
    private String optionContent;

    /** 是否正确答案：1 是，0 否。 */
    private String isCorrect;

    /** 选项显示顺序，数值越小越靠前。 */
    private Integer sortOrder;

    /** 创建时间。 */
    private Date createTime;
}
