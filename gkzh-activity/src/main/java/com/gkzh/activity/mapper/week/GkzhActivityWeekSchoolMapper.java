package com.gkzh.activity.mapper.week;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.activity.domain.week.GkzhActivityWeekSchool;

@Mapper
public interface GkzhActivityWeekSchoolMapper extends BaseMapper<GkzhActivityWeekSchool> {
    @Update("UPDATE gkzh_activity_week_school SET status = '2' "
            + "WHERE instance_id = #{instanceId} AND school_id = #{schoolId} "
            + "AND (status IS NULL OR status != '2')")
    int markSchoolDeleted(@Param("instanceId") Long instanceId, @Param("schoolId") Long schoolId);

    @Update("UPDATE gkzh_activity_week_school SET min_finish_count = #{config.minFinishCount}, "
            + "lottery_id = #{config.lotteryId}, max_draw_count = #{config.maxDrawCount} "
            + "WHERE instance_id = #{config.instanceId} AND school_id = #{config.schoolId} "
            + "AND (status IS NULL OR status != '2')")
    int updateSchoolConfig(@Param("config") GkzhActivityWeekSchool config);
}
