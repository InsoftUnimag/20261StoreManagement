package com.distribuidoras.inventario.infrastructure.web.dto;

/**
 * DTO representing today's inventory movements summary.
 */
public record MovimientosHoyDTO(
        Integer entradas,
        Integer salidas,
        Integer compromisos
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer entradas;
        private Integer salidas;
        private Integer compromisos;

        public Builder entradas(Integer entradas) {
            this.entradas = entradas;
            return this;
        }

        public Builder salidas(Integer salidas) {
            this.salidas = salidas;
            return this;
        }

        public Builder compromisos(Integer compromisos) {
            this.compromisos = compromisos;
            return this;
        }

        public MovimientosHoyDTO build() {
            return new MovimientosHoyDTO(
                    entradas,
                    salidas,
                    compromisos
            );
        }
    }
}
