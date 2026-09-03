package com.gkzh.activity.domain.staff;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("gkzh_prize_redemption")
public class GkzhPrizeRedemption {
    @TableId(type = IdType.AUTO)
    private Long redemptionId;
    private Long lotteryRecordId;
    private Long schoolId;
    private Long studentId;
    private Long staffId;
    private Long adminUserId;
    private String status;
    private Date redeemTime;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
