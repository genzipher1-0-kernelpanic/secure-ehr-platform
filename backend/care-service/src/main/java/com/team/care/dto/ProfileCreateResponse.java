package com.team.care.dto;

public class ProfileCreateResponse {

    private Long profileId;

    public ProfileCreateResponse() {
    }

    public ProfileCreateResponse(Long profileId) {
        this.profileId = profileId;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }
}
