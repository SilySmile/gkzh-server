package com.gkzh.activity.mapper.staff;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.activity.domain.staff.GkzhPrizeRedemptionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GkzhPrizeRedemptionLogMapper extends BaseMapper<GkzhPrizeRedemptionLog> {
    @Select("select l.*, " +
            "case when l.staff_id is not null then coalesce(ss.staff_name, ss.user_name, concat('工作人员#', l.staff_id)) " +
            "when l.admin_user_id is not null then coalesce(u.nick_name, u.user_name, concat('管理员#', l.admin_user_id)) " +
            "when l.action = 'ADMIN_REDEEM' then '后台管理员（历史记录）' else '系统' end as operator_name, " +
            "case when l.staff_id is not null then ss.user_name when l.admin_user_id is not null then u.user_name else null end as operator_account, " +
            "case when l.staff_id is not null then 'STAFF' when l.admin_user_id is not null or l.action = 'ADMIN_REDEEM' then 'ADMIN' else 'SYSTEM' end as operator_type " +
            "from gkzh_prize_redemption_log l " +
            "left join gkzh_school_staff ss on ss.staff_id = l.staff_id " +
            "left join sys_user u on u.user_id = l.admin_user_id " +
            "where l.lottery_record_id = #{recordId} order by l.create_time desc")
    List<GkzhPrizeRedemptionLog> selectByRecordId(Long recordId);
}
