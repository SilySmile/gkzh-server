package com.gkzh.zcyltzj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.zcyltzj.domain.ZcyltzjRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** 骰子局参与记录数据访问。 */
@Mapper
public interface ZcyltzjRecordMapper extends BaseMapper<ZcyltzjRecord> {

    /**
     * 后台参与记录分页查询；动态条件统一维护在 ZcyltzjRecordMapper.xml。
     *
     * @param instanceId   活动实例编号
     * @param schoolId     学校编号
     * @param departmentId 院系编号，查询时包含其下级专业
     * @param major        专业名称模糊条件
     * @param genderValue  性别中文值：男、女、其他
     * @param studentNo    学号模糊条件
     * @param pointNo      骰子点数 1-6
     * @param status       finished 已完成，inProgress 进行中，null 表示全部
     * @param startDate    参与日期开始，含当天零点
     * @param endDate      参与日期结束次日零点，不含
     * @return 当前条件下的参与记录
     */
    List<ZcyltzjRecord> selectAdminRecords(@Param("instanceId") Long instanceId,
                                           @Param("schoolId") Long schoolId,
                                           @Param("departmentId") Long departmentId,
                                           @Param("major") String major,
                                           @Param("genderValue") String genderValue,
                                           @Param("studentNo") String studentNo,
                                           @Param("pointNo") Integer pointNo,
                                           @Param("status") String status,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * 按与列表相同的条件汇总参与人数、完成人数、颜色分布和平均压力值。
     *
     * @return total、finished、redCount、greenCount、yellowCount、avgPressure
     */
    Map<String, Object> selectAdminSummary(@Param("instanceId") Long instanceId,
                                           @Param("schoolId") Long schoolId,
                                           @Param("departmentId") Long departmentId,
                                           @Param("major") String major,
                                           @Param("genderValue") String genderValue,
                                           @Param("studentNo") String studentNo,
                                           @Param("pointNo") Integer pointNo,
                                           @Param("status") String status,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}
