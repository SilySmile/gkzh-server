package com.gkzh.sszctop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.sszctop.domain.SszctopRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/** 房间主表 Mapper。 */
@Mapper
public interface SszctopRoomMapper extends BaseMapper<SszctopRoom> {

    /**
     * 从候选房间中筛出已没有活跃成员的房间。
     * 已退出成员不会阻止物理清理；调用方会在删除房间前同步删除其遗留成员行。
     */
    @Select("<script>SELECT r.room_id FROM gkzh_sszctop_room r " +
            "LEFT JOIN gkzh_sszctop_room_member m ON m.room_id=r.room_id " +
            "WHERE r.room_id IN " +
            "<foreach collection='roomIds' item='roomId' open='(' separator=',' close=')'>#{roomId}</foreach> " +
            "GROUP BY r.room_id HAVING SUM(CASE WHEN m.member_id IS NOT NULL AND m.removed_time IS NULL THEN 1 ELSE 0 END)=0</script>")
    List<Long> selectEmptyRoomIds(@Param("roomIds") Collection<Long> roomIds);
}
