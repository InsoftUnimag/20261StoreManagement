package com.distribuidoras.inventario.domain.exception;

/**
 * Exception thrown when a client is inactive and cannot be used for orders.
 */
public class ClienteInactivoException extends RuntimeException {

    private final String clienteCc;

    public ClienteInactivoException(String clienteCc) {
        super("Cliente con CC '%s' está inactivo y no puede realizar pedidos".formatted(clienteCc));
        this.clienteCc = clienteCc;
    }

    public String getClienteCc() {
        return clienteCc;
    }
}
