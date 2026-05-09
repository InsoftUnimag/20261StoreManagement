package com.distribuidoras.inventario.infrastructure.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for assigned orders list (picking/despacho).
 */
public record PedidoAsignadoDTO(
        UUID pedidoId,
        String numeroPedido,
        String clienteCc,
        String clienteNombre,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaCompromiso,
        LocalDateTime fechaSalida,
        String estado,
        Integer totalUnidades
) {}
