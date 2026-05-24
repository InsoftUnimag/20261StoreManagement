package com.distribuidoras.inventario.domain.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Domain entity for a Dispatch Record.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RegistroDespacho {

    @NotNull(message = "El ID del registro de despacho no puede ser nulo")
    private Long registroDespachoId;

    @NotNull(message = "El ID del pedido no puede ser nulo")
    private Long pedidoId;

    @NotNull(message = "El ID del operario no puede ser nulo")
    private Long operarioId;

    @NotNull(message = "La fecha de despacho no puede ser nula")
    private LocalDateTime fechaDespacho;

    @NotBlank(message = "El transportista no puede estar vacío")
    @Size(max = 200, message = "El transportista no puede exceder 200 caracteres")
    private String transportista;

    @NotBlank(message = "La placa del vehículo no puede estar vacía")
    @Size(max = 20, message = "La placa no puede exceder 20 caracteres")
    private String placaVehiculo;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}