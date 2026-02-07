package com.genzipher.identityservice.DTO;

public record LoginResponse(
        String access_token,
        String refresh_token,
        String role
) {}
