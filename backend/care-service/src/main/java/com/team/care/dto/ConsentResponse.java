package com.team.care.dto;

public class ConsentResponse {

    private Long consentId;

    public ConsentResponse() {
    }

    public ConsentResponse(Long consentId) {
        this.consentId = consentId;
    }

    public Long getConsentId() {
        return consentId;
    }

    public void setConsentId(Long consentId) {
        this.consentId = consentId;
    }
}
