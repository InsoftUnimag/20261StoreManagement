package com.distribuidoras.inventario.infrastructure.web.dto;

import java.util.List;

/**
 * Request DTO for checking availability of a pedido.
 */
public record DisponibilidadRequestDTO(
        String pedidoId,
        List<LineaPedidoItemDTO> lineas
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String pedidoId;
        private List<LineaPedidoItemDTO> lineas;

        public Builder pedidoId(String pedidoId) {
            this.pedidoId = pedidoId;
            return this;
        }

        public Builder lineas(List<LineaPedidoItemDTO> lineas) {
            this.lineas = lineas;
            return this;
        }

        public DisponibilidadRequestDTO build() {
            return new DisponibilidadRequestDTO(pedidoId, lineas);
        }
    }
}
