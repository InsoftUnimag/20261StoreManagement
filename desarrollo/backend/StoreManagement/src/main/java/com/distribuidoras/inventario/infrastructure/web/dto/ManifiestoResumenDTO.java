package com.distribuidoras.inventario.infrastructure.web.dto;

import java.time.LocalDateTime;

public record ManifiestoResumenDTO(
        String manifiestoId,
        String numeroManifiesto,
        LocalDateTime fechaEmision,
        String proveedor,
        String estado,
        int totalLineas,
        int lineasRecibidas,
        LocalDateTime creadoEl,
        int sumEsperado,
        int sumRecibido) {
}
