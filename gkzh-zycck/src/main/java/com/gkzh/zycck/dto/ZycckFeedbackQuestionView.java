package com.gkzh.zycck.dto;

import lombok.Data;
import java.util.List;

/** 活动反馈题目与选项定义，供小程序答题和 Web 查看统一使用。 */
@Data
public class ZycckFeedbackQuestionView {
    /** 题号，固定为 1、2、3。 */
    private Integer questionNo;
    /** 题目内容。 */
    private String title;
    /** 选择类型：multiple 多选，single 单选。 */
    private String type;
    /** 最多可选择的选项数。 */
    private Integer maxSelections;
    /** 与其余选项互斥的编码，无互斥项时为空。 */
    private String exclusiveOption;
    /** 该题全部选项。 */
    private List<Option> options;

    /** 固定选项；编码用于后续汇总，文字用于展示。 */
    @Data
    public static class Option {
        /** 稳定选项编码。 */
        private String code;
        /** 选项中文内容。 */
        private String label;
        /** 是否需要填写补充文字。 */
        private boolean requiresText;
    }
}
