package com.gkzh.nlld.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 能量雷达关卡物品选项实体。
 * 每关固定 5 件物品，对应 A-E 五个选项，每个选项绑定一个价值观维度。
 */
@Data
@TableName("gkzh_nlld_option")
public class NlldOption {

    /** 物品编号。 */
    @TableId(value = "option_id", type = IdType.AUTO)
    private Long optionId;

    /** 所属关卡编号。 */
    private Long roundId;

    /** 选项字母：A、B、C、D、E。 */
    private String optionLabel;

    /** 物品名称，例如居民应急供水分配器。 */
    private String optionName;

    /** 物品说明，即需求文档括号内的描述。 */
    private String optionDesc;

    /** 对应价值观维度编号。 */
    private Long dimensionId;

    /** 关内显示顺序，数值越小越靠前。 */
    private Integer sortOrder;

    /** 状态：0 启用，1 停用。 */
    private String status;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
