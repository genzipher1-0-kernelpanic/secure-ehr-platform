package com.team.ehr.dto;

public class EhrCreateResponse {

    private Integer version;

    public EhrCreateResponse() {
    }

    public EhrCreateResponse(Integer version) {
        this.version = version;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
