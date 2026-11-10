package com.gkzh.zdyp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职得一拼闯关尝试实体。
 * 每次点击开始闯关生成一条，重闯累加 attempt_no；用于重闯次数、超时与用时统计。
 */
@Data
@TableName("gkzh_zdyp_attempt")
public class ZdypAttempt {

    /** 闯关尝试编号。 */
    @TableId(value = "attempt_id", type = IdType.AUTO)
    private Long attemptId;

    /** 所属参与记录编号。 */
    private Long recordId;

    /** 第几次闯关，从 1 开始累加。 */
    private Integer attemptNo;

    /** 本局所选岗位编号。 */
    private Long careerId;

    /** 本次放对的卡槽数量（0-4）。 */
    private Integer correctCount;

    /** 本次放错的卡槽数量（0-4）。 */
    private Integer wrongCount;

    /** 本次实际提交的卡槽数量，少于 4 表示超时未填满。 */
    private Integer filledCount;

    /** 是否超时：0 否，1 是；由服务端按开始时间加限时判定。 */
    private String isTimeout;

    /** 本次是否通关：0 否，1 是（4 个卡槽全部放对）。 */
    private String isPass;

    /** 本次从开始到提交的用时（毫秒）。 */
    private Long durationMs;

    /** 本局第一个被填入的卡槽编码，用于首填偏好统计。 */
    private String firstSlotCode;

    /** 提交时间；未提交时为空，表示进行中。 */
    private Date submitTime;

    /** 开始时间。 */
    private Date createTime;
}
