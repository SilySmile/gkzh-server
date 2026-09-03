package com.gkzh.cyzs.vo;

import lombok.Data;

@Data
public class QuestionVO {
    private Long id;
    private String questionText;
    private String type;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}
