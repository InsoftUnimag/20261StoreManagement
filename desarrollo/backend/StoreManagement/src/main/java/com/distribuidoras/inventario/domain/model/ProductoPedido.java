package com.distribuidoras.inventario.domain.model;

import lombok.*;

import java.util.UUID;

/**
 * Domain entity: ProductoPedido (Order line item).
 * Represents a product line in an order.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductoPedido {

    private UUID productoPedidoId;
    
    private UUID pedidoId;
    
    private String skuId;
    
    private Integer cantidadSolicitada;
    
    private Integer cantidadConfirmada;
    
    private Integer cantidadDespachada;

    /**
     * Validates that confirmed quantity doesn't exceed requested quantity.
     */
    public void validarCantidadConfirmada() {
        if (this.cantidadConfirmada != null && this.cantidadConfirmada > this.cantidadSolicitada) {
            throw new IllegalArgumentException(
                    "Cantidad confirmada (%d) no puede ser mayor a la solicitada (%d)"
                            .formatted(this.cantidadConfirmada, this.cantidadSolicitada));
        }
        if (this.cantidadConfirmada != null && this.cantidadConfirmada < 0) {
            throw new IllegalArgumentException(
                    "Cantidad confirmada no puede ser negativa: %d".formatted(this.cantidadConfirmada));
        }
    }
}
