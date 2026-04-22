package com.distribuidoras.inventario.domain.exception;

import java.util.UUID;

/**
 * Se lanza cuando no se encuentra un producto por su SKU ID.
 */
public class ProductoNotFoundException extends RuntimeException {

    private final UUID skuId;

    public ProductoNotFoundException(UUID skuId) {
        super("Producto con SKU '%s' no encontrado".formatted(skuId));
        this.skuId = skuId;
    }

    public UUID getSkuId() {
        return skuId;
    }
}
