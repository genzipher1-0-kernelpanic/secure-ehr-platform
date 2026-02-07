package com.team.care.controller;

import com.team.care.dto.DoctorProfileDto;
import com.team.care.service.JwtService;
import com.team.care.service.ProfileService;
import com.team.care.service.exception.ForbiddenException;
import com.team.care.service.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctors")
public class DoctorSelfController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final ProfileService profileService;

    public DoctorSelfController(JwtService jwtService, ProfileService profileService) {
        this.jwtService = jwtService;
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public DoctorProfileDto getMyProfile(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException("Missing Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        Claims claims = jwtService.parseAndValidate(token);
        if (claims == null) {
            throw new UnauthorizedException("Invalid token");
        }

        Object role = claims.get("role");
        if (role == null || !"DOCTOR".equalsIgnoreCase(role.toString())) {
            throw new ForbiddenException("Not a doctor token");
        }

        Object userIdClaim = claims.get("uid");
        if (userIdClaim == null) {
            throw new UnauthorizedException("Missing uid claim");
        }

        Long userId = parseUserId(userIdClaim);
        return profileService.getDoctorByUserId(userId);
    }

    private Long parseUserId(Object userIdClaim) {
        if (userIdClaim instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(userIdClaim.toString());
        } catch (NumberFormatException ex) {
            throw new UnauthorizedException("Invalid userid claim");
        }
    }
}
