package com.gkzh.zyfxq.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职业飞行棋棋盘格子配置实体。
 * 19 个格子按行进顺序编号；行列位置由小程序固定布局渲染，本表只维护类型、文案和底色。
 */
@Data
@TableName("gkzh_zyfxq_cell")
public class ZyfxqCell {

    /** 格子编号（自增主键）。 */
    @TableId(value = "cell_id", type = IdType.AUTO)
    private Long cellId;

    /** 棋盘格号，取值 1-19，按行进顺序编号。 */
    private Integer cellNo;

    /** 格子类型：normal 普通格，question 答题格，end 终点。 */
    private String cellType;

    /** 答题格题目类型：career 职业问答，scenario 情景任务卡；普通格与终点为空。 */
    private String questionType;

    /** 格子底色（十六进制），按设计稿采样，供小程序渲染。 */
    private String cellColor;

    /** 格子显示文案，例如职业问答、情景任务卡、终点；普通格为空。 */
    private String cellLabel;

    /** 状态：0 启用，1 停用；停用的答题格按普通格处理。 */
    private String status;

    /** 创建时间。 */
    private Date createTime;

    /** 最后更新时间。 */
    private Date updateTime;
}
