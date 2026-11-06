package com.gkzh.nlld.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 能量雷达职业价值观维度配置实体。
 * 固定 15 个维度，数据库中不允许新增或删除，只允许后台维护名称、分组、解读文案、排序和状态。
 */
@Data
@TableName("gkzh_nlld_dimension")
public class NlldDimension {

    /** 维度编号。 */
    @TableId(value = "dimension_id", type = IdType.AUTO)
    private Long dimensionId;

    /** 维度编码：intellect、achievement、creativity、autonomy、variety、salary、security、prestige、aesthetics、environment、management、colleague、supervisor、altruism、lifestyle。 */
    private String dimensionCode;

    /** 维度中文名，例如智力激发、安全稳定。 */
    private String dimensionName;

    /** 维度分组：内在价值、外在与保障、人际影响与生活方式。 */
    private String dimensionGroup;

    /** 该维度的“渴望……”解读文案。 */
    private String summary;

    /** 该维度的“职业参考：……”文案。 */
    private String careerReference;

    /** 显示顺序，数值越小越靠前，也用于 TOP2 同分排序。 */
    private Integer sortOrder;

    /** 状态：0 启用，1 停用。 */
    private String status;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
