package com.gkzh.nlld.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 能量雷达关卡配置实体。
 * 固定 6 关，只允许后台维护关卡名称、任务情景、排序和状态。
 */
@Data
@TableName("gkzh_nlld_round")
public class NlldRound {

    /** 关卡编号。 */
    @TableId(value = "round_id", type = IdType.AUTO)
    private Long roundId;

    /** 第几关，取值 1-6。 */
    private Integer roundNo;

    /** 关卡名称，例如抢修海岛供水系统。 */
    private String title;

    /** 任务情景说明。 */
    private String scene;

    /** 显示顺序，数值越小越靠前。 */
    private Integer sortOrder;

    /** 状态：0 启用，1 停用。 */
    private String status;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
