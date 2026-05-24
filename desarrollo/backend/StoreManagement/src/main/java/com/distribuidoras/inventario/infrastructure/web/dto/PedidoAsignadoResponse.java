package com.distribuidoras.inventario.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoAsignadoResponse {
    private Long pedidoId;
    private String numeroPedido;
    private String clienteCc;
    private String estado;
    private Long operarioPickingId;
    private Long operarioDespachoId;
}