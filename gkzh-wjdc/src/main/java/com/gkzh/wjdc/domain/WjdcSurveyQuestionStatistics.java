package com.gkzh.wjdc.domain;

import lombok.Data;

import java.util.List;

@Data
public class WjdcSurveyQuestionStatistics {
    private Long questionId;
    private String questionTitle;
    private String questionType;
    private Long totalResponses;
    private List<WjdcSurveyStatistics> options;
    private List<WjdcSurveyStatistics> answers;


    public String getQuestionTypeName() {
        switch (questionType) {
            case "1": return "[单选题]";
            case "2": return "[多选题]";
            case "3": return "[填空题]";
            case "4": return "[打分题]";
            case "5": return "[选项打分题]";
            default: return "";
        }
    }
}
