package com.distribuidoras.inventario.domain.exception;

/**
 * Se lanza cuando se intenta eliminar un producto que tiene lotes activos.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 * Spec: FR-011
 */
public class ProductoConLotesActivosException extends RuntimeException {

    private final String skuId;

    public ProductoConLotesActivosException(String skuId) {
        super("No se puede eliminar el producto con SKU '%s' porque tiene lotes activos (Disponible o Comprometido)".formatted(skuId));
        this.skuId = skuId;
    }

    public String getSkuId() {
        return skuId;
    }
}
