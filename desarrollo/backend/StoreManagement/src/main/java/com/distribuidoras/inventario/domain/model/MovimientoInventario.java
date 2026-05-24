package com.distribuidoras.inventario.domain.model;

import jakarta.validation.constraints.*;
import lombok.*;
import com.distribuidoras.inventario.domain.model.enums.TipoMovimiento;
import java.time.LocalDateTime;
/**
 * Entidad de dominio: MovimientoInventario (Kardex).
 * Registro contable de cada cambio en stock de un lote.
 * Spec: 04 (FR-017), 16 (FR-028)
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario {

    private Long movimientoId;

    @NotBlank(message = "El código de lote no puede estar vacío")
    @Size(max = 100, message = "El código de lote no puede exceder 100 caracteres")
    private String codigoLote;

    @NotNull(message = "El tipo de movimiento no puede ser nulo")
    private TipoMovimiento tipoMovimiento;

    @NotNull(message = "La cantidad no puede ser nula")
    private Integer cantidad;

    @NotNull(message = "La fecha de movimiento no puede ser nula")
    private LocalDateTime fechaMovimiento;

    private Long pedidoId;

    private Long excepcionId;

    private Long operarioId;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}
