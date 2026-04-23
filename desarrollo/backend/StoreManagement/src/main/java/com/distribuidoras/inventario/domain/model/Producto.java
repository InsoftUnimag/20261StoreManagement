package com.distribuidoras.inventario.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de dominio: Producto (SKU).
 * Representa una referencia comercial en el catálogo.
 * Formato SKU: SKU-001, SKU-012, SKU-111, etc.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto {

    @EqualsAndHashCode.Include
    private String skuId;
    private String marca;
    private String presentacion;
    private Integer contenidoMl;
    private BigDecimal pesoLogisticoKg;
    private LocalDateTime creadoEl;
}

