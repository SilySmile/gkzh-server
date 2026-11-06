package com.gkzh.jlcz.domain.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 简历称重参与记录查询条件。
 * 后台列表、统计汇总、砝码统计和 Excel 导出共用同一份筛选口径，避免各页面人数不一致。
 */
@Data
public class JlczRecordQuery {

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

    /** 称重结果：pass 通关、fail 未通关。 */
    private String resultValue;

    /** 记录状态：finished 已完成、inProgress 进行中。 */
    private String status;

    /** 参与日期开始，含当天零点。 */
    private LocalDate startDate;

    /** 参与日期结束次日零点，不含；页面上按“结束日期含当天”转换。 */
    private LocalDate endDate;
}
