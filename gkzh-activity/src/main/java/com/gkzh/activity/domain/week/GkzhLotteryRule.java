package com.gkzh.activity.domain.week;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 活动抽奖规则
 */
@Data
@TableName("gkzh_lottery_rule")
public class GkzhLotteryRule {

    @TableId(type = IdType.AUTO)
    private Long ruleId;

    private Long instanceId;

    private Integer requiredCompletedGames;

    private Integer maxDrawPerStudent;

    private String status;

    private String config;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
