package com.distribuidoras.inventario.domain.model;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @NotBlank(message = "El código de lote no puede estar vacío")
    @Size(max = 100, message = "El código de lote no puede exceder 100 caracteres")
    private String codigoLote;

    @NotBlank(message = "El SKU no puede estar vacío")
    @Size(max = 20, message = "El SKU no puede exceder 20 caracteres")
    private String skuId;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidad;

    @NotNull(message = "La fecha de vencimiento no puede ser nula")
    private LocalDate fechaVencimiento;

    private LocalDate fechaExpedicion;

    @NotNull(message = "El indicador de disponibilidad no puede ser nulo")
    private Boolean disponible;

    @NotNull(message = "El flag de urgencia FEFO no puede ser nulo")
    private Boolean flagUrgenciaFefo;

    @DecimalMin(value = "0", message = "El costo no puede ser negativo")
    private BigDecimal costoUnitarioProducto;

    private Long recepcionId;

    @NotNull(message = "La fecha de creación no puede ser nula")
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

