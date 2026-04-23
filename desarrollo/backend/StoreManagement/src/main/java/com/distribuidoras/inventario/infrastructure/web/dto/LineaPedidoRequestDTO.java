package com.distribuidoras.inventario.infrastructure.web.dto;

/**
 * Request DTO for a single order line.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
public record LineaPedidoRequestDTO(
        String skuId,
        Integer cantidadSolicitada
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String skuId;
        private Integer cantidadSolicitada;

        public Builder skuId(String skuId) {
            this.skuId = skuId;
            return this;
        }

        public Builder cantidadSolicitada(Integer cantidadSolicitada) {
            this.cantidadSolicitada = cantidadSolicitada;
            return this;
        }

        public LineaPedidoRequestDTO build() {
            return new LineaPedidoRequestDTO(skuId, cantidadSolicitada);
        }
    }
}
