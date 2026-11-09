package com.gkzh.zmwl.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职面未来发展方向配置实体。
 * 8 个方向对应线下贴纸选项；名称、简短介绍、标签称号、方向解读和寄语均可在 Web 后台维护。
 */
@Data
@TableName("gkzh_zmwl_direction")
public class ZmwlDirection {

    /** 发展方向编号（自增主键）。 */
    @TableId(value = "direction_id", type = IdType.AUTO)
    private Long directionId;

    /** 发展方向编码，全小写下划线，例如 employment，用于记录快照与统计。 */
    private String directionCode;

    /** 发展方向中文名，例如“就业工作”。 */
    private String name;

    /** 卡片上的简短介绍，例如“走进职场，开启职业生涯”。 */
    private String shortIntro;

    /** 方向标签称号，例如“职场启航者”，展示在个人选择卡上。 */
    private String tagName;

    /** 方向解读长文案，展示在个人选择卡“方向解读”区域。 */
    private String interpretation;

    /** 寄语长文案，展示在个人选择卡“寄语”区域。 */
    private String message;

    /** 方向图标图片地址；为空时学生端使用内置兜底图标。 */
    private String iconUrl;

    /** 学生端选项展示顺序，数值越小越靠前。 */
    private Integer sortOrder;

    /** 状态：0 启用，1 停用；停用方向不出现在学生端，也不能被选择。 */
    private String status;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
