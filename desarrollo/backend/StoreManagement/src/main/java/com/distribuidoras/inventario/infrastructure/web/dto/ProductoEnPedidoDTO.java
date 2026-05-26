package com.distribuidoras.inventario.infrastructure.web.dto;

import java.math.BigDecimal;

public record ProductoEnPedidoDTO(
        String id,
        String nombre,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
