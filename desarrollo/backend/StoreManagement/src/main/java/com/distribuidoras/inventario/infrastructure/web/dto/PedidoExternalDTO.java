package com.distribuidoras.inventario.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for external modules (Transporte/Financiero) to query pedido details.
 * Per document_Api.yml: GET /api/v1/external/pedidos/{pedidoId}?modulo=transporte|financiero
 */
public record PedidoExternalDTO(
        String pedidoId,
        String numeroPedido,
        String estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaCompromiso,
        String rutaId,
        ClienteExternalDTO cliente,
        List<LineaExternalDTO> lineas,
        BigDecimal pesoLogisticoTotal,
        BigDecimal precioTotal
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String pedidoId;
        private String numeroPedido;
        private String estado;
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaCompromiso;
        private String rutaId;
        private ClienteExternalDTO cliente;
        private List<LineaExternalDTO> lineas;
        private BigDecimal pesoLogisticoTotal;
        private BigDecimal precioTotal;

        public Builder pedidoId(String pedidoId) { this.pedidoId = pedidoId; return this; }
        public Builder numeroPedido(String numeroPedido) { this.numeroPedido = numeroPedido; return this; }
        public Builder estado(String estado) { this.estado = estado; return this; }
        public Builder fechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; return this; }
        public Builder fechaCompromiso(LocalDateTime fechaCompromiso) { this.fechaCompromiso = fechaCompromiso; return this; }
        public Builder rutaId(String rutaId) { this.rutaId = rutaId; return this; }
        public Builder cliente(ClienteExternalDTO cliente) { this.cliente = cliente; return this; }
        public Builder lineas(List<LineaExternalDTO> lineas) { this.lineas = lineas; return this; }
        public Builder pesoLogisticoTotal(BigDecimal pesoLogisticoTotal) { this.pesoLogisticoTotal = pesoLogisticoTotal; return this; }
        public Builder precioTotal(BigDecimal precioTotal) { this.precioTotal = precioTotal; return this; }

        public PedidoExternalDTO build() {
            return new PedidoExternalDTO(pedidoId, numeroPedido, estado, fechaCreacion, fechaCompromiso, rutaId, cliente, lineas, pesoLogisticoTotal, precioTotal);
        }
    }
}
