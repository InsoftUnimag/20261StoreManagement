package com.distribuidoras.inventario.infrastructure.web.dto;

/**
 * Request DTO for a single line item in a pedido.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
public record LineaPedidoItemDTO(
        String skuId,
        Integer cantidad
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String skuId;
        private Integer cantidad;

        public Builder skuId(String skuId) {
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
