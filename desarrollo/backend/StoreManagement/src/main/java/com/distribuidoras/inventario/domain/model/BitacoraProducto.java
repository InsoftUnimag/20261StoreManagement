package com.distribuidoras.inventario.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio: BitacoraProducto.
 * Registro de auditoría para cambios en productos.
 * Spec: 02_modificar_plantilla_producto.md (FR-009)
 */
public class BitacoraProducto {

    private Long id;
    private UUID skuIdRef;
    private String campo;
    private String valorAnterior;
    private String valorNuevo;
    private String descripcion;
    private LocalDateTime fecha;
    private String usuario;

    public BitacoraProducto() {
    }

    public BitacoraProducto(Long id, UUID skuIdRef, String campo, String valorAnterior,
                            String valorNuevo, String descripcion, LocalDateTime fecha, String usuario) {
        this.id = id;
        this.skuIdRef = skuIdRef;
        this.campo = campo;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.usuario = usuario;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getSkuIdRef() {
        return skuIdRef;
    }

    public void setSkuIdRef(UUID skuIdRef) {
        this.skuIdRef = skuIdRef;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public String getValorNuevo() {
        return valorNuevo;
    }

    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
