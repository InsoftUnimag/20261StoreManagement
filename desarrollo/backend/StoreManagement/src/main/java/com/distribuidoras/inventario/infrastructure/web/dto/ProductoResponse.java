package com.distribuidoras.inventario.infrastructure.web.dto;

import com.distribuidoras.inventario.domain.model.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para Producto.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 * Incluye disponibilidad calculada y posible alerta.
 */
public class ProductoResponse {

    private String skuId;
    private String marca;
    private String presentacion;
    private Integer contenidoMl;
    private BigDecimal pesoLogisticoKg;
    private BigDecimal costoCop;
    private int stockDisponible;
    private String disponibilidad;
    private LocalDateTime creadoEl;
    private String alerta;

    public ProductoResponse() {
    }

    /**
     * Crea un ProductoResponse a partir del modelo de dominio.
     */
    public static ProductoResponse fromDomain(Producto producto, int stockDisponible, String alerta, BigDecimal costoCop) {
        ProductoResponse response = new ProductoResponse();
        response.skuId = producto.getSkuId();
        response.marca = producto.getMarca();
        response.presentacion = producto.getPresentacion();
        response.contenidoMl = producto.getContenidoMl();
        response.pesoLogisticoKg = producto.getPesoLogisticoKg();
        response.costoCop = costoCop;
        response.stockDisponible = stockDisponible;
        response.disponibilidad = stockDisponible > 0 ? "Disponible" : "No disponible";
        response.creadoEl = producto.getCreadoEl();
        response.alerta = alerta;
        return response;
    }

    /**
     * Crea un ProductoResponse para un producto recién creado (stock = 0).
     */
    public static ProductoResponse fromCreado(Producto producto) {
        return fromDomain(producto, 0, null, null);
    }

    // Getters y Setters

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public Integer getContenidoMl() {
        return contenidoMl;
    }

    public void setContenidoMl(Integer contenidoMl) {
        this.contenidoMl = contenidoMl;
    }

    public BigDecimal getPesoLogisticoKg() {
        return pesoLogisticoKg;
    }

    public void setPesoLogisticoKg(BigDecimal pesoLogisticoKg) {
        this.pesoLogisticoKg = pesoLogisticoKg;
    }

    public BigDecimal getCostoCop() {
        return costoCop;
    }

    public void setCostoCop(BigDecimal costoCop) {
        this.costoCop = costoCop;
    }

    public int getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(int stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public String getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(String disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public LocalDateTime getCreadoEl() {
        return creadoEl;
    }

    public void setCreadoEl(LocalDateTime creadoEl) {
        this.creadoEl = creadoEl;
    }

    public String getAlerta() {
        return alerta;
    }

    public void setAlerta(String alerta) {
        this.alerta = alerta;
    }
}
