package com.distribuidoras.inventario.infrastructure.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IntegrationLogger {
    private static final Logger log = LoggerFactory.getLogger(IntegrationLogger.class);

    public void logCircuitBreakerState(String name, String state) {
        log.warn("El CircuitBreaker '{}' ha transicionado al estado: {}", name, state);
    }
}
