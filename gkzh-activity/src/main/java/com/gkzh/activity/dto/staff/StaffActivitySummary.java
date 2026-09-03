package com.gkzh.activity.dto.staff;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class StaffActivitySummary {
    private Long instanceId;
    private String bizType;
    private String title;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endTime;
    private List<StaffGameSummary> games;
}
