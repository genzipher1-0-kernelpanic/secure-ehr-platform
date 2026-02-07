package com.genzipher.identityservice.Handler;

import com.genzipher.identityservice.Config.JwtProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
public class InactivityFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redis;
    private final JwtProperties props;

    public InactivityFilter(StringRedisTemplate redis, JwtProperties props) {
        this.redis = redis;
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Only enforce for authenticated requests
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != "anonymousUser") {
            String subject = auth.getName(); // email
            String key = "last_activity:" + subject;

            String last = redis.opsForValue().get(key);
            long now = Instant.now().getEpochSecond();

            if (last != null) {
                long lastTs = Long.parseLong(last);
                long idle = now - lastTs;

                if (idle > props.maxIdleSeconds()) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("""
                            {"error":"SESSION_IDLE_TIMEOUT","message":"Logged out due to inactivity"}
                            """);
                    return;
                }
            }

            // Sliding window update + TTL
            redis.opsForValue().set(key, String.valueOf(now), props.maxIdleSeconds(), TimeUnit.SECONDS);
        }

        filterChain.doFilter(request, response);
    }

}
