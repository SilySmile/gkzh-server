package com.gkzh.app.dto;

import com.gkzh.xycc.domain.Career;
import com.gkzh.xycc.domain.WorkEnv;

import java.util.List;

public class XyccResult {
    private String code;
    private List<Career> careers;
    private List<WorkEnv> workEnvs;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Career> getCareers() {
        return careers;
    }

    public void setCareers(List<Career> careers) {
        this.careers = careers;
    }

    public List<WorkEnv> getWorkEnvs() {
        return workEnvs;
    }

    public void setWorkEnvs(List<WorkEnv> workEnvs) {
        this.workEnvs = workEnvs;
    }
}
