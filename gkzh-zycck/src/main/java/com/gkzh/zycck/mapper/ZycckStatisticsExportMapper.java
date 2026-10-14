package com.gkzh.zycck.mapper;

import com.gkzh.zycck.dto.ZycckStatisticsExportRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 学校、活动范围内的全量导出查询，按主键分批读取，不使用列表分页。 */
@Mapper
public interface ZycckStatisticsExportMapper {
    /** 读取指定学校活动中主键大于游标的学生数据，包含未完成学生和活动内全部猜猜看游戏。 */
    List<ZycckStatisticsExportRow> selectBatch(@Param("schoolId") Long schoolId,
            @Param("instanceId") Long instanceId, @Param("afterId") Long afterId, @Param("limit") int limit);
}
