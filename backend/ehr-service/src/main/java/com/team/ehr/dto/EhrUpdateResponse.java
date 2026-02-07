package com.team.ehr.dto;

public class EhrUpdateResponse {

    private Integer newVersion;

    public EhrUpdateResponse() {
    }

    public EhrUpdateResponse(Integer newVersion) {
        this.newVersion = newVersion;
    }

    public Integer getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(Integer newVersion) {
        this.newVersion = newVersion;
    }
}
