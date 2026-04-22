package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla 'producto'.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoJpaEntity {

    @Id
    @Column(name = "sku_id", updatable = false, nullable = false, length = 20)
    private String skuId;

    @Column(name = "marca", nullable = false, length = 100)
    private String marca;

    @Column(name = "presentacion", nullable = false, length = 100)
    private String presentacion;

    @Column(name = "contenido_ml", nullable = false)
    private Integer contenidoMl;

    @Column(name = "peso_logistico_kg", nullable = false, precision = 10, scale = 3)
    private BigDecimal pesoLogisticoKg;

    @Column(name = "creado_el", nullable = false, updatable = false)
    private LocalDateTime creadoEl;
}
