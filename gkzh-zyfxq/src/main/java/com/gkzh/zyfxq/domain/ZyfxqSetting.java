package com.gkzh.zyfxq.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职业飞行棋规则参数实体（单行配置，setting_id 固定为 1）。
 * 每局开始时把下列值快照到参与记录，避免对局中途改配置导致判定口径不一致。
 */
@Data
@TableName("gkzh_zyfxq_setting")
public class ZyfxqSetting {

    /** 配置编号，固定为 1。 */
    @TableId(value = "setting_id", type = IdType.INPUT)
    private Integer settingId;

    /** 单局总时长（秒），到点未抵达终点判定为未通关。 */
    private Integer totalSeconds;

    /** 单题限时（秒），超时按答错处理。 */
    private Integer questionSeconds;

    /** 答错或超时后退格数。 */
    private Integer backSteps;

    /** 骰子面数，单骰取 1 到该值。 */
    private Integer diceFaces;

    /** 是否启用游戏：0 停用，1 启用。 */
    private String enabled;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
