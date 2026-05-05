package com.distribuidoras.inventario.infrastructure.web.dto;

import java.math.BigDecimal;

/**
 * Product info DTO.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
public record ProductoInfoDTO(
        String skuId,
        String marca,
        String presentacion,
        Integer contenidoMl,
        BigDecimal pesoLogisticoKg
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String skuId;
        private String marca;
        private String presentacion;
        private Integer contenidoMl;
        private BigDecimal pesoLogisticoKg;

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

        public Builder contenidoMl(Integer contenidoMl) {
            this.contenidoMl = contenidoMl;
            return this;
        }

        public Builder pesoLogisticoKg(BigDecimal pesoLogisticoKg) {
            this.pesoLogisticoKg = pesoLogisticoKg;
            return this;
        }

        public ProductoInfoDTO build() {
            return new ProductoInfoDTO(skuId, marca, presentacion, contenidoMl, pesoLogisticoKg);
        }
    }
}
