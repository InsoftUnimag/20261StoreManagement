package com.distribuidoras.inventario.infrastructure.web.dto;

import com.distribuidoras.inventario.infrastructure.web.dto.PaginacionDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.PedidoResumenDTO;

import java.util.List;

/**
 * Response DTO for paginated order list.
 */
public record PedidosListResponseDTO(
        List<PedidoResumenDTO> pedidos,
        PaginacionDTO paginacion
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<PedidoResumenDTO> pedidos;
        private PaginacionDTO paginacion;

        public Builder pedidos(List<PedidoResumenDTO> pedidos) {
            this.pedidos = pedidos;
            return this;
        }

        public Builder paginacion(PaginacionDTO paginacion) {
            this.paginacion = paginacion;
            return this;
        }

        public PedidosListResponseDTO build() {
            return new PedidosListResponseDTO(pedidos, paginacion);
        }
    }
}
