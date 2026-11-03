package com.gkzh.zyfxq.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职业飞行棋学生参与记录实体。
 * 同一学生在同一学校、同一活动实例、同一活动游戏下只保留一条记录；
 * 未通关重开一局只重置本局状态并累加 attempt_no，不会新增参与记录。
 */
@Data
@TableName("gkzh_zyfxq_record")
public class ZyfxqRecord {

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

    /** 游戏类型，固定为 zyfxq。 */
    private String gameType;

    /** 记录状态：playing 对局中，finished 本局已结束。 */
    private String status;

    /** 对局结果：win 通关，lose 未通关；对局中为空。 */
    private String result;

    /** 第几局，未通关重开后累加；参与记录仍只有一条。 */
    private Integer attemptNo;

    /** 当前所在格号，0 表示仍在起点。 */
    private Integer position;

    /** 本局掷骰次数。 */
    private Integer rollCount;

    /** 本局累计前行总步数（骰子点数之和，不含后退）。 */
    private Integer stepTotal;

    /** 最近一次掷出的点数。 */
    private Integer diceValue;

    /** 本局已作答题数。 */
    private Integer answerCount;

    /** 本局答对题数。 */
    private Integer correctCount;

    /** 本局超时题数。 */
    private Integer timeoutCount;

    /** 当前待答题目编号；无待答题目为空。 */
    private Long currentQuestionId;

    /** 当前题目下发时间，用于服务端计算作答耗时与超时。 */
    private Date currentQuestionTime;

    /** 触发当前题目的格号。 */
    private Integer currentCellNo;

    /** 本局总时长（秒）快照，开局时固化。 */
    private Integer totalSeconds;

    /** 本局单题限时（秒）快照，开局时固化。 */
    private Integer questionSeconds;

    /** 本局答错后退格数快照，开局时固化。 */
    private Integer backSteps;

    /** 本局骰子面数快照，开局时固化；单骰取 1 到该值。 */
    private Integer diceFaces;

    /** 本局开始时间；未点击开始游戏时为空。 */
    private Date startTime;

    /** 本局结束时间：通关时间或超时截止时间。 */
    private Date endTime;

    /** 本局总耗时（毫秒），结束后写入，未结束为空。 */
    private Integer durationMs;

    /** 首次扫码进入游戏时间。 */
    private Date scanTime;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
