package com.distribuidoras.inventario.domain.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

/**
 * Entidad de dominio: DetalleManifiesto.
 * Línea de manifiesto con cantidad esperada por SKU.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleManifiesto {

    private UUID detalleId;

    @NotNull(message = "El ID del manifiesto no puede ser nulo")
    private UUID manifiestoId;

    @NotBlank(message = "El SKU no puede estar vacío")
    @Size(max = 20, message = "El SKU no puede exceder 20 caracteres")
    private String skuId;

    @NotNull(message = "La cantidad esperada no puede ser nula")
    @Min(value = 1, message = "La cantidad esperada debe ser mayor a 0")
    private Integer cantidadEsperada;

    @NotNull(message = "La cantidad recibida no puede ser nula")
    @Min(value = 0, message = "La cantidad recibida no puede ser negativa")
    private Integer cantidadRecibida;
}