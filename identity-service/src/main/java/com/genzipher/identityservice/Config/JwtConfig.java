package com.genzipher.identityservice.Config;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public SecretKey jwtSigningKey(JwtProperties props) {
        byte[] keyBytes;

        // Try base64 decode; if it fails, fallback to raw string bytes.
        try {
            keyBytes = Decoders.BASE64.decode(props.secret());
        } catch (IllegalArgumentException ex) {
            keyBytes = props.secret().getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) { // HS256 needs >= 256-bit key
            throw new IllegalStateException("JWT secret is too short. Use 32+ bytes (or base64 of 32+ bytes).");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

}
