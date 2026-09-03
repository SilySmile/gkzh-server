package com.gkzh.activity.mapper.staff;

import java.util.List;
import com.gkzh.activity.dto.staff.StaffActivitySummary;
import com.gkzh.activity.dto.staff.StaffDimensionOption;
import com.gkzh.activity.dto.staff.StaffGameDimensionRow;
import com.gkzh.activity.dto.staff.StaffGameSummary;
import com.gkzh.activity.dto.staff.StaffGameStudentRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GkzhStaffActivityStatisticsMapper {
    String LATEST_PARTICIPATION = " from gkzh_game_participation p join (" +
            "select user_id, max(participation_id) participation_id from gkzh_game_participation " +
            "where game_id = #{gameId} and school_id = #{schoolId} group by user_id" +
            ") latest on latest.participation_id = p.participation_id " +
            "join gkzh_student s on s.user_id = p.user_id and s.school_id = #{schoolId} " +
            "left join gkzh_school_department d on d.department_id = s.department_id " +
            "left join gkzh_school_department c on c.department_id = d.parent_id ";

    @Select("select i.instance_id, i.biz_type, i.title, i.start_time, i.end_time " +
            "from gkzh_activity_week_instance i join gkzh_activity_week_school ws on ws.instance_id = i.instance_id " +
            "where i.biz_type = #{bizType} and i.status = '1' and ws.school_id = #{schoolId} and ws.status = '0' " +
            "order by i.start_time desc, i.instance_id desc limit 1")
    StaffActivitySummary selectCurrentActivity(@Param("bizType") String bizType, @Param("schoolId") Long schoolId);

    @Select("select g.game_id, g.instance_id, i.title activity_title, i.biz_type, a.title area_title, g.game_type, g.title, g.sort_order, " +
            "coalesce(stat.participant_count, 0) participant_count, coalesce(stat.completed_count, 0) completed_count, " +
            "coalesce(stat.in_progress_count, 0) in_progress_count, coalesce(stat.failed_count, 0) failed_count " +
            "from gkzh_activity_game g join gkzh_activity_area a on a.area_id = g.area_id " +
            "join gkzh_activity_week_instance i on i.instance_id = g.instance_id " +
            "left join (select p.game_id, count(*) participant_count, sum(p.status = '1') completed_count, " +
            "sum(p.status = '0') in_progress_count, sum(p.status = '2') failed_count " +
            "from gkzh_game_participation p join (select game_id, user_id, max(participation_id) participation_id " +
            "from gkzh_game_participation where instance_id = #{instanceId} and school_id = #{schoolId} group by game_id, user_id) latest " +
            "on latest.participation_id = p.participation_id group by p.game_id) stat on stat.game_id = g.game_id " +
            "where g.instance_id = #{instanceId} and a.school_id = #{schoolId} and g.status = '0' and a.status = '0' " +
            "order by a.sort_order, a.area_id, g.sort_order, g.game_id")
    List<StaffGameSummary> selectActivityGames(@Param("instanceId") Long instanceId, @Param("schoolId") Long schoolId);

    @Select("select g.game_id, g.instance_id, i.title activity_title, i.biz_type, a.title area_title, g.game_type, g.title, g.sort_order " +
            "from gkzh_activity_game g join gkzh_activity_area a on a.area_id = g.area_id " +
            "join gkzh_activity_week_instance i on i.instance_id = g.instance_id " +
            "where g.game_id = #{gameId} and a.school_id = #{schoolId} limit 1")
    StaffGameSummary selectGame(@Param("gameId") Long gameId, @Param("schoolId") Long schoolId);

    @Select("<script>select coalesce(c.department_id, d.department_id) college_id, " +
            "coalesce(c.title, d.title, '未填写院系') college_name, " +
            "case when c.department_id is null then null else d.department_id end major_id, " +
            "case when c.department_id is null then '未填写专业' else coalesce(d.title, '未填写专业') end major_name, " +
            "coalesce(s.gender, '2') gender, count(*) participant_count, " +
            "sum(p.status = '1') completed_count, sum(p.status = '0') in_progress_count, sum(p.status = '2') failed_count " +
            LATEST_PARTICIPATION +
            "<where>" +
            "<if test='collegeId != null'>and coalesce(c.department_id, d.department_id) = #{collegeId}</if>" +
            "<if test='majorId != null'>and d.department_id = #{majorId}</if>" +
            "<if test='gender != null and gender != \"\"'>and coalesce(s.gender, '2') = #{gender}</if>" +
            "</where> group by college_id, college_name, major_id, major_name, coalesce(s.gender, '2') " +
            "order by college_name, major_name, gender</script>")
    List<StaffGameDimensionRow> selectDimensionRows(@Param("gameId") Long gameId,
            @Param("schoolId") Long schoolId, @Param("collegeId") Long collegeId,
            @Param("majorId") Long majorId, @Param("gender") String gender);

    @Select("<script>select s.student_id, s.student_name, s.student_no, " +
            "coalesce(c.department_id, d.department_id) college_id, coalesce(c.title, d.title, '未填写院系') college_name, " +
            "case when c.department_id is null then null else d.department_id end major_id, " +
            "case when c.department_id is null then '未填写专业' else coalesce(d.title, '未填写专业') end major_name, " +
            "coalesce(s.gender, '2') gender, p.status, p.finish_time " +
            LATEST_PARTICIPATION +
            "<where>" +
            "<if test='collegeId != null'>and coalesce(c.department_id, d.department_id) = #{collegeId}</if>" +
            "<if test='majorId != null'>and d.department_id = #{majorId}</if>" +
            "<if test='gender != null and gender != \"\"'>and coalesce(s.gender, '2') = #{gender}</if>" +
            "</where> order by college_name, major_name, s.student_no, s.student_id</script>")
    List<StaffGameStudentRow> selectStudentRows(@Param("gameId") Long gameId,
            @Param("schoolId") Long schoolId, @Param("collegeId") Long collegeId,
            @Param("majorId") Long majorId, @Param("gender") String gender);

    @Select("select distinct coalesce(c.department_id, d.department_id) id, coalesce(c.title, d.title, '未填写院系') name " +
            LATEST_PARTICIPATION + "order by name")
    List<StaffDimensionOption> selectCollegeOptions(@Param("gameId") Long gameId, @Param("schoolId") Long schoolId);

    @Select("select distinct d.department_id id, coalesce(c.department_id, d.department_id) parent_id, d.title name " +
            LATEST_PARTICIPATION + "where c.department_id is not null order by name")
    List<StaffDimensionOption> selectMajorOptions(@Param("gameId") Long gameId, @Param("schoolId") Long schoolId);
}
