package com.distribuidoras.inventario.infrastructure.web.dto;

import java.util.List;

public record ProductosPedidoResponse(
        List<ProductoEnPedidoDTO> productos
) {
}
