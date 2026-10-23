package com.gkzh.zcyltzj.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/** 骰子局点数配置实体：每个骰子点数对应一个职场压力场景。 */
@Data
@TableName("gkzh_zcyltzj_point")
public class ZcyltzjPoint {
    /** 点数配置编号。 */
    @TableId(value = "point_id", type = IdType.AUTO)
    private Long pointId;
    /** 骰子点数：1-6。 */
    private Integer pointNo;
    /** 点数名称，例如“早八生存”。 */
    private String pointName;
    /** 该点数对应的压力主题，例如“早起通勤/固定班次”。 */
    private String theme;
    /** 该点数对应的职场投射说明。 */
    private String projection;
    /** 显示顺序，数值越小越靠前。 */
    private Integer sortOrder;
    /** 状态：0启用，1停用。 */
    private String status;
    /** 创建时间。 */
    private Date createTime;
    /** 最后更新时间。 */
    private Date updateTime;
    /** 该点数已配置的启用题目数量，仅用于后台展示，不持久化。 */
    @TableField(exist = false)
    private Long questionCount;
}
