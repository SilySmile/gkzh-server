package com.gkzh.jlcz.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 简历称重规则参数实体（单行配置）。
 * 左侧标准砝码、通关阈值、限时与重试开关均以此表为准，学生端不能自行传入。
 */
@Data
@TableName("gkzh_jlcz_setting")
public class JlczSetting {

    /** 配置编号，固定为 1（单行配置）。 */
    @TableId("setting_id")
    private Integer settingId;

    /** 天平左侧标准砝码重量（克），线下固定为 80g。 */
    private Integer leftWeightG;

    /** 通关总重量阈值（克），总重达到该值即通关。 */
    private Integer passWeightG;

    /** 称重限时（秒），0 表示不限时；超过后提交按超时处理。 */
    private Integer timeLimitSeconds;

    /** 未通关是否允许重新称重：0 禁止，1 允许。 */
    private String allowRetry;

    /** 是否启用游戏：0 停用，1 启用；停用时学生端拒绝进入。 */
    private String enabled;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
