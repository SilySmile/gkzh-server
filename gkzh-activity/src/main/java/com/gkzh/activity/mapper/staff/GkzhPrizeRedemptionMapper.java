package com.gkzh.activity.mapper.staff;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.activity.domain.staff.GkzhPrizeRedemption;
import com.gkzh.activity.dto.staff.StaffPrizeView;
import org.apache.ibatis.annotations.*;

@Mapper
public interface GkzhPrizeRedemptionMapper extends BaseMapper<GkzhPrizeRedemption> {
    @Select("select record_id from lottery_record where redemption_code = #{code} limit 1")
    Long selectRecordIdByRedemptionCode(@Param("code") String code);
    @Select("select r.redemption_code, r.record_id as lottery_record_id, r.user_id, s.student_id, s.school_id, s.student_name, s.student_no, sc.title as school_name, r.prize_title, r.create_time as draw_time, coalesce(pr.status, '0') as status, pr.redeem_time, pr.staff_id, pr.remark from lottery_record r join gkzh_student s on s.user_id = r.user_id left join gkzh_school sc on sc.school_id = s.school_id left join gkzh_prize_redemption pr on pr.lottery_record_id = r.record_id where r.record_id = #{recordId} limit 1")
    StaffPrizeView selectPrizeView(Long recordId);

    @Select("select r.redemption_code, r.record_id as lottery_record_id, r.user_id, s.student_id, s.school_id, s.student_name, s.student_no, sc.title as school_name, r.prize_title, r.create_time as draw_time, coalesce(pr.status, '0') as status, pr.redeem_time, pr.staff_id, pr.remark from lottery_record r join gkzh_student s on s.user_id = r.user_id left join gkzh_school sc on sc.school_id = s.school_id left join gkzh_prize_redemption pr on pr.lottery_record_id = r.record_id left join lottery_prize as lp on r.prize_id = lp.prize_id where s.school_id = #{schoolId} and lp.prize_type != 3 order by r.create_time desc")
    List<StaffPrizeView> selectPrizeViewsBySchool(Long schoolId);

    @Update("update gkzh_prize_redemption set status='1', staff_id=#{staffId}, admin_user_id=#{adminUserId}, redeem_time=now(), remark=#{remark}, update_time=now() where redemption_id=#{redemptionId} and school_id=#{schoolId} and status='0'")
    int redeem(@Param("redemptionId") Long redemptionId, @Param("schoolId") Long schoolId, @Param("staffId") Long staffId, @Param("adminUserId") Long adminUserId, @Param("remark") String remark);
}
