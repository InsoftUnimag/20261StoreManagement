package com.distribuidoras.inventario.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


import java.time.Duration;

/**
 * Configuration for external service clients.
 * SC-021: Client query timeout ≤ 2 seconds
 */
@Configuration
public class RestTemplateConfig {

    @Value("${modulo.usuarios.timeout.connect:2000}")
    private long connectTimeout;

    @Value("${modulo.usuarios.timeout.read:5000}")
    private long readTimeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .readTimeout(Duration.ofMillis(readTimeout))
                .build();
    }
}
