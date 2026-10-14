package com.gkzh.zycck.dto;

import lombok.Data;

/** 统计详情中的浏览或探索职业简要信息。 */
@Data
public class ZycckCareerBriefView {
    /** 职业编号；原职业删除后仍保留编号。 */
    private Long careerId;
    /** 职业名称；删除后显示包含编号的提示。 */
    private String careerName;
    /** 职业一句话介绍。 */
    private String oneLineIntro;
}
