package com.ehrplatform.audit.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Service for JWT token validation.
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    private SecretKey key;

    @PostConstruct
    void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("jwt.secret must be at least 32 characters");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validate and parse JWT token
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .clockSkewSeconds(30)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extract user ID from claims
     */
    public Long getUserId(Claims claims) {
        Object sub = claims.get("sub");
        if (sub instanceof Number) {
            return ((Number) sub).longValue();
        }
        try {
            return Long.parseLong(sub.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Extract role from claims
     */
    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    /**
     * Extract admin type from claims (if present)
     */
    public String getAdminType(Claims claims) {
        return claims.get("adminType", String.class);
    }

    /**
     * Extract email from claims
     */
    public String getEmail(Claims claims) {
        return claims.get("email", String.class);
    }

    /**
     * Get authorities/roles from claims
     */
    @SuppressWarnings("unchecked")
    public List<String> getAuthorities(Claims claims) {
        String role = getRole(claims);
        String adminType = getAdminType(claims);
        
        if ("ADMIN".equals(role) && adminType != null) {
            return List.of("ROLE_ADMIN", "ROLE_" + adminType);
        }
        
        return role != null ? List.of("ROLE_" + role) : List.of();
    }
}
