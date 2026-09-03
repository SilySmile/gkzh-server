package com.gkzh.sszctop.domain;
/** 房间游玩业务日志；房间销毁后仍保留活动、房间号和用户快照。 */
import com.baomidou.mybatisplus.annotation.*; import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.util.Date;
@Data @TableName("gkzh_sszctop_room_log") public class SszctopRoomLog { @TableId(type=IdType.AUTO) private Long logId; private Long instanceId,gameId,roomId,userId,studentId; private String roomCode,roomStatus,eventType,content,studentName,studentNo; @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8") private Date createTime;
 /** 中文事件名由后台接口转换，不写入日志表。 */
 @TableField(exist=false) private String eventName;
 /** 后台房间汇总列表专用字段：一间房只展示一行。 */
 @TableField(exist=false) private Integer logCount,memberCount;
 @TableField(exist=false) private String roomStatusName;
 /** 后台活动用户记录列表的活动名称，来自活动实例表，不写入房间日志表。 */
 @TableField(exist=false) private String activityTitle;
}
