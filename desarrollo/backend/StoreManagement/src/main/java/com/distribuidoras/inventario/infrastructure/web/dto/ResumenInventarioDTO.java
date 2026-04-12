package com.distribuidoras.inventario.infrastructure.web.dto;

/**
 * DTO representing inventory summary dashboard.
 */
public record ResumenInventarioDTO(
        Integer totalSkusActivos,
        Integer totalLotesConStock,
        Integer stockTotalUnidades,
        AlertasDTO alertas,
        MovimientosHoyDTO movimientosHoy
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer totalSkusActivos;
        private Integer totalLotesConStock;
        private Integer stockTotalUnidades;
        private AlertasDTO alertas;
        private MovimientosHoyDTO movimientosHoy;

        public Builder totalSkusActivos(Integer totalSkusActivos) {
            this.totalSkusActivos = totalSkusActivos;
            return this;
        }

        public Builder totalLotesConStock(Integer totalLotesConStock) {
            this.totalLotesConStock = totalLotesConStock;
            return this;
        }

        public Builder stockTotalUnidades(Integer stockTotalUnidades) {
            this.stockTotalUnidades = stockTotalUnidades;
            return this;
        }

        public Builder alertas(AlertasDTO alertas) {
            this.alertas = alertas;
            return this;
        }

        public Builder movimientosHoy(MovimientosHoyDTO movimientosHoy) {
            this.movimientosHoy = movimientosHoy;
            return this;
        }

        public ResumenInventarioDTO build() {
            return new ResumenInventarioDTO(
                    totalSkusActivos,
                    totalLotesConStock,
                    stockTotalUnidades,
                    alertas,
                    movimientosHoy
            );
        }
    }
}
