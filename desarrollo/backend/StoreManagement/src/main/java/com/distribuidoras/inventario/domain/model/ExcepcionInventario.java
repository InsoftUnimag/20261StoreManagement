package com.distribuidoras.inventario.domain.model;

import jakarta.validation.constraints.*;
import lombok.*;

import com.distribuidoras.inventario.domain.model.enums.TipoExcepcion;
import java.time.LocalDateTime;
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

    private Long excepcionId;

    @NotNull(message = "El tipo de excepción no puede ser nulo")
    private TipoExcepcion tipoExcepcion;

    @Size(max = 100, message = "El código de lote no puede exceder 100 caracteres")
    private String codigoLote;

    @NotBlank(message = "El SKU no puede estar vacío")
    @Size(max = 20, message = "El SKU no puede exceder 20 caracteres")
    private String skuId;

    @NotNull(message = "La cantidad afectada no puede ser nula")
    private Integer cantidadAfectada;

    @NotNull(message = "La fecha de registro no puede ser nula")
    private LocalDateTime fechaRegistro;

    private Long operarioId;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @Size(max = 500, message = "La URL de evidencia no puede exceder 500 caracteres")
    private String evidenciaUrl;
}
