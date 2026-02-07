package com.team.ehr.security;

import com.team.ehr.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static Long getUserId() {
        AuthUser user = getUser();
        return user.getUserId();
    }

    public static UserRole getRole() {
        AuthUser user = getUser();
        return user.getRole();
    }

    private static AuthUser getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser user)) {
            throw new UnauthorizedException("Missing authentication");
        }
        return user;
    }
}
