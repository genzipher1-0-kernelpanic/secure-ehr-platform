package com.team.ehr.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.ehr.dto.ErrorResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && path.startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response, "Missing Authorization header");
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());
        Claims claims = jwtService.parse(token);
        if (claims == null) {
            writeUnauthorized(response, "Invalid token");
            return;
        }

        Long userId = parseUserId(claims.get("userId"));
        UserRole role = parseRole(claims.get("role"));
        if (userId == null || role == null) {
            writeUnauthorized(response, "Invalid token claims");
            return;
        }
        AuthUser authUser = new AuthUser(userId, role);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                authUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private Long parseUserId(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private UserRole parseRole(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return UserRole.valueOf(value.toString().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String detail) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = new ErrorResponse("about:blank", "UNAUTHORIZED", 401, detail);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
