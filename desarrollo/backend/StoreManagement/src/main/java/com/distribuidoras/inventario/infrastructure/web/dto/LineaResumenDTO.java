package com.distribuidoras.inventario.infrastructure.web.dto;

import java.util.List;

/**
 * Summary DTO for an order line in a list context.
 */
public record LineaResumenDTO(
        String skuId,
        String marca,
        String presentacion,
        Integer cantidadSolicitada,
        Integer cantidadConfirmada,
        List<LoteResumenDTO> lotes
) {
}
