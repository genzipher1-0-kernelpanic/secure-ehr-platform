package com.team.care.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.care.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class InternalAuthFilter implements HandlerInterceptor {

    private static final String HEADER_NAME = "X-Internal-Token";

    private final String expectedToken;
    private final ObjectMapper objectMapper;

    public InternalAuthFilter(
            @Value("${INTERNAL_SERVICE_TOKEN:}") String expectedToken,
            ObjectMapper objectMapper
    ) {
        this.expectedToken = expectedToken;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String token = request.getHeader(HEADER_NAME);
        if (expectedToken == null || expectedToken.isBlank() || token == null || !token.equals(expectedToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse error = new ErrorResponse("UNAUTHORIZED", "Invalid internal token");
            response.getWriter().write(objectMapper.writeValueAsString(error));
            return false;
        }
        return true;
    }
}
