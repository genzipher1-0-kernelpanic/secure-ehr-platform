package com.team.ehr.config;

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
        Exception failure = null;
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            failure = ex;
            throw ex;
        } finally {
            String method = request.getMethod();
            String path = request.getRequestURI();
            int status = response.getStatus();
            long durationMs = System.currentTimeMillis() - start;
            String userId = "unknown";
            if (failure != null || status >= 400) {
                log.warn("request method={} path={} status={} durationMs={} userId={} body=[MASKED]",
                        method, path, status, durationMs, userId, failure);
            } else {
                log.info("request method={} path={} status={} durationMs={} userId={} body=[MASKED]",
                        method, path, status, durationMs, userId);
            }
        }
    }
}
