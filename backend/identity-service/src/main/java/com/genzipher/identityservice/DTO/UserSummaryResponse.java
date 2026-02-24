package com.genzipher.identityservice.DTO;

import com.genzipher.identityservice.Model.RoleName;

import java.time.Instant;

public record UserSummaryResponse(
        Long id,
        String email,
        String status,
        RoleName role,
        Instant createdAt
) {}
