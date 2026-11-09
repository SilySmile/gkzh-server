package com.gkzh.zmwl.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职面未来规则参数实体（单行配置，主键固定为 1）。
 * 只保留现场真正需要的两个开关，页面文案与选项内容全部由方向配置维护。
 */
@Data
@TableName("gkzh_zmwl_setting")
public class ZmwlSetting {

    /** 配置编号，固定为 1（单行配置）。 */
    @TableId(value = "setting_id", type = IdType.INPUT)
    private Integer settingId;

    /** 确认后是否允许重新选择：0 不允许（只读），1 允许覆盖。 */
    private String allowReselect;

    /** 是否启用游戏：0 停用，1 启用；停用时学生端拒绝进入。 */
    private String enabled;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
