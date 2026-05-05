package com.distribuidoras.inventario.domain.exception;

/**
 * Se lanza cuando se intenta crear un lote que ya existe.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
public class LoteDuplicadoException extends RuntimeException {
    public LoteDuplicadoException(String skuId, String codigoLote) {
        super("El lote '%s' ya existe para el SKU '%s'".formatted(codigoLote, skuId));
    }
}
