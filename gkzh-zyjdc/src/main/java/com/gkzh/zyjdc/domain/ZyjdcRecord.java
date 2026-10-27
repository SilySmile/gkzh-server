package com.gkzh.zyjdc.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职业角斗场学生参与记录实体。
 * 同一学生在同一学校、同一活动实例、同一活动游戏下只保留一条记录；
 * 失败或平局重开只增加尝试次数，不新增记录。
 */
@Data
@TableName("gkzh_zyjdc_record")
public class ZyjdcRecord {

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

    /** 游戏类型，固定为 zyjdc。 */
    private String gameType;

    /** 参与状态：participating 进行中，finished 已完成（仅胜利）。 */
    private String status;

    /** 业务阶段：entered 已扫码，role_selected 已选角色，playing 对弈中，finished 已结束。 */
    private String stage;

    /** 所选角色编号。 */
    private Long roleId;

    /** 所选动物编码快照。 */
    private String roleCode;

    /** 所选动物名称快照。 */
    private String roleName;

    /** 所选角色职场定位快照。 */
    private String jobTitle;

    /** 当前第几局，从 1 开始。 */
    private Integer attemptNo;

    /** 当前棋盘状态 JSON：4×4 棋子、回合、开始时间、落子记录。 */
    private String boardJson;

    /** 对局结果：win 胜利，lose 失败，draw 平局，未结束为空。 */
    private String result;

    /** 结束原因：capture_all 吃光对方，timeout 时间到，no_move 无子可动。 */
    private String resultReason;

    /** 结束结算时剩余秒数。 */
    private Integer remainingSeconds;

    /** 本局双方累计落子步数。 */
    private Integer moveCount;

    /** 扫码进入游戏时间。 */
    private Date scanTime;

    /** 本局开始时间，用于服务端 3 分钟计时。 */
    private Date startTime;

    /** 本局结束时间。 */
    private Date finishTime;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
