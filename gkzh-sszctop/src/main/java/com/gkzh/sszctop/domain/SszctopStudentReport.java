package com.gkzh.sszctop.domain;
/** 每位学生独立的结算快照，避免复用房间结果造成数据串扰。 */
import com.baomidou.mybatisplus.annotation.*; import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.util.Date; import java.util.List;
@Data @TableName("gkzh_sszctop_student_report") public class SszctopStudentReport { @TableId(type=IdType.AUTO) private Long reportId; private Long roomId,instanceId,gameId,userId,studentId; private String dimensionSnapshot,careersSnapshot,sharedOrderSnapshot,standardOrderSnapshot,result,reportJson; @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8") private Date createTime;
 /** 维度排序中该职业对应的说明快照，学生端报告仅用于展示，不写入独立字段。 */
 @TableField(exist=false) private List<SszctopDimensionRank> rankDetails;
}
