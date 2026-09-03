package com.gkzh.activity.domain.week;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 活动抽奖记录
 */
@Data
@TableName("gkzh_lottery_record")
public class GkzhLotteryRecord {

    @TableId(type = IdType.AUTO)
    private Long recordId;

    private Long ruleId;

    private Long instanceId;

    private Long studentId;

    private Long userId;

    private Long prizeId;

    private String prizeTitle;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date drawTime;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
