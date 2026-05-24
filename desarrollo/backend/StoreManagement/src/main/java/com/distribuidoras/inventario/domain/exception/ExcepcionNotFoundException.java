package com.distribuidoras.inventario.domain.exception;

public class ExcepcionNotFoundException extends RuntimeException {
    public ExcepcionNotFoundException(Long excepcionId) {
        super("Excepción con ID '%s' no encontrada".formatted(excepcionId));
    }
}
