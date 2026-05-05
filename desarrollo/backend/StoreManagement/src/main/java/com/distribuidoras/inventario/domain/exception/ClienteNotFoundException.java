package com.distribuidoras.inventario.domain.exception;

/**
 * Exception thrown when client is not found in external module.
 */
public class ClienteNotFoundException extends RuntimeException {

    private final String clienteCc;

    public ClienteNotFoundException(String clienteCc) {
        super("Cliente con CC '%s' no encontrado en el sistema".formatted(clienteCc));
        this.clienteCc = clienteCc;
    }

    public String getClienteCc() {
        return clienteCc;
    }
}
