package com.distribuidoras.inventario.domain.exception;

import java.util.Map;

/**
 * Exception thrown when stock is insufficient for order creation.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 * Contains details of which SKUs failed and their available stock.
 */
public class StockInsuficienteException extends RuntimeException {

    private final Map<String, StockDetalle> detalles;

    public StockInsuficienteException(Map<String, StockDetalle> detalles) {
        super("Stock insuficiente para completar el pedido");
        this.detalles = detalles;
    }

    public Map<String, StockDetalle> getDetalles() {
        return detalles;
    }

    public record StockDetalle(
            String skuId,
            String marca,
            String presentacion,
            Integer cantidadSolicitada,
            Integer stockDisponible
    ) {}
}
