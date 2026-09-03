package com.gkzh.wjyd.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.gkzh.common.annotation.Excel;
import lombok.Data;

@Data
public class UserAnswerRecordVO {
    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    @Excel(name = "学号")
    /** 用户名（如果有）*/
    private String userName;
    @Excel(name = "姓名")
    /** 用户名（如果有）*/
    private String nickName;
    /** 题目内容 */
    @Excel(name = "题目")
    private String questionText;

    /** 选项A内容 */
    @Excel(name = "选项A")
    private String optionA;

    /** 选项B内容 */
    @Excel(name = "选项B")
    private String optionB;

    /** 选项C内容 */
    @Excel(name = "选项C")
    private String optionC;

    /** 用户答案 */
    @Excel(name = "回答结果")
    private String userAnswer;

    /** 是否答对 */
    @Excel(name = "是否答对")
    private String isCorrect;

    /** 答题时间 */
    @Excel(name = "答题时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String answerTime;

    private Long questionId;
}
