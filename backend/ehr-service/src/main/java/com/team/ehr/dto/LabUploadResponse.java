package com.team.ehr.dto;

public class LabUploadResponse {

    private String objectId;

    public LabUploadResponse() {
    }

    public LabUploadResponse(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
