package com.distribuidoras.inventario.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExcepcionRequestDTO {
    @NotBlank(message = "El tipo de excepción es obligatorio")
    private String tipoExcepcion;

    @NotBlank(message = "El SKU es obligatorio")
    private String skuId;

    private String codigoLote;

    @Positive(message = "La cantidad afectada debe ser mayor a cero")
    private int cantidadAfectada;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    private String evidenciaUrl;

    private Long operarioId;
}
