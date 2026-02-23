package com.genzipher.identityservice.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bootstrap.super-admin")
public record SuperAdminProperties(
        String email,
        String password
) {}
