package com.distribuidoras.inventario.domain.exception;

import java.util.UUID;

public class ExcepcionNotFoundException extends RuntimeException {
    public ExcepcionNotFoundException(UUID excepcionId) {
        super("Excepción con ID '%s' no encontrada".formatted(excepcionId));
    }
}
