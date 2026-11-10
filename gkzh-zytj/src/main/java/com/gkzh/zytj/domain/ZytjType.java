package com.gkzh.zytj.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职愿探究 16 型人格对照实体。
 * 四位代码由四列所选物品的维度字母按列序拼成，例如 ENFP。
 */
@Data
@TableName("gkzh_zytj_type")
public class ZytjType {

    /** 人格类型编号。 */
    @TableId(value = "type_id", type = IdType.AUTO)
    private Long typeId;

    /** 四位人格类型代码，例如 ENFP。 */
    private String typeCode;

    /** 性格特点，展示在职业探索卡“性格特点”区域。 */
    private String feature;

    /** 适合职业领域，展示在职业探索卡“适合职业领域”区域。 */
    private String careerField;

    /** 典型职业，展示在职业探索卡“典型职业”区域。 */
    private String typicalCareers;

    /** 后台列表展示顺序。 */
    private Integer sortOrder;

    /** 状态：0 启用，1 停用；当前不拦截结果展示。 */
    private String status;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
