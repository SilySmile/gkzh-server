package com.gkzh.cyzs.vo;

import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;
@Data

public class SubmitAnswerRequest {
    @Size(min = 5, max = 5, message = "必须回答5道题")
    private List<AnswerRequest> answers;
}
