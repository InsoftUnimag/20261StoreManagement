package com.distribuidoras.inventario.infrastructure.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

/**
 * Order summary DTO for list view.
 */
public record PedidoResumenDTO(
        UUID pedidoId,
        String numeroPedido,
        String clienteCc,
        String clienteNombre,
        String direccionEntrega,
        LocalDateTime fechaCreacion,
        String estado,
        Integer totalLineas,
        Integer totalUnidades,
        List<LineaResumenDTO> lineas
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID pedidoId;
        private String numeroPedido;
        private String clienteCc;
        private String clienteNombre;
        private String direccionEntrega;
        private LocalDateTime fechaCreacion;
        private String estado;
        private Integer totalLineas;
        private Integer totalUnidades;
        private List<LineaResumenDTO> lineas;

        public Builder pedidoId(UUID pedidoId) {
            this.pedidoId = pedidoId;
            return this;
        }

        public Builder numeroPedido(String numeroPedido) {
            this.numeroPedido = numeroPedido;
            return this;
        }

        public Builder clienteCc(String clienteCc) {
            this.clienteCc = clienteCc;
            return this;
        }

        public Builder clienteNombre(String clienteNombre) {
            this.clienteNombre = clienteNombre;
            return this;
        }

        public Builder direccionEntrega(String direccionEntrega) {
            this.direccionEntrega = direccionEntrega;
            return this;
        }

        public Builder fechaCreacion(LocalDateTime fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
            return this;
        }

        public Builder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public Builder totalLineas(Integer totalLineas) {
            this.totalLineas = totalLineas;
            return this;
        }

        public Builder totalUnidades(Integer totalUnidades) {
            this.totalUnidades = totalUnidades;
            return this;
        }

        public Builder lineas(List<LineaResumenDTO> lineas) {
            this.lineas = lineas;
            return this;
        }

        public PedidoResumenDTO build() {
            return new PedidoResumenDTO(
                    pedidoId,
                    numeroPedido,
                    clienteCc,
                    clienteNombre,
                    direccionEntrega,
                    fechaCreacion,
                    estado,
                    totalLineas,
                    totalUnidades,
                    lineas
            );
        }
    }
}
