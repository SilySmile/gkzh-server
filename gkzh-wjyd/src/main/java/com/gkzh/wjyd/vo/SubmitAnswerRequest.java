package com.gkzh.wjyd.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;
@Data

public class SubmitAnswerRequest {
    @Size(min = 3, max = 3, message = "必须回答3道题")
    private List<AnswerRequest> answers;
}
