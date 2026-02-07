package com.team.care.dto;

public class AccessDecisionResponse {

    private boolean allowed;
    private String reason;

    public AccessDecisionResponse() {
    }

    public AccessDecisionResponse(boolean allowed, String reason) {
        this.allowed = allowed;
        this.reason = reason;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
