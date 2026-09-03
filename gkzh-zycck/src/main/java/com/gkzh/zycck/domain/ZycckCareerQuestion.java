package com.gkzh.zycck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("gkzh_zycck_career_question")
public class ZycckCareerQuestion {
    @TableId(value = "question_id", type = IdType.AUTO) private Long questionId;
    private Long categoryId;
    private Long careerId;
    private String careerName;
    private String title;
    private String imageUrl;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String explanation;
    private String status;
    private Date createTime;
    private Date updateTime;
}
