package com.gkzh.activity.domain.staff;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
@TableName("gkzh_prize_redemption_log")
public class GkzhPrizeRedemptionLog {
    @TableId(type = IdType.AUTO)
    private Long logId;
    private Long redemptionId;
    private Long lotteryRecordId;
    private Long schoolId;
    private Long staffId;
    private Long adminUserId;
    private String action;
    private String beforeStatus;
    private String afterStatus;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @TableField(exist = false)
    private String operatorName;
    @TableField(exist = false)
    private String operatorAccount;
    @TableField(exist = false)
    private String operatorType;
}
