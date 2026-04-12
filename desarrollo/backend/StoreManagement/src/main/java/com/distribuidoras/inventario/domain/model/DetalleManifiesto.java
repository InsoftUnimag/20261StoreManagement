package com.distribuidoras.inventario.domain.model;

import java.util.UUID;

/**
 * Entidad de dominio: DetalleManifiesto.
 * Línea de manifiesto con cantidad esperada por SKU.
 */
public class DetalleManifiesto {

    private UUID detalleId;
    private UUID manifiestoId;
    private UUID skuId;
    private Integer cantidadEsperada;
    private Integer cantidadRecibida;

    public DetalleManifiesto() {}

    public DetalleManifiesto(UUID detalleId, UUID manifiestoId, UUID skuId,
                             Integer cantidadEsperada, Integer cantidadRecibida) {
        this.detalleId = detalleId;
        this.manifiestoId = manifiestoId;
        this.skuId = skuId;
        this.cantidadEsperada = cantidadEsperada;
        this.cantidadRecibida = cantidadRecibida;
    }

    public UUID getDetalleId() { return detalleId; }
    public void setDetalleId(UUID detalleId) { this.detalleId = detalleId; }
    public UUID getManifiestoId() { return manifiestoId; }
    public void setManifiestoId(UUID manifiestoId) { this.manifiestoId = manifiestoId; }
    public UUID getSkuId() { return skuId; }
    public void setSkuId(UUID skuId) { this.skuId = skuId; }
    public Integer getCantidadEsperada() { return cantidadEsperada; }
    public void setCantidadEsperada(Integer cantidadEsperada) { this.cantidadEsperada = cantidadEsperada; }
    public Integer getCantidadRecibida() { return cantidadRecibida; }
    public void setCantidadRecibida(Integer cantidadRecibida) { this.cantidadRecibida = cantidadRecibida; }
}
