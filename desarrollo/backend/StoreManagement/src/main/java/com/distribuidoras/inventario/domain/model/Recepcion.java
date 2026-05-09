package com.distribuidoras.inventario.domain.model;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio: Recepcion.
 * Registro de recepción física de mercancía.
 * Spec: 04_registrar_ingreso_productos.md (FR-024)
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Recepcion {

    private UUID recepcionId;

    private UUID manifiestoId;

    private UUID operarioId;

    @NotNull(message = "La fecha de recepción no puede ser nula")
    private LocalDateTime fechaRecepcion;

    @Size(max = 1000, message = "Las notas no pueden exceder 1000 caracteres")
    private String notas;

    // Número de recepción generado (secuencial, no UUID)
    private String numeroRecepcion;
}

