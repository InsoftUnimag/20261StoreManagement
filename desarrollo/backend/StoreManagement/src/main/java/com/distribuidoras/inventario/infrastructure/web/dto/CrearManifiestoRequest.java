package com.distribuidoras.inventario.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearManifiestoRequest {
    private String numeroManifiesto;
    private String proveedor;
    private List<LineaManifiestoRequest> lineas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineaManifiestoRequest {
        private String skuId;
        private Integer cantidadEsperada;
    }
}