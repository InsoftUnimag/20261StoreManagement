package com.distribuidoras.inventario.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for external service clients.
 * SC-021: Client query timeout ≤ 2 seconds
 */
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        // Using default RestTemplate configuration
        // Timeout settings should be configured via application.properties
        return new RestTemplate();
    }
}
