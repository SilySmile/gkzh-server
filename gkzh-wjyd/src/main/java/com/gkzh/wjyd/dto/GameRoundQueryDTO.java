package com.gkzh.wjyd.dto;

import com.gkzh.common.core.domain.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 答题结果查询参数DTO
 */
@Data
public class GameRoundQueryDTO extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 学号 */
    private String username;

    /** 姓名 */
    private String nickname;

    /** 是否通关 */
    private String isSuccess;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;


}
