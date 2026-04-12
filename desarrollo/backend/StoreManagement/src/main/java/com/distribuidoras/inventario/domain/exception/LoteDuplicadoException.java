package com.distribuidoras.inventario.domain.exception;

import java.util.UUID;

public class LoteDuplicadoException extends RuntimeException {
    public LoteDuplicadoException(UUID skuId, String codigoLote) {
        super("El lote '%s' ya existe para el SKU '%s'".formatted(codigoLote, skuId));
    }
}
