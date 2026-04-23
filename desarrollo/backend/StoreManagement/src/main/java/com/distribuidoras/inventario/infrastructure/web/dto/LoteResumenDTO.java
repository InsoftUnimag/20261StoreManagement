package com.distribuidoras.inventario.infrastructure.web.dto;

public record LoteResumenDTO(
        String codigoLote,
        Integer cantidadComprometida
) {
}
