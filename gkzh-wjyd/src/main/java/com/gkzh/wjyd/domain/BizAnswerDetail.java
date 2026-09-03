package com.gkzh.wjyd.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 职场危机对象 biz_question
 * 
 * @author gkzh
 * @date 2025-10-13
 */
@Data
public class BizAnswerDetail
{
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roundId;
    private Long questionId;
    private String userAnswer;
    private Integer isCorrect;

}
