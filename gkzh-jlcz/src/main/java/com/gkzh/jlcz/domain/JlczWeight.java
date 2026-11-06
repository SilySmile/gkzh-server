package com.gkzh.jlcz.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 简历称重砝码配置实体。
 * 20 个砝码按四大类分组，学生对照简历勾选已具备的项，重量为 10g 或 5g。
 */
@Data
@TableName("gkzh_jlcz_weight")
public class JlczWeight {

    /** 砝码编号（自增主键）。 */
    @TableId(value = "weight_id", type = IdType.AUTO)
    private Long weightId;

    /** 挂钩号，取值 1-20，与线下简历选装卡编号一致。 */
    private Integer weightNo;

    /** 砝码大类编码：core 硬通货，soft 软实力，basic 基本功，bonus 加分项。 */
    private String categoryCode;

    /** 砝码大类中文名，例如“硬通货（核心竞争力）”。 */
    private String categoryName;

    /** 砝码标签，例如“靶心岗位”。 */
    private String label;

    /** 砝码图标字符（emoji），例如“🎯”。 */
    private String icon;

    /** 砝码图片地址；为空时学生端使用 icon 字符展示。 */
    private String imageUrl;

    /** 砝码重量（克），当前取 10 或 5。 */
    private Integer weightG;

    /** 学生自查标准说明，用于卡片展开和结果建议。 */
    private String conditionText;

    /** 同大类内显示顺序，数值越小越靠前。 */
    private Integer sortOrder;

    /** 状态：0 启用，1 停用；停用砝码不出现在学生端且不计分。 */
    private String status;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
