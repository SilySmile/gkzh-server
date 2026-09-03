package com.gkzh.activity.domain.week;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 活动实例与学校关系
 */
@Data
@TableName("gkzh_activity_week_school")
public class GkzhActivityWeekSchool {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long instanceId;

    private Long schoolId;

    private Long lotteryId;

    private Integer minFinishCount;

    private Integer maxDrawCount;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
