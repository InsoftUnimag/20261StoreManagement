package com.distribuidoras.inventario.infrastructure.observability;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ExternalServicesHealthIndicator implements HealthIndicator {

    public ExternalServicesHealthIndicator() {
    }

    @Override
    public Health health() {
        try {
            // Ejemplo de chequeo de la API si soportara un endpoint de health
            return Health.up().withDetail("ModuloUsuarios", "Disponible").build();
        } catch (Exception e) {
            return Health.down(e).withDetail("ModuloUsuarios", "No Disponible").build();
        }
    }
}
