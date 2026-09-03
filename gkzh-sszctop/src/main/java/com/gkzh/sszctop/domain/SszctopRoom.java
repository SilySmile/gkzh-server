package com.gkzh.sszctop.domain;
/** 游戏房间状态、共享排序和乐观锁版本。 */
import com.baomidou.mybatisplus.annotation.*; import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.util.Date;
@Data @TableName("gkzh_sszctop_room") public class SszctopRoom { @TableId(type=IdType.AUTO) private Long roomId; private Long instanceId,gameId,ownerUserId,dimensionId; private String roomCode,mode,careerIds,sharedOrderIds,status; private Integer orderVersion; @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8") private Date createTime,updateTime,finishTime; }
