package com.distribuidoras.inventario.domain.exception;

/**
 * Exception thrown when external service (Módulo Usuarios) is unavailable.
 */
public class ExternalServiceException extends RuntimeException {

    private final String serviceName;

    public ExternalServiceException(String serviceName, String message) {
        super("Servicio '%s' no disponible: %s".formatted(serviceName, message));
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
