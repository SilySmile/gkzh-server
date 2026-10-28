package com.gkzh.nlld.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 能量雷达学生参与记录实体。
 * 同一学生在同一学校、同一活动实例、同一活动游戏下只保留一条记录；
 * 6 关全部提交后记录完成，不提供重试或重复测评。
 */
@Data
@TableName("gkzh_nlld_record")
public class NlldRecord {

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

    /** 游戏类型，固定为 nlld。 */
    private String gameType;

    /** 参与状态：participating 进行中，finished 已完成。 */
    private String status;

    /** 业务阶段：entered 已扫码，choosing 选择中，finished 已完成。 */
    private String stage;

    /** 已提交关数，取值 0-6。 */
    private Integer answeredRounds;

    /** TOP1 维度编号快照；多元开放型为空。 */
    private Long top1DimensionId;

    /** TOP1 维度名称快照；多元开放型为空。 */
    private String top1DimensionName;

    /** TOP2 维度编号快照；多元开放型为空。 */
    private Long top2DimensionId;

    /** TOP2 维度名称快照；多元开放型为空。 */
    private String top2DimensionName;

    /** 结果类型：top2 两个维度并列最高分，open 多元开放型；未结束为空。 */
    private String resultType;

    /** 15 个维度得分与结果类型快照 JSON，供报告和统计稳定展示。 */
    private String scoreJson;

    /** 扫码进入游戏时间。 */
    private Date scanTime;

    /** 完成时间。 */
    private Date finishTime;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
