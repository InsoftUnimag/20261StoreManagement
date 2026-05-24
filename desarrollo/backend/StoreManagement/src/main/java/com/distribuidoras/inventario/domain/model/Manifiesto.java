package com.distribuidoras.inventario.domain.model;

import com.distribuidoras.inventario.domain.model.enums.EstadoManifiesto;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidad de dominio: Manifiesto.
 * Documento de fábrica con productos esperados.
 * Read-only en Módulo 1 (solo se actualiza estado).
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Manifiesto {

    private Long manifiestoId;

    @NotBlank(message = "El número de manifiesto no puede estar vacío")
    @Size(max = 50, message = "El número de manifiesto no puede exceder 50 caracteres")
    private String numeroManifiesto;

    @NotNull(message = "La fecha de emisión no puede ser nula")
    private LocalDate fechaEmision;

    @NotBlank(message = "El proveedor no puede estar vacío")
    @Size(max = 200, message = "El proveedor no puede exceder 200 caracteres")
    private String proveedor;

    @NotNull(message = "El estado del manifiesto no puede ser nulo")
    private EstadoManifiesto estado;

    private java.time.LocalDateTime creadoEl;
}