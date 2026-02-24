package com.team.care.controller;

import com.team.care.dto.DoctorProfileDto;
import com.team.care.security.JwtService;
import com.team.care.service.ProfileService;
import com.team.care.service.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctors")
public class DoctorSelfController {
    private final ProfileService profileService;
    private final JwtService jwtService;

    public DoctorSelfController(ProfileService profileService, JwtService jwtService) {
        this.profileService = profileService;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public DoctorProfileDto getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authorization.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        Claims claims = jwtService.validateToken(token);
        Long userId = jwtService.getUserId(claims);
        if (userId == null) {
            throw new UnauthorizedException("Invalid token payload");
        }

        return profileService.getDoctorByUserId(userId);
    }
}
