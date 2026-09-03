package com.gkzh.activity.mapper.staff;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.activity.domain.staff.GkzhSchoolStaff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GkzhSchoolStaffMapper extends BaseMapper<GkzhSchoolStaff> {
    @Select("select st.*, sc.title as school_name from gkzh_school_staff st left join gkzh_school sc on sc.school_id=st.school_id where st.user_name = #{userName} limit 1")
    GkzhSchoolStaff selectEnabledByUserName(String userName);

    @Select("select st.*, sc.title as school_name from gkzh_school_staff st left join gkzh_school sc on sc.school_id=st.school_id order by st.staff_id desc")
    List<GkzhSchoolStaff> selectStaffList();

    @Update("update gkzh_school_staff set password=#{password}, update_time=now() where staff_id=#{staffId}")
    int updatePassword(@Param("staffId") Long staffId, @Param("password") String password);
}
