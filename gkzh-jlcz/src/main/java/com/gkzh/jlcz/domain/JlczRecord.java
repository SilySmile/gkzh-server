package com.gkzh.jlcz.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 简历称重参与记录实体。
 * 同一学校、活动实例、活动游戏和用户只保留一条记录；未通关重新称重时 attempt_no 累加。
 */
@Data
@TableName("gkzh_jlcz_record")
public class JlczRecord {

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

    /** 游戏类型，固定为 jlcz。 */
    private String gameType;

    /** 参与状态：participating 进行中，finished 已完成。 */
    private String status;

    /** 称重结果：pass 通关，fail 未通关；未提交为空。 */
    private String result;

    /** 本次勾选砝码总重量（克）。 */
    private Integer totalWeight;

    /** 本次勾选砝码数量。 */
    private Integer selectedCount;

    /** 四大类小计重量快照 JSON，例如 {"core":30,"soft":10}。 */
    private String categoryJson;

    /** 本次勾选砝码快照 JSON，后台改配置不影响历史记录。 */
    private String selectedJson;

    /** 第几次称重；未通关重新称重时累加。 */
    private Integer attemptNo;

    /** 开始称重时间。 */
    private Date startTime;

    /** 提交称重时间。 */
    private Date finishTime;

    /** 本次称重用时（毫秒），从开始称重到提交。 */
    private Long durationMs;

    /** 扫码进入游戏时间。 */
    private Date scanTime;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
