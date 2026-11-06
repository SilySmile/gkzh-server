package com.gkzh.zyfxq.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 职业飞行棋题目实体。
 * 题目分两类：career 职业问答、scenario 情景任务卡；每题固定 4 个选项且只有一个正确选项。
 */
@Data
@TableName("gkzh_zyfxq_question")
public class ZyfxqQuestion {

    /** 题目编号。 */
    @TableId(value = "question_id", type = IdType.AUTO)
    private Long questionId;

    /** 题目类型：career 职业问答，scenario 情景任务卡。 */
    private String questionType;

    /** 题干。 */
    private String title;

    /** 题目解析，用于答题反馈展示，可为空。 */
    private String analysis;

    /** 同类型内显示顺序，数值越小越靠前。 */
    private Integer sortOrder;

    /** 状态：0 启用，1 停用；停用题目不会被抽中。 */
    private String status;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;

    /** 选项列表，仅用于后台配置与学生端答题展示，不落库。 */
    @TableField(exist = false)
    private List<ZyfxqOption> options;
}
