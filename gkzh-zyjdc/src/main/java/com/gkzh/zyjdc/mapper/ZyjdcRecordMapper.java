package com.gkzh.zyjdc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.zyjdc.domain.ZyjdcRecord;
import com.gkzh.zyjdc.domain.dto.ZyjdcRecordQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 职业角斗场参与记录数据访问。
 * 动态筛选条件统一维护在 ZyjdcRecordMapper.xml，列表、统计和导出共用同一口径。
 */
@Mapper
public interface ZyjdcRecordMapper extends BaseMapper<ZyjdcRecord> {

    /** 按筛选条件查询参与记录，供后台分页和 Excel 导出使用。 */
    List<ZyjdcRecord> selectAdminRecords(@Param("q") ZyjdcRecordQuery query);

    /** 按与列表相同的条件汇总参与人数、完成人数、胜负分布和平均尝试次数。 */
    Map<String, Object> selectAdminSummary(@Param("q") ZyjdcRecordQuery query);

    /** 按角色汇总选择人数和完成人数；只包含已有记录的角色。 */
    List<Map<String, Object>> selectRoleCounts(@Param("q") ZyjdcRecordQuery query);

    /** 按对局结果汇总人数；未结束的记录不计入。 */
    List<Map<String, Object>> selectResultCounts(@Param("q") ZyjdcRecordQuery query);

    /** 活动查看页使用：按游戏取最近若干条记录，避免一次加载全部历史数据。 */
    List<ZyjdcRecord> selectGameViewRecords(@Param("gameId") Long gameId, @Param("limit") int limit);
}
