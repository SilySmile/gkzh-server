package com.gkzh.zmwl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.zmwl.domain.ZmwlRecord;
import com.gkzh.zmwl.domain.dto.ZmwlRecordQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 职面未来参与记录 Mapper。
 * 列表、汇总、方向分布、院系偏好和活动查看共用同一套动态条件，SQL 统一写在 XML 中便于排查。
 */
@Mapper
public interface ZmwlRecordMapper extends BaseMapper<ZmwlRecord> {

    /** 后台参与记录查询：按活动、方向、状态、学生属性和参与日期动态筛选。 */
    List<ZmwlRecord> selectAdminRecords(@Param("q") ZmwlRecordQuery query);

    /** 统计汇总：参与人数、完成人数与“暂未确定”人数等，与列表同一口径。 */
    Map<String, Object> selectAdminSummary(@Param("q") ZmwlRecordQuery query);

    /** 各发展方向的选择人数，只统计已确认选择的记录。 */
    List<Map<String, Object>> selectDirectionStats(@Param("q") ZmwlRecordQuery query);

    /** 各院系的参与与完成人数；院系取专业部门的父部门。 */
    List<Map<String, Object>> selectDepartmentSummary(@Param("q") ZmwlRecordQuery query);

    /** 各院系在每个发展方向上的人数，用于院系偏好与 Excel 明细。 */
    List<Map<String, Object>> selectDepartmentDirectionCounts(@Param("q") ZmwlRecordQuery query);

    /** 活动查看页只取最近若干条记录，限制数据量。 */
    List<ZmwlRecord> selectGameViewRecords(@Param("gameId") Long gameId, @Param("limit") int limit);
}
