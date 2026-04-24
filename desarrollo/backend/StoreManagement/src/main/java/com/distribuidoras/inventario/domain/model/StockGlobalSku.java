package com.distribuidoras.inventario.domain.model;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Entidad de dominio para stock global por SKU.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class StockGlobalSku {

    @NotBlank(message = "El SKU no puede estar vacío")
    @Size(max = 20, message = "El SKU no puede exceder 20 caracteres")
    private String skuId;

    @NotNull(message = "La cantidad disponible no puede ser nula")
    @Min(value = 0, message = "La cantidad disponible no puede ser negativa")
    private Integer disponibles;

    @NotNull(message = "La cantidad comprometida no puede ser nula")
    @Min(value = 0, message = "La cantidad comprometida no puede ser negativa")
    private Integer comprometidos;

    @NotNull(message = "El stock físico total no puede ser nula")
    @Min(value = 0, message = "El stock físico total no puede ser negativo")
    private Integer fisicoTotal;
}
