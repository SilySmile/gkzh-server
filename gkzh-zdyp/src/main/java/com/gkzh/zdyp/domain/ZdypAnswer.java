package com.gkzh.zdyp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职得一拼作答明细实体。
 * 每次尝试固定四个卡槽，每次尝试最多写入四条；超时未填的卡槽不会写入记录。
 */
@Data
@TableName("gkzh_zdyp_answer")
public class ZdypAnswer {

    /** 作答明细编号。 */
    @TableId(value = "answer_id", type = IdType.AUTO)
    private Long answerId;

    /** 所属闯关尝试编号。 */
    private Long attemptId;

    /** 所属参与记录编号，便于按记录直接统计。 */
    private Long recordId;

    /** 卡槽编码：content、skill、weakness、prospect。 */
    private String slotCode;

    /** 放入该卡槽的卡片编号；超时未填时为空。 */
    private Long cardId;

    /** 放入卡片所属维度，用于判断是否放对。 */
    private String cardDimension;

    /** 是否放对：0 否，1 是。 */
    private String isCorrect;

    /** 本局放置顺序，从 1 开始，用于首填偏好与操作轨迹。 */
    private Integer placeOrder;

    /** 从本局开始到放入该卡槽的用时（毫秒）。 */
    private Long elapsedMs;

    /** 创建时间。 */
    private Date createTime;
}
