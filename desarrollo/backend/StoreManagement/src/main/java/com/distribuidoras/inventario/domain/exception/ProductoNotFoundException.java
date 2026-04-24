package com.distribuidoras.inventario.domain.exception;

/**
 * Se lanza cuando no se encuentra un producto por su SKU ID.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
public class ProductoNotFoundException extends RuntimeException {

    private final String skuId;

    public ProductoNotFoundException(String skuId) {
        super("Producto con SKU '%s' no encontrado".formatted(skuId));
        this.skuId = skuId;
    }

    public String getSkuId() {
        return skuId;
    }
}
