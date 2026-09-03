package com.gkzh.sszctop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.sszctop.domain.SszctopRoomLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SszctopRoomLogMapper extends BaseMapper<SszctopRoomLog> {
    // 复杂查询统一放在同目录 XML 中；日志不依赖房间主表或成员表。
    List<SszctopRoomLog> selectUserActivitySummaries(@Param("activityName") String activityName, @Param("roomCode") String roomCode, @Param("keyword") String keyword);

    // 房间销毁后仍可按日志中的历史 room_id 查看详情；同时隐藏排序更新和准备操作噪声。
    List<SszctopRoomLog> selectRoomDetails(@Param("roomId") Long roomId);
}
