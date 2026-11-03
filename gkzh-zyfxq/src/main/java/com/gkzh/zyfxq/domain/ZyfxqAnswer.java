package com.gkzh.zyfxq.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职业飞行棋答题明细实体。
 * 每次落点触发题目并作答（含超时未作答）都会写入一行，题干、选项和正确答案同时快照，
 * 题库后续修改或删除都不影响历史统计。
 */
@Data
@TableName("gkzh_zyfxq_answer")
public class ZyfxqAnswer {

    /** 答题明细编号。 */
    @TableId(value = "answer_id", type = IdType.AUTO)
    private Long answerId;

    /** 参与记录编号。 */
    private Long recordId;

    /** 第几局，与参与记录的 attempt_no 对应。 */
    private Integer attemptNo;

    /** 触发该题的格号。 */
    private Integer cellNo;

    /** 题目编号。 */
    private Long questionId;

    /** 题目类型快照：career 职业问答，scenario 情景任务卡。 */
    private String questionType;

    /** 题干快照。 */
    private String questionTitle;

    /** 学生所选选项字母；超时未作答为空。 */
    private String optionLabel;

    /** 学生所选选项内容快照；超时未作答为空。 */
    private String optionContent;

    /** 正确选项字母快照。 */
    private String correctLabel;

    /** 正确选项内容快照。 */
    private String correctContent;

    /** 是否答对：1 答对，0 答错或超时。 */
    private String isCorrect;

    /** 是否超时：1 超时未作答，0 已作答。 */
    private String isTimeout;

    /** 题目限时（秒）快照。 */
    private Integer limitSeconds;

    /** 实际答题耗时（毫秒），由服务端下发与提交时间计算。 */
    private Integer costMs;

    /** 答题前所处格号。 */
    private Integer positionBefore;

    /** 答题后所处格号（答对不变，答错或超时后退）。 */
    private Integer positionAfter;

    /** 作答时间。 */
    private Date createTime;
}
