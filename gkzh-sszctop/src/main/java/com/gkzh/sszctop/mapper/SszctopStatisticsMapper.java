package com.gkzh.sszctop.mapper;

import com.gkzh.sszctop.domain.dto.SszctopStatisticsQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 谁是职场TOP统计查询。
 * 动态筛选条件统一维护在 SszctopStatisticsMapper.xml，统计汇总、学生明细和导出共用。
 */
@Mapper
public interface SszctopStatisticsMapper {

    /** 按筛选条件查询报告及学生、学校、院系、专业展示字段。 */
    List<Map<String, Object>> selectReportRows(@Param("q") SszctopStatisticsQuery query);
}
