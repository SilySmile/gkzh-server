package com.gkzh.zytj.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职愿探究参与记录实体。
 * 一名学生在同一学校、同一活动实例、同一活动游戏下只有一条记录；提交时写入人格类型与
 * 三段文案快照，后台后续修改 16 型文案不会影响已经生成的职业探索卡。
 */
@Data
@TableName("gkzh_zytj_record")
public class ZytjRecord {

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

    /** 性别快照：男、女，其他值表示未知。 */
    private String gender;

    /** 游戏类型，固定为 zytj。 */
    private String gameType;

    /** 参与状态：participating 进行中，finished 已提交。 */
    private String status;

    /** 人格类型代码快照，例如 ENFP；未提交时为空。 */
    private String typeCode;

    /** 性格特点快照，提交时写入。 */
    private String feature;

    /** 适合职业领域快照，提交时写入。 */
    private String careerField;

    /** 典型职业快照，提交时写入。 */
    private String typicalCareers;

    /** 按列序选择物品的维度代码快照，例如“E,N,T,P”。 */
    private String itemCodes;

    /** 按列序选择物品编号快照，例如“11,13,14,17”。 */
    private String itemIds;

    /** 本局限时快照（秒），0 表示不限时。 */
    private Integer timeLimit;

    /** 是否超时提交：0 否，1 是。 */
    private String isTimeout;

    /** 探究次数，允许重新探究时每次覆盖并自增。 */
    private Integer attemptNo;

    /** 扫码进入游戏时间。 */
    private Date scanTime;

    /** 进入选择页并开始计时的时间。 */
    private Date startTime;

    /** 确认选择时间。 */
    private Date finishTime;

    /** 从进入选择页到确认选择的用时（毫秒）。 */
    private Long durationMs;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
