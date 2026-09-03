package com.gkzh.zycck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("gkzh_zycck_career_question")
public class ZycckCareerQuestion {
    @TableId(value = "career_question_id", type = IdType.AUTO) private Long careerQuestionId;
    private Long categoryId;
    private String careerName;
    private String oneLineIntro;
    private String mainWork;
    private String dayExample;
    private String whyExists;
    private String careerImageUrl;
    private String questionImageUrl;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Long optionACareerId;
    private Long optionBCareerId;
    private Long optionCCareerId;
    private Long optionDCareerId;
    private String correctOptionKey;
    private String explanation;
    private String drawCandidate;
    private Integer sortOrder;
    private String status;
    private Date createTime;
    private Date updateTime;
}
