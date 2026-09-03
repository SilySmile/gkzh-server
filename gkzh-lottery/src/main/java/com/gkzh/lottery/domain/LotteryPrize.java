package com.gkzh.lottery.domain;

import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;
import java.math.BigDecimal;

/**
 * 抽奖奖品对象 lottery_prize
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@Data
public class LotteryPrize extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 奖品ID */
    private Long prizeId;

    /** 所属抽奖活动 */
    private Long  lotteryId;

    /** 所属抽奖活动名称 */
    @Excel(name = "所属抽奖活动")
    private String activityTitle;

    /** 奖品名称 */
    @Excel(name = "奖品名称")
    private String title;

    /** 奖品图片 */
    private String imageUrl;

    /** 库存数量（-1表示不限） */
    @Excel(name = "库存数量")
    private Long stock;

    /** 奖品权重（数值越大中奖概率越高） */
    @Excel(name = "奖品权重")
    private Integer weight;

    /** 奖品类型（1-实物奖品，2-虚拟奖品，3-谢谢参与） */
    @Excel(name = "奖品类型", readConverterExp = "1=实物奖品,2=虚拟奖品,3=谢谢参与")
    private Integer prizeType;

    /** 是否启用（0-禁用，1-启用） */
    @Excel(name = "是否启用", readConverterExp = "0=禁用,1=启用")
    private String isEnabled;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    private Long sortOrder;

    /** 计算出的实际概率（用于显示） */
    private BigDecimal actualProbability;

}
