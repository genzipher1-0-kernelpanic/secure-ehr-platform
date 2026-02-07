package com.team.ehr.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    // For multi-instance deployments, replace this in-memory map with Redis-backed buckets.
    private final int tokensPerMinute;
    private final int bucketCapacity;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimiterService(
            @Value("${rateLimit.export.tokensPerMinute}") int tokensPerMinute,
            @Value("${rateLimit.export.bucketCapacity}") int bucketCapacity
    ) {
        this.tokensPerMinute = tokensPerMinute;
        this.bucketCapacity = bucketCapacity;
    }

    public boolean tryConsume(String key) {
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(bucketCapacity, tokensPerMinute));
        return bucket.tryConsume();
    }

    private static class TokenBucket {
        private final int capacity;
        private final int refillPerMinute;
        private double tokens;
        private Instant lastRefill;

        private TokenBucket(int capacity, int refillPerMinute) {
            this.capacity = capacity;
            this.refillPerMinute = refillPerMinute;
            this.tokens = capacity;
            this.lastRefill = Instant.now();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        private void refill() {
            Instant now = Instant.now();
            double minutes = Math.max(0, (now.toEpochMilli() - lastRefill.toEpochMilli()) / 60000.0);
            if (minutes > 0) {
                tokens = Math.min(capacity, tokens + minutes * refillPerMinute);
                lastRefill = now;
            }
        }
    }
}
