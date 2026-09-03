package com.gkzh.lottery.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;
import com.gkzh.common.annotation.Excel;

/**
 * 抽奖环节对象 lottery_activity
 * 
 * @author gkzh
 * @date 2025-06-16
 */
@Data
public class Lottery extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long lotteryId;

    /** 活动名称 */
    @Excel(name = "活动名称")
    private String title;

    /** 活动描述 */
    @Excel(name = "活动描述")
    private String description;

    /** 活动开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "活动开始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startTime;

    /** 活动结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "活动结束时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endTime;

    /** 状态（0-禁用，1-启用） */
    @Excel(name = "状态", readConverterExp = "0=禁用,1=启用")
    private String status;

}
