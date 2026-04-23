package com.distribuidoras.inventario.domain.model;

import lombok.*;

import java.util.UUID;

/**
 * Entidad de dominio: DetalleManifiesto.
 * Línea de manifiesto con cantidad esperada por SKU.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleManifiesto {

    private UUID detalleId;
    private UUID manifistoId;
    private String skuId;
    private Integer cantidadEsperada;
    private Integer cantidadRecibida;
}