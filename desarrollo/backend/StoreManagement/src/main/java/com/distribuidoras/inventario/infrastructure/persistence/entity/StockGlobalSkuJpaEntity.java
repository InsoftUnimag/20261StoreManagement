package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA para stock global por SKU.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
@Entity
@Table(name = "stock_global_sku", indexes = {
    @Index(name = "idx_stock_sku_id", columnList = "sku_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockGlobalSkuJpaEntity {

    @Id
    @Column(name = "sku_id", nullable = false, length = 20)
    private String skuId;

    @Column(name = "disponibles", nullable = false)
    private Integer disponibles;

    @Column(name = "comprometidos", nullable = false)
    private Integer comprometidos;

    @Column(name = "fisico_total", nullable = false)
    private Integer fisicoTotal;
}
