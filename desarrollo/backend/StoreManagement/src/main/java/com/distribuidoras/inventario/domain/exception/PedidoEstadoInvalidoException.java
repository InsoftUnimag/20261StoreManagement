package com.distribuidoras.inventario.domain.exception;

/**
 * Exception thrown when order is in wrong state for picking confirmation.
 */
public class PedidoEstadoInvalidoException extends RuntimeException {

    private final String pedidoId;
    private final String estadoActual;

    public PedidoEstadoInvalidoException(String pedidoId, String estadoActual) {
        super("Pedido %s ya fue procesado. Estado actual: %s".formatted(pedidoId, estadoActual));
        this.pedidoId = pedidoId;
        this.estadoActual = estadoActual;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public String getEstadoActual() {
        return estadoActual;
    }
}
