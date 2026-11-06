package com.gkzh.zycck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/** 职业猜猜看职业资料及竞猜题目实体。 */
@Data
@TableName("gkzh_zycck_career_question")
public class ZycckCareerQuestion {
    /** 职业资料主键，同时作为题目编号使用。 */
    @TableId(value = "career_question_id", type = IdType.AUTO)
    private Long careerQuestionId;
    /** 所属职业大类编号。 */
    private Long categoryId;
    /** 职业名称。 */
    private String careerName;
    /** 是否有对应竞猜题：1 有题目，0 仅作为探索职业。 */
    private String hasQuestion;
    /** 职业一句话介绍。 */
    private String oneLineIntro;
    /** 职业主要工作内容。 */
    private String mainWork;
    /** 职业一天工作示例。 */
    private String dayExample;
    /** 该职业产生或存在的原因。 */
    private String whyExists;
    /** 职业展示图片地址。 */
    private String careerImageUrl;
    /** 竞猜题目场景图片地址。 */
    private String questionImageUrl;
    /** 竞猜题 A 选项文字。 */
    private String optionA;
    /** 竞猜题 B 选项文字。 */
    private String optionB;
    /** 竞猜题 C 选项文字。 */
    private String optionC;
    /** 竞猜题 D 选项文字。 */
    private String optionD;
    /** A 选项关联的职业资料编号。 */
    private Long optionACareerId;
    /** B 选项关联的职业资料编号。 */
    private Long optionBCareerId;
    /** C 选项关联的职业资料编号。 */
    private Long optionCCareerId;
    /** D 选项关联的职业资料编号。 */
    private Long optionDCareerId;
    /** 正确选项标识：A、B、C 或 D。 */
    private String correctOptionKey;
    /** 答案解析说明。 */
    private String explanation;
    /** 是否进入随机抽题候选池：0 否，1 是。 */
    private String drawCandidate;
    /** 同一职业大类中的显示顺序。 */
    private Integer sortOrder;
    /** 状态：0 正常，1 停用。 */
    private String status;
    /** 创建时间。 */
    private Date createTime;
    /** 最后更新时间。 */
    private Date updateTime;
}
