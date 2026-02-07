package com.team.ehr.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityBeans {

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper) {
        return new JwtAuthFilter(jwtService, objectMapper);
    }

    @Bean
    public InternalAuthFilter internalAuthFilter(@org.springframework.beans.factory.annotation.Value("${internal.service.token:}") String token,
                                                  ObjectMapper objectMapper) {
        return new InternalAuthFilter(token, objectMapper);
    }
}
