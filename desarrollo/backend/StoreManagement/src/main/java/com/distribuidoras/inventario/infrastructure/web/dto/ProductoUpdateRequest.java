package com.distribuidoras.inventario.infrastructure.web.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO para modificar un producto existente.
 * Todos los campos son opcionales (se actualizan solo los proporcionados).
 * Spec: 02_modificar_plantilla_producto.md (FR-007)
 */
public class ProductoUpdateRequest {

    private String marca;

    private String presentacion;

    @Positive(message = "El contenido en ml debe ser mayor a cero")
    private Integer contenidoMl;

    @Positive(message = "El peso logístico debe ser mayor a cero")
    private BigDecimal pesoLogisticoKg;

    private String descripcion;

    public ProductoUpdateRequest() {
    }

    public ProductoUpdateRequest(String marca, String presentacion, Integer contenidoMl,
                                  BigDecimal pesoLogisticoKg, String descripcion) {
        this.marca = marca;
        this.presentacion = presentacion;
        this.contenidoMl = contenidoMl;
        this.pesoLogisticoKg = pesoLogisticoKg;
        this.descripcion = descripcion;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
