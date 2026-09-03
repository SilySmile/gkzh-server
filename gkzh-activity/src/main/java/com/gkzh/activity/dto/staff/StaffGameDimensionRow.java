package com.gkzh.activity.dto.staff;

import lombok.Data;

@Data
public class StaffGameDimensionRow {
    private Long collegeId;
    private String collegeName;
    private Long majorId;
    private String majorName;
    private String gender;
    private Long participantCount;
    private Long completedCount;
    private Long inProgressCount;
    private Long failedCount;
}
