package com.gkzh.zycck.dto;

import lombok.Data;

/** 学校活动内按系、专业汇总的去重参与人数；一位用户只归入一组。 */
@Data
public class ZycckParticipationSummaryRow {
    /** 系（一级部门）编号；没有有效部门时为空。 */
    private Long facultyId;
    /** 系（一级部门）名称；没有有效部门时为空。 */
    private String facultyName;
    /** 专业（二级部门）编号；仅有专业名称快照时为空。 */
    private Long majorId;
    /** 专业名称，优先取首次参与记录的专业快照。 */
    private String majorName;
    /** 当前系和专业内的去重参与人数。 */
    private Long participantCount;
}
