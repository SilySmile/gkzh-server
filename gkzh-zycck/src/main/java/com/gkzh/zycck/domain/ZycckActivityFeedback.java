package com.gkzh.zycck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/** 活动反馈实体；通过参与记录关联学校、活动、游戏和学生，每份记录只有一份反馈。 */
@Data
@TableName("gkzh_zycck_activity_feedback")
public class ZycckActivityFeedback {
    /** 参与记录主键，也是本表主键，保证重复提交不产生多份反馈。 */
    @TableId(value = "record_id", type = IdType.INPUT)
    private Long recordId;
    /** 问卷版本，供后续总结报告区分不同版本的题目与选项。 */
    private String questionnaireVersion;
    /** 第 1 题选择的稳定选项编码 JSON 数组。 */
    private String question1Answers;
    /** 第 1 题“其他”的补充文字。 */
    private String question1Other;
    /** 第 2 题是否适用：0 未曾加入清单，1 曾加入清单。 */
    private String showQuestion2;
    /** 第 2 题单选编码；不适用时为空。 */
    private String question2Answer;
    /** 第 2 题“其他”的补充文字。 */
    private String question2Other;
    /** 第 3 题选择的稳定选项编码 JSON 数组。 */
    private String question3Answers;
    /** 第 3 题“其他”的补充文字。 */
    private String question3Other;
    /** 首次成功提交反馈的时间，提交后不再修改。 */
    private Date submittedTime;
}
