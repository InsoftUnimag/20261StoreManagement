package com.distribuidoras.inventario.domain.model;

import lombok.*;
import com.distribuidoras.inventario.domain.model.enums.TipoMovimiento;
import java.time.LocalDateTime;
import java.util.UUID;

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

    private UUID movimientoId;
    private String codigoLote;
    private TipoMovimiento tipoMovimiento;
    private Integer cantidad;
    private LocalDateTime fechaMovimiento;
    private UUID pedidoId;
    private UUID excepcionId;
    private UUID operarioId;
    private String observaciones;
}

