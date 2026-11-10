package com.gkzh.zdyp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职得一拼岗位卡片实体。
 * 每个岗位固定四个维度各一张卡片；卡片的维度编码是判定与统计的唯一依据。
 */
@Data
@TableName("gkzh_zdyp_card")
public class ZdypCard {

    /** 卡片编号。 */
    @TableId(value = "card_id", type = IdType.AUTO)
    private Long cardId;

    /** 所属岗位编号。 */
    private Long careerId;

    /** 卡片维度编码：content 工作内容，skill 核心技能，weakness 职业缺点，prospect 发展前景。 */
    private String dimensionCode;

    /** 卡片正文，学生端直接展示该文案。 */
    private String cardText;

    /** 同一岗位内卡片的默认顺序，仅用于后台展示。 */
    private Integer sortOrder;

    /** 状态：0 启用，1 停用；停用卡片不参与拼图。 */
    private String status;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
