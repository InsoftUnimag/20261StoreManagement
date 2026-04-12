package com.distribuidoras.inventario.domain.model;

import java.util.UUID;

public class StockGlobalSku {
    private UUID skuId;
    private Integer disponibles;
    private Integer comprometidos;
    private Integer fisicoTotal;

    public StockGlobalSku() {
    }

    public StockGlobalSku(UUID skuId, Integer disponibles, Integer comprometidos, Integer fisicoTotal) {
        this.skuId = skuId;
        this.disponibles = disponibles;
        this.comprometidos = comprometidos;
        this.fisicoTotal = fisicoTotal;
    }

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
