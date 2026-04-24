package com.distribuidoras.inventario.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration to enable asynchronous execution for RabbitMQ Producers.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
