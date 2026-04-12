package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "stock_global_sku")
public class StockGlobalSkuJpaEntity {

    @Id
    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(name = "disponibles", nullable = false)
    private Integer disponibles;

    @Column(name = "comprometidos", nullable = false)
    private Integer comprometidos;

    @Column(name = "fisico_total", nullable = false)
    private Integer fisicoTotal;

    public StockGlobalSkuJpaEntity() {}

    public UUID getSkuId() {
        return skuId;
    }

    public void setSkuId(UUID skuId) {
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
