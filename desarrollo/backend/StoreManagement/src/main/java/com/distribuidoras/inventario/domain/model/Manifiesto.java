package com.distribuidoras.inventario.domain.model;

import com.distribuidoras.inventario.domain.model.enums.EstadoManifiesto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad de dominio: Manifiesto.
 * Documento de fábrica con productos esperados.
 * Read-only en Módulo 1 (solo se actualiza estado).
 */
public class Manifiesto {

    private UUID manifiestoId;
    private String numeroManifiesto;
    private LocalDate fechaEmision;
    private String proveedor;
    private EstadoManifiesto estado;

    public Manifiesto() {}

    public Manifiesto(UUID manifiestoId, String numeroManifiesto, LocalDate fechaEmision,
                      String proveedor, EstadoManifiesto estado) {
        this.manifiestoId = manifiestoId;
        this.numeroManifiesto = numeroManifiesto;
        this.fechaEmision = fechaEmision;
        this.proveedor = proveedor;
        this.estado = estado;
    }

    public UUID getManifiestoId() { return manifiestoId; }
    public void setManifiestoId(UUID manifiestoId) { this.manifiestoId = manifiestoId; }
    public String getNumeroManifiesto() { return numeroManifiesto; }
    public void setNumeroManifiesto(String numeroManifiesto) { this.numeroManifiesto = numeroManifiesto; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public EstadoManifiesto getEstado() { return estado; }
    public void setEstado(EstadoManifiesto estado) { this.estado = estado; }
}
