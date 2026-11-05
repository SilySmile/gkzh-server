package com.gkzh.xycc.domain.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 心愿橱窗统计分析查询条件。
 * 后台统计、学生明细和 Excel 导出共用同一份筛选口径。
 */
@Data
public class XyccStatisticsQuery {

    /** 活动实例编号。 */
    private Long instanceId;

    /** 学校编号；优先学生档案，缺失时回退活动区域。 */
    private Long schoolId;

    /** 院系编号；同时兼容院系或直属专业编号。 */
    private Long departmentId;

    /** 专业名称模糊条件。 */
    private String major;

    /** 学号模糊条件。 */
    private String studentNo;

    /** 性别中文值：男、女、其他。 */
    private String genderValue;

    /** 复合编码，如 RIA；精确匹配。 */
    private String code;

    /** 选择日期开始，含当天零点。 */
    private LocalDate startDate;

    /** 选择日期结束次日零点，不含。 */
    private LocalDate endDate;
}
