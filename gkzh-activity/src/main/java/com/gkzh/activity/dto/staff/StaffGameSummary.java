package com.gkzh.activity.dto.staff;

import lombok.Data;

@Data
public class StaffGameSummary {
    private Long gameId;
    private Long instanceId;
    private String activityTitle;
    private String bizType;
    private String areaTitle;
    private String gameType;
    private String title;
    private Integer sortOrder;
    private Long participantCount;
    private Long completedCount;
    private Long inProgressCount;
    private Long failedCount;
}
