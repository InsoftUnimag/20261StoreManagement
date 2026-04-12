package com.distribuidoras.inventario.domain.exception;

import java.util.Map;
import java.util.UUID;

/**
 * Exception thrown when stock is insufficient for order creation.
 * Contains details of which SKUs failed and their available stock.
 */
public class StockInsuficienteException extends RuntimeException {

    private final Map<UUID, StockDetalle> detalles;

    public StockInsuficienteException(Map<UUID, StockDetalle> detalles) {
        super("Stock insuficiente para completar el pedido");
        this.detalles = detalles;
    }

    public Map<UUID, StockDetalle> getDetalles() {
        return detalles;
    }

    public record StockDetalle(
            UUID skuId,
            String marca,
            String presentacion,
            Integer cantidadSolicitada,
            Integer stockDisponible
    ) {}
}
