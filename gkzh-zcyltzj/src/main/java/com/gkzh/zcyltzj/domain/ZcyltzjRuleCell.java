package com.gkzh.zcyltzj.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/** 骰子局九宫格规则格实体：下左、下右两格用于展示后台配置的规则。 */
@Data
@TableName("gkzh_zcyltzj_rule_cell")
public class ZcyltzjRuleCell {
    /** 规则格编号。 */
    @TableId(value = "rule_id", type = IdType.AUTO)
    private Long ruleId;
    /** 九宫格位置：7 下左，9 下右。 */
    private Integer cellNo;
    /** 格子标题。 */
    private String title;
    /** 规则说明。 */
    private String content;
    /** 状态：0启用，1停用。 */
    private String status;
    /** 创建时间。 */
    private Date createTime;
    /** 最后更新时间。 */
    private Date updateTime;
}
