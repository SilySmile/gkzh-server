package com.gkzh.zmwl.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职面未来参与记录实体。
 * 一名学生在同一学校、同一活动实例、同一活动游戏下只有一条记录；确认选择时写入方向与文案快照，
 * 后台后续修改方向名称、解读或寄语不会影响历史选择卡。
 */
@Data
@TableName("gkzh_zmwl_record")
public class ZmwlRecord {

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

    /** 游戏类型，固定为 zmwl。 */
    private String gameType;

    /** 参与状态：participating 进行中，finished 已完成。 */
    private String status;

    /** 所选发展方向编码快照；未确认时为空。 */
    private String directionCode;

    /** 所选发展方向中文名快照，后台改名称不影响历史记录。 */
    private String directionName;

    /** 所选方向标签称号快照，例如“世界探索者”。 */
    private String tagName;

    /** 方向解读快照，确认选择时写入，后台改文案不影响历史记录。 */
    private String interpretation;

    /** 寄语快照，确认选择时写入，后台改文案不影响历史记录。 */
    private String message;

    /** 所选方向图标地址快照。 */
    private String iconUrl;

    /** 扫码进入游戏时间。 */
    private Date scanTime;

    /** 点击“开启探索”进入选择页时间。 */
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
