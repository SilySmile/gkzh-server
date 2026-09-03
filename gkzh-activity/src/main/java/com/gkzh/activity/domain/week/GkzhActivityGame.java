package com.gkzh.activity.domain.week;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 活动游戏
 */
@Data
@TableName("gkzh_activity_game")
public class GkzhActivityGame {

    @TableId(type = IdType.AUTO)
    private Long gameId;

    private Long areaId;

    private Long instanceId;

    private String gameType;

    private String title;

    private String config;

    private Long ruleId;

    private String requiredFlag;

    private Integer sortOrder;

    private String status;

    private String qrCode;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String remark;
}
