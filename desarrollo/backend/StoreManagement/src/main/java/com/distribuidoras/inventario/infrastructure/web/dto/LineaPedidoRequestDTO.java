package com.distribuidoras.inventario.infrastructure.web.dto;

import java.util.UUID;

/**
 * Request DTO for a single order line.
 */
public record LineaPedidoRequestDTO(
        UUID skuId,
        Integer cantidadSolicitada
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID skuId;
        private Integer cantidadSolicitada;

        public Builder skuId(UUID skuId) {
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
