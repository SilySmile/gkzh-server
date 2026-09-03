package com.gkzh.wjyd.vo;

import lombok.Data;

@Data
public class AnswerResult {
    private boolean success;
    private String message;
    private Integer correctCount;
    private Integer totalCount;
    private Long roundId;

}