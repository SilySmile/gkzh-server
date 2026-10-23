package com.gkzh.zcyltzj.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/** 骰子局学生参与记录实体，一条记录对应一名学生在一个活动游戏中的一局。 */
@Data
@TableName("gkzh_zcyltzj_record")
public class ZcyltzjRecord {
    /** 参与记录编号。 */
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;
    /** 学校编号。 */
    private Long schoolId;
    /** 活动周实例编号。 */
    private Long instanceId;
    /** 活动游戏编号。 */
    private Long gameId;
    /** 平台用户编号。 */
    private Long userId;
    /** 学生编号。 */
    private Long studentId;
    /** 学号快照。 */
    private String studentNo;
    /** 学生姓名快照。 */
    private String studentName;
    /** 专业（二级部门）编号，其父部门才是院系。 */
    private Long departmentId;
    /** 专业名称快照。 */
    private String major;
    /** 性别快照：0 男，1 女，其他值表示未知。 */
    private String gender;
    /** 游戏类型，固定为 zcyltzj。 */
    private String gameType;
    /** 参与状态：participating 进行中，finished 已完成。 */
    private String status;
    /** 业务阶段：entered 已扫码，answering 已掷骰子答题中，finished 已完成。 */
    private String stage;
    /** 本局掷出的骰子点数：1-6。 */
    private Integer pointNo;
    /** 本局点数名称快照。 */
    private String pointName;
    /** 本局点数、试题和选项快照 JSON，题库修改不影响进行中的记录。 */
    private String questionSnapshotJson;
    /** 学生答案 JSON，保存每题题号与选项。 */
    private String answersJson;
    /** 红色（压力高）答案数量。 */
    private Integer redCount;
    /** 绿色（压力低）答案数量。 */
    private Integer greenCount;
    /** 压力值（红色答案占比），0-100。 */
    private Integer pressureValue;
    /** 结果颜色：red 红色，green 绿色，yellow 黄色。 */
    private String color;
    /** 扫码进入游戏时间。 */
    private Date scanTime;
    /** 掷骰子并开始答题的时间。 */
    private Date startTime;
    /** 完成答题的时间。 */
    private Date finishTime;
    /** 创建时间。 */
    private Date createTime;
    /** 最后更新时间。 */
    private Date updateTime;
}
