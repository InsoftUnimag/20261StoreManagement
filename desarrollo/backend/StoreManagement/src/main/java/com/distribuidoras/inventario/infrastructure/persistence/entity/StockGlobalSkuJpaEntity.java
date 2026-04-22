package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA para stock global por SKU.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
@Entity
@Table(name = "stock_global_sku")
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

    public StockGlobalSkuJpaEntity() {}

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public Integer getDisponibles() {
        return disponibles;
    }

    public void setDisponibles(Integer disponibles) {
        this.disponibles = disponibles;
    }

    public Integer getComprometidos() {
        return comprometidos;
    }

    public void setComprometidos(Integer comprometidos) {
        this.comprometidos = comprometidos;
    }

    public Integer getFisicoTotal() {
        return fisicoTotal;
    }

    public void setFisicoTotal(Integer fisicoTotal) {
        this.fisicoTotal = fisicoTotal;
    }
}
