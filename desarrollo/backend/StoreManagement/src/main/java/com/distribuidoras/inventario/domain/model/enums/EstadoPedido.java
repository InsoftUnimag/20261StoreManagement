package com.distribuidoras.inventario.domain.model.enums;

/**
 * Enum representing the state of a Pedido (order).
 * State machine: Esperando Ruta → Comprometido → En Picking → Despachado → Entregado
 */
public enum EstadoPedido {
    ESPERANDO_RUTA,
    COMPROMETIDO,
    EN_PICKING,
    DESPACHADO,
    ENTREGADO
}
