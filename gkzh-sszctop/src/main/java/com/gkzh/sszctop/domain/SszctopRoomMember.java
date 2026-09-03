package com.gkzh.sszctop.domain;
/** 房间成员及确认状态；removedTime 不为空表示已退出当前局。 */
import com.baomidou.mybatisplus.annotation.*; import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.util.Date;
@Data @TableName("gkzh_sszctop_room_member") public class SszctopRoomMember { @TableId(type=IdType.AUTO) private Long memberId; private Long roomId,userId,studentId; private String studentName,studentNo,confirmStatus,removedReason; private Integer confirmVersion; @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8") private Date confirmedAt,removedTime,createTime; }
