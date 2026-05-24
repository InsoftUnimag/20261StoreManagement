package com.distribuidoras.inventario.infrastructure.web.dto;

import java.time.LocalDateTime;

/**
 * DTO for assigned orders list (picking/despacho).
 */
public record PedidoAsignadoDTO(
                Long pedidoId,
                String numeroPedido,
                String clienteCc,
                String clienteNombre,
                LocalDateTime fechaCreacion,
                LocalDateTime fechaCompromiso,
                LocalDateTime fechaSalida,
                String estado,
                Integer totalUnidades) {
}
