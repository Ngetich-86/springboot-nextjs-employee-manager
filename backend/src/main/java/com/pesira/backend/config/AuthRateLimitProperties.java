package com.pesira.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.rate-limit.auth")
@Getter
@Setter
public class AuthRateLimitProperties {

    private int capacity = 10;
    private int refillTokens = 10;
    private int refillDurationMinutes = 1;
}
