package com.gkzh.zycck.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

/** 学生端和统计详情共用的活动反馈返回内容。 */
@Data
public class ZycckActivityFeedbackView {
    /** 所属参与记录编号。 */
    private Long recordId;
    /** 问卷版本。 */
    private String questionnaireVersion;
    /** 是否曾经成功保存活动反馈。 */
    private boolean submitted;
    /** 游戏是否已结束；有效反馈保存成功后为 true，待填写期间为 false。 */
    private boolean gameFinished;
    /** 第 2 题是否适用，由服务端根据曾加入清单的记录计算。 */
    private boolean showQuestion2;
    /** 当前适用的题目定义；第 2 题不适用时不包含它。 */
    private List<ZycckFeedbackQuestionView> questions;
    /** 已保存的答案，未提交时为空白答案对象。 */
    private ZycckActivityFeedbackRequest answers;
    /** 成功提交时间，提交后固定，未提交时为空。 */
    private Date submittedTime;
}
