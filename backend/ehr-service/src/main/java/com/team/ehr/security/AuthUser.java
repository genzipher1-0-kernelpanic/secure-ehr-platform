package com.team.ehr.security;

public class AuthUser {

    private final Long userId;
    private final UserRole role;

    public AuthUser(Long userId, UserRole role) {
        this.userId = userId;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public UserRole getRole() {
        return role;
    }
}
