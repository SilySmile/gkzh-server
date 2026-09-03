package com.gkzh.activity.dto.staff;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class StaffGameStudentRow {
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long collegeId;
    private String collegeName;
    private Long majorId;
    private String majorName;
    private String gender;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;
}
