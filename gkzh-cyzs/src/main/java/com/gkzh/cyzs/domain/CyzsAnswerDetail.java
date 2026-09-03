package com.gkzh.cyzs.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 职场危机对象 biz_question
 * 
 * @author gkzh
 * @date 2025-10-13
 */
@Data
public class CyzsAnswerDetail
{
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roundId;
    private Long questionId;
    private String userAnswer;
    private Integer isCorrect;

}
