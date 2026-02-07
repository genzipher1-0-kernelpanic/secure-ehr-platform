package com.genzipher.identityservice.Service;

import io.jsonwebtoken.Claims;

import java.time.Instant;
import java.util.Map;

public interface JwtService {

    String generateAccessToken(
            String subject,
            Long userId,
            Map<String, Object> claims
    );

    String generateRefreshToken(
            String subject,
            Long userId,
            Map<String, Object> claims
    );

    String extractSubject(String token);

    Long extractUserId(String token);

    Claims parseAndValidate(String token);

    String extractUsername(String token);

    Instant extractExpiration(String token);

    boolean isTokenValid(String token, String expectedSubject);

}
