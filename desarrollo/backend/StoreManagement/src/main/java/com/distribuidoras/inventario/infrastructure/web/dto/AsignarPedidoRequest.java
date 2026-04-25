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
public class AsignarPedidoRequest {
    private UUID pedidoId;
    private UUID operarioPickingId;
    private UUID operarioDespachoId;
}