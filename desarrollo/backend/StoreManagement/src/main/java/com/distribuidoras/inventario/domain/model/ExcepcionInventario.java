package com.distribuidoras.inventario.domain.model;

import lombok.*;

import com.distribuidoras.inventario.domain.model.enums.TipoExcepcion;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio: ExcepcionInventario.
 * Registro de anomalías para trazabilidad.
 * Spec: 16_reportar_excepciones_inventario.md (FR-025 a FR-033)
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExcepcionInventario {

    private UUID excepcionId;
    private TipoExcepcion tipoExcepcion;
    private String codigoLote;
    private String skuId;
    private Integer cantidadAfectada;
    private LocalDateTime fechaRegistro;
    private String operarioId;
    private String descripcion;
    private String evidenciaUrl;

    // Validación custom si es necesaria, pero Lombok ya maneja lo básico.
    // Podría usarse un @Builder custom o validación en el constructor.
}
