package com.gkzh.sszctop.domain.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 谁是职场TOP统计分析查询条件。
 * 后台统计、学生明细和 Excel 导出共用同一份筛选口径，避免各页面人数不一致。
 */
@Data
public class SszctopStatisticsQuery {

    /** 活动实例编号，空表示全部活动。 */
    private Long instanceId;

    /** 学校编号；报告表未存学校，按学生档案或活动区域回退。 */
    private Long schoolId;

    /** 院系编号；同时兼容按院系或直属专业编号筛选。 */
    private Long departmentId;

    /** 专业名称模糊条件。 */
    private String major;

    /** 学号模糊条件。 */
    private String studentNo;

    /** 性别中文值：男、女、其他；由服务端转换为数据库存储值。 */
    private String genderValue;

    /** 挑战结果：passed=挑战成功、failed=本局未通过。 */
    private String result;

    /** 完成日期开始，含当天零点。 */
    private LocalDate startDate;

    /** 完成日期结束次日零点，不含；页面上按“结束日期含当天”转换。 */
    private LocalDate endDate;
}
