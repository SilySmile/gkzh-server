package com.gkzh.wjyd.vo;

import lombok.Data;

@Data
public class QuestionVO {
    private Long id;
    private String questionText;
    private String questionImage;
    private String optionA;
    private String optionB;
    private String optionC;
}
