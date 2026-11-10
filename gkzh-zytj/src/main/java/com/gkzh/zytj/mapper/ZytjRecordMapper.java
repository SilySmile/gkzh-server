package com.gkzh.zytj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.zytj.domain.ZytjRecord;
import com.gkzh.zytj.domain.dto.ZytjRecordQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 职愿探究参与记录 Mapper。
 * 列表、汇总、16 型分布、维度倾向、院系分布和活动查看共用同一套动态条件，
 * SQL 统一写在 XML 中便于排查和调整。
 */
@Mapper
public interface ZytjRecordMapper extends BaseMapper<ZytjRecord> {

    /** 后台参与记录查询：按活动、人格类型、状态、学生属性和参与日期动态筛选。 */
    List<ZytjRecord> selectAdminRecords(@Param("q") ZytjRecordQuery query);

    /** 统计汇总：参与人数、已提交人数、超时人数与平均用时，与列表同一口径。 */
    Map<String, Object> selectAdminSummary(@Param("q") ZytjRecordQuery query);

    /** 16 型分布，连同当前配置的性格特点与职业文案，用于学校端“各人格类型解读”页。 */
    List<Map<String, Object>> selectTypeStats(@Param("q") ZytjRecordQuery query);

    /** 四个维度的字母倾向分布（E/I、S/N、T/F、J/P），按所选位置拆分 item_codes。 */
    List<Map<String, Object>> selectDimensionStats(@Param("q") ZytjRecordQuery query);

    /** 各院系的参与与提交人数；院系取专业部门的父部门。 */
    List<Map<String, Object>> selectDepartmentSummary(@Param("q") ZytjRecordQuery query);

    /** 各院系在每个维度字母上的人数，用于院系倾向与 Excel 明细。 */
    List<Map<String, Object>> selectDepartmentLetterCounts(@Param("q") ZytjRecordQuery query);

    /** 活动查看页只取最近若干条记录，限制数据量。 */
    List<ZytjRecord> selectGameViewRecords(@Param("gameId") Long gameId, @Param("limit") int limit);
}
