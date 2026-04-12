package com.distribuidoras.inventario.infrastructure.web.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for order creation and details.
 */
public record PedidoResponseDTO(
        UUID pedidoId,
        String numeroPedido,
        String estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaCompromiso,
        UUID rutaId,
        ClienteInfoDTO cliente,
        AsesorInfoDTO asesor,
        List<LineaPedidoResponseDTO> lineas,
        Integer totalSolicitado,
        Integer totalConfirmado
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID pedidoId;
        private String numeroPedido;
        private String estado;
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaCompromiso;
        private UUID rutaId;
        private ClienteInfoDTO cliente;
        private AsesorInfoDTO asesor;
        private List<LineaPedidoResponseDTO> lineas;
        private Integer totalSolicitado;
        private Integer totalConfirmado;

        public Builder pedidoId(UUID pedidoId) {
            this.pedidoId = pedidoId;
            return this;
        }

        public Builder numeroPedido(String numeroPedido) {
            this.numeroPedido = numeroPedido;
            return this;
        }

        public Builder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public Builder fechaCreacion(LocalDateTime fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
            return this;
        }

        public Builder fechaCompromiso(LocalDateTime fechaCompromiso) {
            this.fechaCompromiso = fechaCompromiso;
            return this;
        }

        public Builder rutaId(UUID rutaId) {
            this.rutaId = rutaId;
            return this;
        }

        public Builder cliente(ClienteInfoDTO cliente) {
            this.cliente = cliente;
            return this;
        }

        public Builder asesor(AsesorInfoDTO asesor) {
            this.asesor = asesor;
            return this;
        }

        public Builder lineas(List<LineaPedidoResponseDTO> lineas) {
            this.lineas = lineas;
            return this;
        }

        public Builder totalSolicitado(Integer totalSolicitado) {
            this.totalSolicitado = totalSolicitado;
            return this;
        }

        public Builder totalConfirmado(Integer totalConfirmado) {
            this.totalConfirmado = totalConfirmado;
            return this;
        }

        public PedidoResponseDTO build() {
            return new PedidoResponseDTO(
                    pedidoId,
                    numeroPedido,
                    estado,
                    fechaCreacion,
                    fechaCompromiso,
                    rutaId,
                    cliente,
                    asesor,
                    lineas,
                    totalSolicitado,
                    totalConfirmado
            );
        }
    }
}
