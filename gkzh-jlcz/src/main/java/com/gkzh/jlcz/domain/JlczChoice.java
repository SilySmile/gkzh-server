package com.gkzh.jlcz.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 简历称重砝码选择明细实体。
 * 每次提交按 attempt_no 保存本次勾选的砝码快照，后台修改砝码名称或重量不影响历史统计。
 */
@Data
@TableName("gkzh_jlcz_choice")
public class JlczChoice {

    /** 选择明细编号。 */
    @TableId(value = "choice_id", type = IdType.AUTO)
    private Long choiceId;

    /** 参与记录编号。 */
    private Long recordId;

    /** 第几次称重，与参与记录 attempt_no 对应。 */
    private Integer attemptNo;

    /** 砝码编号（提交时的配置编号）。 */
    private Long weightId;

    /** 挂钩号快照，取值 1-20。 */
    private Integer weightNo;

    /** 砝码标签快照。 */
    private String weightLabel;

    /** 砝码重量快照（克）。 */
    private Integer weightG;

    /** 砝码大类编码快照：core、soft、basic、bonus。 */
    private String categoryCode;

    /** 选择时间。 */
    private Date createTime;
}
