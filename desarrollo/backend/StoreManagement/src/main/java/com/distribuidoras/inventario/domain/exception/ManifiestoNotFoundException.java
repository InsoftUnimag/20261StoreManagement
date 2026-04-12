package com.distribuidoras.inventario.domain.exception;

import java.util.UUID;

public class ManifiestoNotFoundException extends RuntimeException {
    public ManifiestoNotFoundException(UUID manifiestoId) {
        super("Manifiesto con ID '%s' no encontrado".formatted(manifiestoId));
    }
}
