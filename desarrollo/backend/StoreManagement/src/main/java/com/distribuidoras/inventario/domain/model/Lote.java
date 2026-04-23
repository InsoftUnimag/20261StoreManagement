package com.distribuidoras.inventario.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

/**
 * Entidad de dominio: Lote.
 * Unidad de trazabilidad con fecha de vencimiento para FEFO.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 * Spec: 04_registrar_ingreso_productos.md (FR-012 a FR-018)
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Lote {

    private String codigoLote;
    private String skuId;
    private Integer cantidad;
    private LocalDate fechaVencimiento;
    private LocalDate fechaExpedicion;
    private Boolean disponible;
    private Boolean flagUrgenciaFefo;
    private BigDecimal costoUnitarioProducto;
    private UUID recepcionId;
    private LocalDateTime creadoEl;

    /**
     * Reduce la cantidad de stock tras un movimiento de salida o baja.
     * @param cantidadAReducir cantidad a descontar
     * @throws IllegalArgumentException si cantidadAReducir > cantidad actual
     */
    public void reducirStock(int cantidadAReducir) {
        if (cantidadAReducir > this.cantidad) {
            throw new IllegalArgumentException(
                    "Stock insuficiente en lote %s. Disponible: %d, Solicitado: %d"
                            .formatted(this.codigoLote, this.cantidad, cantidadAReducir));
        }
        this.cantidad -= cantidadAReducir;
    }

    /**
     * Incrementa la cantidad de stock tras un movimiento de entrada o reversión.
     * @param cantidadAIncrementar cantidad a aumentar
     */
    public void incrementarStock(int cantidadAIncrementar) {
        this.cantidad += cantidadAIncrementar;
    }
}

