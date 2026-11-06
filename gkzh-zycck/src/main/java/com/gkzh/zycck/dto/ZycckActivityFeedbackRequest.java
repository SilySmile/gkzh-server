package com.gkzh.zycck.dto;

import lombok.Data;
import java.util.List;

/** 学生活动反馈提交内容；服务端校验选项、互斥、数量及其他文字。 */
@Data
public class ZycckActivityFeedbackRequest {
    /** 第 1 题多选编码，需选 1 至 3 项。 */
    private List<String> question1Answers;
    /** 第 1 题“其他”的补充文字，选择其他时必填，最多 500 字。 */
    private String question1Other;
    /** 第 2 题单选编码，仅曾加入探索清单时必填。 */
    private String question2Answer;
    /** 第 2 题“其他”的补充文字，选择其他时必填，最多 500 字。 */
    private String question2Other;
    /** 第 3 题多选编码，需选 1 至 3 项。 */
    private List<String> question3Answers;
    /** 第 3 题“其他”的补充文字，选择其他时必填，最多 500 字。 */
    private String question3Other;
}
