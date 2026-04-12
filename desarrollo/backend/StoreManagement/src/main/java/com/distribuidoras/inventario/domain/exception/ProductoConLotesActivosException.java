package com.distribuidoras.inventario.domain.exception;

import java.util.UUID;

/**
 * Se lanza cuando se intenta eliminar un producto que tiene lotes activos.
 * Spec: FR-011
 */
public class ProductoConLotesActivosException extends RuntimeException {

    private final UUID skuId;

    public ProductoConLotesActivosException(UUID skuId) {
        super("No se puede eliminar el producto con SKU '%s' porque tiene lotes activos (Disponible o Comprometido)".formatted(skuId));
        this.skuId = skuId;
    }

    public UUID getSkuId() {
        return skuId;
    }
}
