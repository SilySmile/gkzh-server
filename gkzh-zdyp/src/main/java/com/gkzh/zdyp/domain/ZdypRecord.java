package com.gkzh.zdyp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职得一拼参与记录实体。
 * 一名学生在同一学校、同一活动实例、同一活动游戏下只有一条记录；
 * 每次重闯累加 attempt_no，历史尝试与作答保留，通关时写入岗位与行为分型快照。
 */
@Data
@TableName("gkzh_zdyp_record")
public class ZdypRecord {

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

    /** 游戏类型，固定为 zdyp。 */
    private String gameType;

    /** 参与状态：participating 进行中，finished 已通关。 */
    private String status;

    /** 所选岗位编号；未开始时为空。 */
    private Long careerId;

    /** 所选岗位编码快照。 */
    private String careerCode;

    /** 所选岗位中文名快照，后台改名不影响历史记录。 */
    private String careerName;

    /** 已发起的闯关次数，重闯时累加。 */
    private Integer attemptNo;

    /** 首次尝试是否一次全对：0 否，1 是；用于行为分型与首次通关率。 */
    private String firstAttemptCorrect;

    /** 全部尝试累计放错的卡槽次数。 */
    private Integer totalWrong;

    /** 超时尝试次数。 */
    private Integer timeoutCount;

    /** 首次尝试用时（毫秒），用于急躁粗放型判定与平均用时。 */
    private Long firstAttemptMs;

    /** 通关尝试的最短用时（毫秒）。 */
    private Long bestMs;

    /** 首次通关时写入的行为分型编码：balanced、advantage、weak、rushed。 */
    private String behaviorType;

    /** 行为分型中文名快照。 */
    private String behaviorName;

    /** 个人结论快照，首次通关时写入，后台改文案不影响历史报告。 */
    private String conclusion;

    /** 提升建议快照，首次通关时写入。 */
    private String suggestion;

    /** 扫码进入游戏时间。 */
    private Date scanTime;

    /** 首次点击开始闯关时间。 */
    private Date startTime;

    /** 首次通关时间。 */
    private Date finishTime;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
