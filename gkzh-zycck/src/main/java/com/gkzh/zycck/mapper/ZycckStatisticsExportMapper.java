package com.gkzh.zycck.mapper;

import com.gkzh.zycck.dto.ZycckStatisticsExportRow;
import com.gkzh.zycck.dto.ZycckParticipationSummaryRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/** 学校、活动及可选页面筛选条件下的学生导出查询，按主键分批读取，不使用列表分页。 */
@Mapper
public interface ZycckStatisticsExportMapper {
    /** 读取符合当前筛选条件且主键大于游标的学生数据，包含未完成学生和活动内全部猜猜看游戏。 */
    List<ZycckStatisticsExportRow> selectBatch(@Param("schoolId") Long schoolId,
            @Param("instanceId") Long instanceId, @Param("departmentId") Long departmentId,
            @Param("major") String major, @Param("gender") String gender, @Param("studentNo") String studentNo,
            @Param("statusFilter") String statusFilter,
            @Param("startTime") LocalDateTime startTime, @Param("endExclusive") LocalDateTime endExclusive,
            @Param("afterId") Long afterId, @Param("limit") int limit);

    /** 在所选参与日期和状态范围内，同一用户多个游戏仅取最早参与记录，再按系、专业聚合。 */
    List<ZycckParticipationSummaryRow> selectParticipationSummary(@Param("schoolId") Long schoolId,
            @Param("instanceId") Long instanceId, @Param("statusFilter") String statusFilter, @Param("startTime") LocalDateTime startTime,
            @Param("endExclusive") LocalDateTime endExclusive);
}
