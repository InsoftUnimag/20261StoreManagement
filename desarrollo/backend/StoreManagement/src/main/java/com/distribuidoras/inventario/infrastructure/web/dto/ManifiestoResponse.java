package com.distribuidoras.inventario.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManifiestoResponse {
    private Long manifiestoId;
    private String numeroManifiesto;
    private LocalDate fechaEmision;
    private String proveedor;
    private String estado;
}