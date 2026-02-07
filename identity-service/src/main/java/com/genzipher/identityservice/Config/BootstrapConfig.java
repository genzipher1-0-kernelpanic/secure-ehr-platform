package com.genzipher.identityservice.Config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SuperAdminProperties.class)
public class BootstrapConfig {
}
