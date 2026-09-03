package com.gkzh.app.dto;

import io.swagger.annotations.ApiModelProperty;

public class PatternSelected {
    private Long activityId;
    private Long gameId;
    private String patternIds;
    private String codeGroup;

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getGameId() { return gameId; }

    public void setGameId(Long gameId) { this.gameId = gameId; }

    public String getPatternIds() {
        return patternIds;
    }

    public void setPatternIds(String patternIds) {
        this.patternIds = patternIds;
    }

    public String getCodeGroup() {
        return codeGroup;
    }

    public void setCodeGroup(String codeGroup) {
        this.codeGroup = codeGroup;
    }
}
