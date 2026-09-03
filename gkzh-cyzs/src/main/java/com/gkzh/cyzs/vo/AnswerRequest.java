package com.gkzh.cyzs.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerRequest {
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    @NotBlank(message = "答案不能为空")
    @Pattern(regexp = "[ABC]", message = "答案必须是A、B或C")
    private String userAnswer;
}
