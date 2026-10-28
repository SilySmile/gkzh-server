package com.gkzh.nlld.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.nlld.domain.NlldRecord;
import com.gkzh.nlld.domain.dto.NlldRecordQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 能量雷达参与记录数据访问。
 * 动态筛选条件统一维护在 NlldRecordMapper.xml，列表、统计、维度偏好和导出共用同一口径。
 */
@Mapper
public interface NlldRecordMapper extends BaseMapper<NlldRecord> {

    /** 按筛选条件查询参与记录，供后台分页和 Excel 导出使用。 */
    List<NlldRecord> selectAdminRecords(@Param("q") NlldRecordQuery query);

    /** 按与列表相同的条件汇总参与人数、完成人数和 TOP2/多元开放型人数。 */
    Map<String, Object> selectAdminSummary(@Param("q") NlldRecordQuery query);

    /** 按维度汇总被选中次数；每个维度最高 2 次、每人合计 12 次。 */
    List<Map<String, Object>> selectDimensionSelectionCounts(@Param("q") NlldRecordQuery query);

    /** 按维度汇总“得 2 分”的记录数；同一记录同一维度被选中 2 次才计入。 */
    List<Map<String, Object>> selectDimensionTwoPointCounts(@Param("q") NlldRecordQuery query);

    /** 按维度汇总进入 TOP1 的记录数。 */
    List<Map<String, Object>> selectTop1Counts(@Param("q") NlldRecordQuery query);

    /** 按维度汇总进入 TOP2 的记录数。 */
    List<Map<String, Object>> selectTop2Counts(@Param("q") NlldRecordQuery query);

    /** 导出“选择明细”工作表：每条记录 12 行，含关卡、物品、维度和学生信息。 */
    List<Map<String, Object>> selectAnswerDetails(@Param("q") NlldRecordQuery query);

    /** 活动查看页使用：按游戏取最近若干条记录，避免一次加载全部历史数据。 */
    List<NlldRecord> selectGameViewRecords(@Param("gameId") Long gameId, @Param("limit") int limit);
}
