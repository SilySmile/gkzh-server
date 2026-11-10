package com.gkzh.zdyp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.zdyp.domain.ZdypRecord;
import com.gkzh.zdyp.domain.dto.ZdypRecordQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 职得一拼参与记录 Mapper。
 * 列表、汇总、行为分型、维度错误、首填偏好、职业分布、院系分布和活动查看共用同一套动态条件，
 * SQL 统一写在 XML 中便于排查。
 */
@Mapper
public interface ZdypRecordMapper extends BaseMapper<ZdypRecord> {

    /** 后台参与记录查询：按活动、岗位、行为分型、状态、学生属性和参与日期动态筛选。 */
    List<ZdypRecord> selectAdminRecords(@Param("q") ZdypRecordQuery query);

    /** 统计汇总：参与、通关、首次通关、重闯、超时与用时等指标，与列表同一口径。 */
    Map<String, Object> selectAdminSummary(@Param("q") ZdypRecordQuery query);

    /** 四类行为分型人数，只统计已通关且已写入分型的记录。 */
    List<Map<String, Object>> selectBehaviorStats(@Param("q") ZdypRecordQuery query);

    /** 首次尝试各卡槽的放对与放错次数，用于维度错误率与职业缺点短板分析。 */
    List<Map<String, Object>> selectFirstAttemptSlotStats(@Param("q") ZdypRecordQuery query);

    /** 首次尝试首选卡槽的分布，用于判断学生是先看优势还是先看短板。 */
    List<Map<String, Object>> selectFirstSlotStats(@Param("q") ZdypRecordQuery query);

    /** 各岗位的选择人数与通关人数。 */
    List<Map<String, Object>> selectCareerStats(@Param("q") ZdypRecordQuery query);

    /** 各院系的参与与通关人数；院系取专业部门的父部门。 */
    List<Map<String, Object>> selectDepartmentSummary(@Param("q") ZdypRecordQuery query);

    /** 活动查看页只取最近若干条记录，限制数据量。 */
    List<ZdypRecord> selectGameViewRecords(@Param("gameId") Long gameId, @Param("limit") int limit);
}
