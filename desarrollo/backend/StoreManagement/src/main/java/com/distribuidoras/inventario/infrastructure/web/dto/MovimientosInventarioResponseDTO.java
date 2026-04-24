package com.distribuidoras.inventario.infrastructure.web.dto;

import java.util.List;

/**
 * DTO representing paginated inventory movements (kardex).
 */
public record MovimientosInventarioResponseDTO(
        List<MovimientoInventarioDTO> movimientos,
        PaginacionDTO paginacion
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<MovimientoInventarioDTO> movimientos;
        private PaginacionDTO paginacion;

        public Builder movimientos(List<MovimientoInventarioDTO> movimientos) {
            this.movimientos = movimientos;
            return this;
        }

        public Builder paginacion(PaginacionDTO paginacion) {
            this.paginacion = paginacion;
            return this;
        }

        public MovimientosInventarioResponseDTO build() {
            return new MovimientosInventarioResponseDTO(movimientos, paginacion);
        }
    }
}
