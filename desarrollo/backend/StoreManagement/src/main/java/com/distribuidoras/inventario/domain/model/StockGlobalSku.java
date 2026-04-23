package com.distribuidoras.inventario.domain.model;

/**
 * Entidad de dominio para stock global por SKU.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
public class StockGlobalSku {
    private String skuId;
    private Integer disponibles;
    private Integer comprometidos;
    private Integer fisicoTotal;

    public StockGlobalSku() {
    }

    public StockGlobalSku(String skuId, Integer disponibles, Integer comprometidos, Integer fisicoTotal) {
        this.skuId = skuId;
        this.disponibles = disponibles;
        this.comprometidos = comprometidos;
        this.fisicoTotal = fisicoTotal;
    }

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
