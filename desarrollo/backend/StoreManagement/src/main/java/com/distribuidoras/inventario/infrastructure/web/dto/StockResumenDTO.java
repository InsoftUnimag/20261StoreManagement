package com.distribuidoras.inventario.infrastructure.web.dto;


/**
 * DTO representing stock summary for a single SKU.
 */
public record StockResumenDTO(
        String skuId,
        String marca,
        String presentacion,
        Integer fisicoTotal
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String skuId;
        private String marca;
        private String presentacion;
        private Integer fisicoTotal;

        public Builder skuId(String skuId) {
            this.skuId = skuId;
            return this;
        }

        public Builder marca(String marca) {
            this.marca = marca;
            return this;
        }

        public Builder presentacion(String presentacion) {
            this.presentacion = presentacion;
            return this;
        }

        public Builder fisicoTotal(Integer fisicoTotal) {
            this.fisicoTotal = fisicoTotal;
            return this;
        }

        public StockResumenDTO build() {
            return new StockResumenDTO(
                    skuId,
                    marca,
                    presentacion,
                    fisicoTotal
            );
        }
    }
}
