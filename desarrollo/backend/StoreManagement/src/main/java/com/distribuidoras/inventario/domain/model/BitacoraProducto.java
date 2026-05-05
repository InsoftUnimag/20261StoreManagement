package com.distribuidoras.inventario.domain.model;

import lombok.*;

import java.time.LocalDateTime;

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

    private Long id;
    private String skuIdRef;
    private String campo;
    private String valorAnterior;
    private String valorNuevo;
    private String descripcion;
    private LocalDateTime fecha;
    private String usuario;
}
