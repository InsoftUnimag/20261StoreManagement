package com.distribuidoras.inventario.infrastructure.web.dto;

import java.util.UUID;

/**
 * Request DTO for a single line item in a pedido.
 */
public record LineaPedidoItemDTO(
        UUID skuId,
        Integer cantidad
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID skuId;
        private Integer cantidad;

        public Builder skuId(UUID skuId) {
            this.skuId = skuId;
            return this;
        }

        public Builder cantidad(Integer cantidad) {
            this.cantidad = cantidad;
            return this;
        }

        public LineaPedidoItemDTO build() {
            return new LineaPedidoItemDTO(skuId, cantidad);
        }
    }
}
