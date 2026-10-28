package com.gkzh.nlld.domain.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 能量雷达参与记录查询条件。
 * 后台列表、统计汇总、维度偏好和 Excel 导出共用同一份筛选口径，避免各页面人数不一致。
 */
@Data
public class NlldRecordQuery {

    /** 活动实例编号，空表示全部活动。 */
    private Long instanceId;

    /** 活动游戏编号，活动查看页按单个游戏查询时使用。 */
    private Long gameId;

    /** 学校编号。 */
    private Long schoolId;

    /** 院系编号；查询时包含其直属专业。 */
    private Long departmentId;

    /** 专业名称模糊条件。 */
    private String major;

    /** 学号模糊条件。 */
    private String studentNo;

    /** 性别中文值：男、女、其他；由服务端转换为数据库存储值。 */
    private String genderValue;

    /** 结果类型：top2、open。 */
    private String resultType;

    /** TOP 维度编码，筛选 TOP1 或 TOP2 命中该维度的记录。 */
    private String dimensionCode;

    /** 参与状态：participating、finished。 */
    private String status;

    /** 参与日期开始，含当天零点。 */
    private LocalDate startDate;

    /** 参与日期结束次日零点，不含；页面上按“结束日期含当天”转换。 */
    private LocalDate endDate;
}
