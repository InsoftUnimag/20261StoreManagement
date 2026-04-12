package com.distribuidoras.inventario.domain.model;

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
    private LocalDateTime fechaRecepcion;
    private String notas;
}

