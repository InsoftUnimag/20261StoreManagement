package com.distribuidoras.inventario.infrastructure.web.dto;

import java.math.BigDecimal;

/**
 * Order line DTO for external API responses.
 */
public record LineaExternalDTO(
        String skuId,
        String marca,
        String presentacion,
        Integer cantidadSolicitada,
        Integer cantidadConfirmada,
        BigDecimal pesoLogisticoUnitario,
        BigDecimal pesoLogisticoTotal
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String skuId;
        private String marca;
        private String presentacion;
        private Integer cantidadSolicitada;
        private Integer cantidadConfirmada;
        private BigDecimal pesoLogisticoUnitario;
        private BigDecimal pesoLogisticoTotal;

        public Builder skuId(String skuId) { this.skuId = skuId; return this; }
        public Builder marca(String marca) { this.marca = marca; return this; }
        public Builder presentacion(String presentacion) { this.presentacion = presentacion; return this; }
        public Builder cantidadSolicitada(Integer cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; return this; }
        public Builder cantidadConfirmada(Integer cantidadConfirmada) { this.cantidadConfirmada = cantidadConfirmada; return this; }
        public Builder pesoLogisticoUnitario(BigDecimal pesoLogisticoUnitario) { this.pesoLogisticoUnitario = pesoLogisticoUnitario; return this; }
        public Builder pesoLogisticoTotal(BigDecimal pesoLogisticoTotal) { this.pesoLogisticoTotal = pesoLogisticoTotal; return this; }

        public LineaExternalDTO build() {
            return new LineaExternalDTO(skuId, marca, presentacion, cantidadSolicitada, cantidadConfirmada, pesoLogisticoUnitario, pesoLogisticoTotal);
        }
    }
}
