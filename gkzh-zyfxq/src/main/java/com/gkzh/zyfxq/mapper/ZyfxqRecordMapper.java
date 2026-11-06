package com.gkzh.zyfxq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.zyfxq.domain.ZyfxqRecord;
import com.gkzh.zyfxq.domain.dto.ZyfxqRecordQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 职业飞行棋参与记录数据访问。
 * 动态筛选条件统一维护在 ZyfxqRecordMapper.xml，列表、统计、题目统计和导出共用同一口径。
 */
@Mapper
public interface ZyfxqRecordMapper extends BaseMapper<ZyfxqRecord> {

    /** 按筛选条件查询参与记录，供后台分页和 Excel 导出使用。 */
    List<ZyfxqRecord> selectAdminRecords(@Param("q") ZyfxqRecordQuery query);

    /** 按与列表相同的条件汇总参与、通关、平均耗时和掷骰总次数。 */
    Map<String, Object> selectAdminSummary(@Param("q") ZyfxqRecordQuery query);

    /** 按题型汇总作答数、答对数和超时数，用于“各知识点正确率”。 */
    List<Map<String, Object>> selectQuestionTypeStats(@Param("q") ZyfxqRecordQuery query);

    /** 按题目汇总作答数、答对数、超时数和平均作答用时。 */
    List<Map<String, Object>> selectQuestionStats(@Param("q") ZyfxqRecordQuery query);

    /** 活动查看页使用：按游戏取最近若干条记录，避免一次加载全部历史数据。 */
    List<ZyfxqRecord> selectGameViewRecords(@Param("gameId") Long gameId, @Param("limit") int limit);
}
