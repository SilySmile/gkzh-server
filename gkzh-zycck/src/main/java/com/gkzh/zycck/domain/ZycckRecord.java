package com.gkzh.zycck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.util.Date;

/** 职业猜猜看学生参与记录实体。 */
@Data
@TableName("gkzh_zycck_record")
public class ZycckRecord {
    /** 是否参与人物画像；来自游戏配置，不保存到参与记录表。 */
    @TableField(exist = false)
    private String participatePortrait;
    /** 参与记录主键。 */
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
    /** 院系或专业部门编号。 */
    private Long departmentId;
    /** 专业名称快照。 */
    private String major;
    /** 性别快照：0 男，1 女，其他值表示未知。 */
    private String gender;
    /** 游戏类型，固定为 zycck。 */
    private String gameType;
    /** 参与状态：participating 进行中，finished 已完成。 */
    private String status;
    /** 当前业务阶段，如 scanned、question、exploration、finished。 */
    private String stage;
    /** 当前题号，从 0 开始记录流程进度。 */
    private Integer currentQuestionNo;
    /** 本局抽中的题目编号列表。 */
    private String questionIds;
    /** 本局题目实际作答顺序。 */
    private String questionOrder;
    /** 本局关联的职业编号列表。 */
    private String careerIds;
    /** 本局关联的职业大类编号列表。 */
    private String categoryIds;
    /** 题目和选项快照 JSON，防止题库修改影响历史记录。 */
    private String optionSnapshotJson;
    /** 学生答案 JSON。 */
    private String answerJson;
    /** 学生职业了解程度 JSON。 */
    private String awarenessJson;
    /** 学生已经查看过的职业编号列表。 */
    private String viewedCareerIds;
    /** 学生加入进一步探索清单的职业编号列表。 */
    private String explorationCareerIds;
    /** 创建记录时使用的游戏配置版本。 */
    private String configVersion;
    /** 当前题目开始作答时间。 */
    private Date questionStartTime;
    /** 当前题目累计作答秒数。 */
    private Integer questionElapsedSeconds;
    /** 学生扫码进入游戏的时间。 */
    private Date scanTime;
    /** 学生正式开始游戏的时间。 */
    private Date startTime;
    /** 学生完成游戏的时间。 */
    private Date finishTime;
    /** 参与记录创建时间。 */
    private Date createTime;
    /** 参与记录最后更新时间。 */
    private Date updateTime;
}
