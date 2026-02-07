package com.genzipher.identityservice.Service;

import com.genzipher.identityservice.Config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtServiceImpl implements JwtService{

    private final SecretKey key;
    private final JwtProperties props;
    private final Clock clock;

    public JwtServiceImpl(SecretKey key, JwtProperties props, Clock clock) {
        this.key = key;
        this.props = props;
        this.clock = clock;
    }

    @Override
    public String generateAccessToken(String subject, Long userId, Map<String, Object> claims) {
        return buildToken(subject, userId, claims, props.accessTtlSeconds());
    }

    @Override
    public String generateRefreshToken(String subject, Long userId, Map<String, Object> claims) {
        return buildToken(subject, userId, claims, props.refreshTtlSeconds());
    }

    private String buildToken(String subject,
                              Long userId,
                              Map<String, Object> claims,
                              long ttlSeconds) {

        Instant now = Instant.now(clock);
        Instant exp = now.plusSeconds(ttlSeconds);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(props.issuer())
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("uid", userId)
                .claims(claims)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public String extractSubject(String token) {
        return parseClaims(token).getPayload().getSubject();
    }

    @Override
    public Claims parseAndValidate(String token) {
        return parseClaims(token).getPayload();
    }

    @Override
    public Long extractUserId(String token) {
        Object uid = parseClaims(token).getPayload().get("uid");
        if (uid == null) return null;

        if (uid instanceof Integer i) return i.longValue();
        if (uid instanceof Long l) return l;
        if (uid instanceof String s) return Long.parseLong(s);

        throw new IllegalStateException("Invalid uid claim type: " + uid.getClass());
    }

    @Override
    public Instant extractExpiration(String token) {
        Date exp = parseClaims(token).getPayload().getExpiration();
        return exp.toInstant();
    }

    @Override
    public boolean isTokenValid(String token, String expectedSubject) {
        try {
            var claims = parseClaims(token).getPayload();
            return expectedSubject.equals(claims.getSubject())
                    && claims.getExpiration().toInstant().isAfter(Instant.now(clock));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Jws<Claims> parseClaims(String token) {
        // JJWT 0.13 style parsing
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

}
