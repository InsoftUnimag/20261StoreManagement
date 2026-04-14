package com.distribuidoras.inventario.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when order is not found.
 */
public class PedidoNotFoundException extends RuntimeException {

    private final String identifier;

    public PedidoNotFoundException(String identifier) {
        super("Pedido '%s' no encontrado".formatted(identifier));
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
