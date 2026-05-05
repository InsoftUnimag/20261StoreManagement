package com.distribuidoras.inventario.domain.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de dominio: Producto (SKU).
 * Representa una referencia comercial en el catálogo.
 * Formato SKU: SKU-001, SKU-012, SKU-111, etc.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto {

    @EqualsAndHashCode.Include
    @NotBlank(message = "El SKU no puede estar vacío")
    @Size(max = 20, message = "El SKU no puede exceder 20 caracteres")
    private String skuId;

    @NotBlank(message = "La marca no puede estar vacía")
    @Size(max = 100, message = "La marca no puede exceder 100 caracteres")
    private String marca;

    @NotBlank(message = "La presentación no puede estar vacía")
    @Size(max = 100, message = "La presentación no puede exceder 100 caracteres")
    private String presentacion;

    @NotNull(message = "El contenido en ml no puede ser nulo")
    @Min(value = 1, message = "El contenido en ml debe ser mayor a 0")
    private Integer contenidoMl;

    @NotNull(message = "El peso logístico no puede ser nulo")
    @DecimalMin(value = "0.001", message = "El peso debe ser mayor a 0")
    @DecimalMax(value = "999999.999", message = "El peso excede el límite permitido")
    private BigDecimal pesoLogisticoKg;

    @NotNull(message = "La fecha de creación no puede ser nula")
    private LocalDateTime creadoEl;

    @Builder.Default
    private boolean activo = true;
}

