package com.distribuidoras.inventario.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio: Producto (SKU).
 * Representa una referencia comercial en el catálogo.
 * POJO puro, sin dependencias de framework.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto {

    @EqualsAndHashCode.Include
    private UUID skuId;
    private String marca;
    private String presentacion;
    private Integer contenidoMl;
    private BigDecimal pesoLogisticoKg;
    private LocalDateTime creadoEl;
}

