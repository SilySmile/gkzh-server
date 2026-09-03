package com.gkzh.lottery.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

/**
 * 抽奖记录对象 lottery_record
 *
 * @author gkzh
 * @date 2025-07-04
 */
@Data
public class LotteryRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 抽奖ID */
    @Excel(name = "抽奖ID")
    private Long lotteryId;

    /** 活动实例 ID，避免同一抽奖配置在不同活动之间串记录 */
    private Long activityId;

    private String bizType;

    /** 中奖奖品ID */
    @Excel(name = "中奖奖品ID")
    private Long prizeId;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户姓名 */
    @Excel(name = "用户姓名")
    private String userName;

    /** 奖品名称 */
    @Excel(name = "奖品名称")
    private String prizeTitle;

    /** 抽奖时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "抽奖时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date drawTime;

    /** 状态（0未领取 1已领取 2已过期） */
    @Excel(name = "状态", readConverterExp = "0=未领取,1=已领取,2=已过期")
    private String status;

    private String nickName;
    private String activityTitle;
    private String resultName;

    /** 奖品核销状态及时间（活动工作人员核销模块） */
    private String redemptionStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date redeemTime;
    private String redemptionStaffName;
    private String redemptionStaffAccount;
    private String redemptionOperatorType;
    private String redemptionCode;

}
