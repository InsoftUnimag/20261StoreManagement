package com.distribuidoras.inventario.infrastructure.web.dto;

import com.distribuidoras.inventario.domain.model.BitacoraProducto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para registros de bitácora.
 * Spec: 02_modificar_plantilla_producto.md (FR-009)
 */
public class BitacoraResponse {

    private String campo;
    private String valorAnterior;
    private String valorNuevo;
    private String descripcion;
    private LocalDateTime fecha;
    private String usuario;

    public BitacoraResponse() {
    }

    public static BitacoraResponse fromDomain(BitacoraProducto bitacora) {
        BitacoraResponse response = new BitacoraResponse();
        response.campo = bitacora.getCampo();
        response.valorAnterior = bitacora.getValorAnterior();
        response.valorNuevo = bitacora.getValorNuevo();
        response.descripcion = bitacora.getDescripcion();
        response.fecha = bitacora.getFecha();
        response.usuario = bitacora.getUsuario();
        return response;
    }

    // Getters y Setters

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
