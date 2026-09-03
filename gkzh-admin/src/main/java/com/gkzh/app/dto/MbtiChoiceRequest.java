package com.gkzh.app.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * MBTI选择请求DTO
 *
 * @author gkzh
 * @date 2026-06-02
 */
@Data
public class MbtiChoiceRequest {

    @ApiModelProperty("活动ID")
    private Long activityId;

    @ApiModelProperty("选择的商品ID列表（逗号分隔，如：1,5,8,12）")
    private String productIds;

    @ApiModelProperty("学生ID（由Controller自动填充）")
    private Long studentId;

    @ApiModelProperty("学生姓名（由Controller自动填充）")
    private String studentName;

    @ApiModelProperty("学号（由Controller自动填充）")
    private String studentNo;
}
