package com.gkzh.cyzs.vo;

import lombok.Data;

@Data
public class AnswerDetailVO {
    /** 题目ID */
    private Long questionId;

    /** 题目内容 */
    private String questionText;

    /** 用户答案 */
    private String userAnswer;

    /** 是否正确 */
    private Integer isCorrect;
}