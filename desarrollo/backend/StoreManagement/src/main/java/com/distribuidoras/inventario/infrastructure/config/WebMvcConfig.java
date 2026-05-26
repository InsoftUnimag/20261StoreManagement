package com.distribuidoras.inventario.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Value("${app.modulo.financiero.url:}")
    private String moduloFinancieroUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                String[] origins = allowedOrigins.split(",");
                String[] allOrigins = moduloFinancieroUrl != null && !moduloFinancieroUrl.isBlank()
                        ? java.util.Arrays.copyOf(origins, origins.length + 1)
                        : origins;
                if (moduloFinancieroUrl != null && !moduloFinancieroUrl.isBlank()) {
                    allOrigins[origins.length] = moduloFinancieroUrl;
                }
                registry.addMapping("/api/v1/**")
                        .allowedOrigins(allOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
