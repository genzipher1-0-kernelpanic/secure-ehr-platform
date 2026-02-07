package com.team.care.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final String issuer;
    private final String audience;
    private final String secret;
    private final long clockSkewSeconds;
    private SecretKey key;

    public JwtService(
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.audience}") String audience,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.clockSkewSeconds:0}") long clockSkewSeconds
    ) {
        this.issuer = issuer;
        this.audience = audience;
        this.secret = secret;
        this.clockSkewSeconds = clockSkewSeconds;
    }

    @PostConstruct
    void init() {
        log.info("JWT Config - issuer: '{}', audience: '{}', secret length: {}, clockSkew: {}",
                issuer, audience, secret != null ? secret.length() : 0, clockSkewSeconds);
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("jwt.secret must be at least 32 characters");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims parseAndValidate(String token) {
        try {
            var builder = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .clockSkewSeconds(clockSkewSeconds);
            if (audience != null && !audience.isBlank()) {
                builder.requireAudience(audience);
            }
            return builder.build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("JWT validation failed: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
            return null;
        }
    }
}
