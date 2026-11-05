package com.gkzh.xycc.mapper;

import com.gkzh.xycc.domain.dto.XyccStatisticsQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 心愿橱窗统计查询。
 * 动态筛选条件维护在 XyccStatisticsMapper.xml，统计、明细和导出共用。
 */
@Mapper
public interface XyccStatisticsMapper {

    /** 按筛选条件查询每位学生每个活动的最新一次选择记录。 */
    List<Map<String, Object>> selectSelectionRows(@Param("q") XyccStatisticsQuery query);

    /** 一次取全部复合编码对应的职业方向与工作环境偏好，避免逐行查询。 */
    List<Map<String, Object>> selectComboTitles();
}
