package com.genzipher.identityservice.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret,           // your secret (prefer base64 or long random)
        String issuer,           // e.g. "genzipher-identity"
        long accessTtlSeconds,   // e.g. 900 (15m)
        long refreshTtlSeconds,  // e.g. 604800 (7d)
        long maxIdleSeconds      // inactivity window, e.g. 1800 (30m)
) {}