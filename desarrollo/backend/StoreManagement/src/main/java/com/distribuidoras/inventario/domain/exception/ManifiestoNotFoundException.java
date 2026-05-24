package com.distribuidoras.inventario.domain.exception;



public class ManifiestoNotFoundException extends RuntimeException {
    public ManifiestoNotFoundException(Long manifiestoId) {
        super("Manifiesto con ID '%s' no encontrado".formatted(manifiestoId));
    }
}
