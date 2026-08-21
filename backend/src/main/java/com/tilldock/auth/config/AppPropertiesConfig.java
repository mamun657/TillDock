package com.tilldock.auth.config;

import com.tilldock.auth.security.BcryptProperties;
import com.tilldock.auth.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, BcryptProperties.class})
public class AppPropertiesConfig {
}