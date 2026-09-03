package com.gkzh.qdqt.dto;

import lombok.Data;

@Data
public class StudentLoginSimpleRequest {
    private Long schoolId;
    private String studentNo;
    private String password;
}
