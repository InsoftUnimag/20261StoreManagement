package com.distribuidoras.inventario.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoAsignadoResponse {
    private UUID pedidoId;
    private String numeroPedido;
    private String clienteCc;
    private String estado;
    private UUID operarioPickingId;
    private UUID operarioDespachoId;
}