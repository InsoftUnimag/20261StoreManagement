package com.distribuidoras.inventario.domain.exception;

/**
 * Se lanza cuando se intenta crear o modificar un producto
 * resultando en una combinación marca + presentación duplicada.
 * Spec: FR-005
 */
public class ProductoDuplicadoException extends RuntimeException {

    private final String marca;
    private final String presentacion;

    public ProductoDuplicadoException(String marca, String presentacion) {
        super("Ya existe un producto con marca '%s' y presentación '%s'".formatted(marca, presentacion));
        this.marca = marca;
        this.presentacion = presentacion;
    }

    public String getMarca() {
        return marca;
    }

    public String getPresentacion() {
        return presentacion;
    }
}
