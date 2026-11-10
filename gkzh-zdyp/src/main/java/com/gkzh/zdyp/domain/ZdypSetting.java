package com.gkzh.zdyp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职得一拼规则参数实体（单行配置）。
 * 限时由服务端按本局开始时间独立判定，不信任客户端倒计时。
 */
@Data
@TableName("gkzh_zdyp_setting")
public class ZdypSetting {

    /** 配置编号，固定为 1。 */
    @TableId(value = "setting_id", type = IdType.INPUT)
    private Integer settingId;

    /** 单次闯关限时（秒），0 表示不限时；到点未填满按超时处理。 */
    private Integer timeLimit;

    /** 是否启用游戏：0 停用，1 启用；停用时学生端拒绝进入。 */
    private String enabled;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
