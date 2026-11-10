package com.gkzh.zytj.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职愿探究规则参数（单行配置）。
 * 需求口径：30 秒内每列选 1 件；提交后默认只读，可在后台开启“允许重新探究”。
 */
@Data
@TableName("gkzh_zytj_setting")
public class ZytjSetting {

    /** 配置编号，固定为 1。 */
    @TableId(value = "setting_id", type = IdType.INPUT)
    private Integer settingId;

    /** 单局限时（秒），从进入选择页开始计时；0 表示不限时。 */
    private Integer timeLimit;

    /** 提交后是否允许重新探究：0 不允许（只读），1 允许覆盖并按最新一次统计。 */
    private String allowReselect;

    /** 是否启用游戏：0 停用，1 启用。 */
    private String enabled;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
