package com.gkzh.zdyp.domain.dto;

import lombok.Data;

/**
 * 职得一拼单次放置请求：学生把某张卡片放进某个卡槽。
 * 服务端只采信卡槽编码与卡片编号，维度与正确性一律由服务端判定。
 */
@Data
public class ZdypPlacement {

    /** 卡槽编码：content、skill、weakness、prospect。 */
    private String slotCode;

    /** 放入该卡槽的卡片编号。 */
    private Long cardId;

    /** 从本局开始到放入该卡槽的用时（毫秒），仅用于统计单块耗时。 */
    private Long elapsedMs;
}
