package com.gkzh.jlcz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.jlcz.domain.JlczRecord;
import com.gkzh.jlcz.domain.dto.JlczRecordQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 简历称重参与记录数据访问。
 * 动态筛选条件统一维护在 JlczRecordMapper.xml，列表、统计、砝码统计和导出共用同一口径。
 */
@Mapper
public interface JlczRecordMapper extends BaseMapper<JlczRecord> {

    /** 按筛选条件查询参与记录，供后台分页和 Excel 导出使用。 */
    List<JlczRecord> selectAdminRecords(@Param("q") JlczRecordQuery query);

    /** 按与列表相同的条件汇总参与、通关、平均重量、平均用时与总重区间分布。 */
    Map<String, Object> selectAdminSummary(@Param("q") JlczRecordQuery query);

    /** 按砝码汇总当前称重次数下的勾选人数，用于砝码选择率和缺口统计。 */
    List<Map<String, Object>> selectWeightStats(@Param("q") JlczRecordQuery query);

    /** 活动查看页使用：按游戏取最近若干条记录，避免一次加载全部历史数据。 */
    List<JlczRecord> selectGameViewRecords(@Param("gameId") Long gameId, @Param("limit") int limit);
}
