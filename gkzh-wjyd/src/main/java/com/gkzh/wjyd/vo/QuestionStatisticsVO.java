package com.gkzh.wjyd.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionStatisticsVO {
    private String questionTitle;
    private String optionContent;
    private Integer count;
    private String percentage;
}
