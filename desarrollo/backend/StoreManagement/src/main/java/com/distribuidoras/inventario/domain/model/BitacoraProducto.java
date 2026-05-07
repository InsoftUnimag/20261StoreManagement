package com.distribuidoras.inventario.domain.model;

import lombok.*;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

/**
 * Entidad de dominio: BitacoraProducto.
 * Registro de auditoría para cambios en productos.
 * Formato skuIdRef: SKU-001, SKU-012, SKU-111, etc.
 * Spec: 02_modificar_plantilla_producto.md (FR-009)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitacoraProducto {

    @NotNull(message = "El ID de la bitácora no puede ser nulo")
    private Long id;

    @NotNull(message = "El SKU-ID del producto no puede ser nulo")
    private String skuIdRef;

    @NotNull(message = "El campo modificado no puede ser nulo")
    private String campo;

    @NotNull(message = "El valor anterior no puede ser nulo")
    private String valorAnterior;

    @NotNull(message = "El valor nuevo no puede ser nulo")
    private String valorNuevo;

    private String descripcion;

    @NotNull(message = "La fecha de cambio no puede ser nula")
    private LocalDateTime fecha;

    @NotNull(message = "El usuario que realizó el cambio no puede ser nulo")
    private String usuario;
}
