package com.distribuidoras.inventario.infrastructure.web.dto;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a new order (Pedido).
 */
public record PedidoRequestDTO(
        String clienteCc,
        UUID asesorId,
        List<LineaPedidoRequestDTO> lineas
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String clienteCc;
        private UUID asesorId;
        private List<LineaPedidoRequestDTO> lineas;

        public Builder clienteCc(String clienteCc) {
            this.clienteCc = clienteCc;
            return this;
        }

        public Builder asesorId(UUID asesorId) {
            this.asesorId = asesorId;
            return this;
        }

        public Builder lineas(List<LineaPedidoRequestDTO> lineas) {
            this.lineas = lineas;
            return this;
        }

        public PedidoRequestDTO build() {
            return new PedidoRequestDTO(clienteCc, asesorId, lineas);
        }
    }
}
