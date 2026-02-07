package com.team.ehr.config;

import com.team.ehr.security.SecurityUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        filterChain.doFilter(request, response);
        String method = request.getMethod();
        String path = request.getRequestURI();
        int status = response.getStatus();
        long durationMs = System.currentTimeMillis() - start;
        String userId = "unknown";
        try {
            userId = String.valueOf(SecurityUtil.getUserId());
        } catch (Exception ex) {
            // ignore missing auth
        }
        log.info("request method={} path={} status={} durationMs={} userId={} body=[MASKED]",
                method, path, status, durationMs, userId);
    }
}
