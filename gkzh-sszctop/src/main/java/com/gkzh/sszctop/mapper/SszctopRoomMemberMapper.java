package com.gkzh.sszctop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.sszctop.domain.SszctopRoomMember;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/** 房间成员 Mapper。 */
@Mapper
public interface SszctopRoomMemberMapper extends BaseMapper<SszctopRoomMember> {

    /**
     * 查询指定活动、指定用户对应的房间 ID；删除成员后会据此判断房间是否已空。
     * 只通过活动实例关联，避免误清理另一场活动中同名游戏的成员状态。
     */
    @Select("<script>SELECT DISTINCT m.room_id FROM gkzh_sszctop_room_member m " +
            "INNER JOIN gkzh_sszctop_room r ON r.room_id=m.room_id " +
            "WHERE r.instance_id=#{instanceId} AND m.user_id IN " +
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>#{userId}</foreach></script>")
    List<Long> selectRoomIdsByInstanceAndUsers(@Param("instanceId") Long instanceId,
                                                @Param("userIds") Collection<Long> userIds);

    /** 删除用户在指定活动房间中的成员及准备/确认状态数据。 */
    @Delete("<script>DELETE m FROM gkzh_sszctop_room_member m " +
            "INNER JOIN gkzh_sszctop_room r ON r.room_id=m.room_id " +
            "WHERE r.instance_id=#{instanceId} AND m.user_id IN " +
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>#{userId}</foreach></script>")
    int deleteByInstanceAndUsers(@Param("instanceId") Long instanceId,
                                 @Param("userIds") Collection<Long> userIds);
}
