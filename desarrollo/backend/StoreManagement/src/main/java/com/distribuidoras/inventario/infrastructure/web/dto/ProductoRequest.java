package com.distribuidoras.inventario.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO para crear un nuevo producto.
 * Spec: 01_crear_plantilla_producto.md (FR-002)
 */
public class ProductoRequest {

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotBlank(message = "La presentación es obligatoria")
    private String presentacion;

    @NotNull(message = "El contenido en ml es obligatorio")
    @Positive(message = "El contenido en ml debe ser mayor a cero")
    private Integer contenidoMl;

    @NotNull(message = "El peso logístico es obligatorio")
    @Positive(message = "El peso logístico debe ser mayor a cero")
    private BigDecimal pesoLogisticoKg;

    public ProductoRequest() {
    }

    public ProductoRequest(String marca, String presentacion, Integer contenidoMl, BigDecimal pesoLogisticoKg) {
        this.marca = marca;
        this.presentacion = presentacion;
        this.contenidoMl = contenidoMl;
        this.pesoLogisticoKg = pesoLogisticoKg;
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
}
