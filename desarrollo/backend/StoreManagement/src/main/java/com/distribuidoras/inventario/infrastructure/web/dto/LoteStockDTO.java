package com.distribuidoras.inventario.infrastructure.web.dto;

import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * DTO representing stock information for a single lot.
 * Used in inventory consultation endpoints.
 */
public record LoteStockDTO(
        String codigoLote,
        LocalDate fechaVencimiento,
        LocalDate fechaExpedicion,
        Integer cantidad,
        Integer diasHastaVencimiento,
        Boolean urgente,
        String estado,
        BigDecimal costoUnitarioProducto) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String codigoLote;
        private LocalDate fechaVencimiento;
        private LocalDate fechaExpedicion;
        private Integer cantidad;
        private Integer diasHastaVencimiento;
        private Boolean urgente;
        private String estado;
        private BigDecimal costoUnitarioProducto;

        public Builder codigoLote(String codigoLote) {
            this.codigoLote = codigoLote;
            return this;
        }

        public Builder fechaVencimiento(LocalDate fechaVencimiento) {
            this.fechaVencimiento = fechaVencimiento;
            return this;
        }

        public Builder fechaExpedicion(LocalDate fechaExpedicion) {
            this.fechaExpedicion = fechaExpedicion;
            return this;
        }

        public Builder cantidad(Integer cantidad) {
            this.cantidad = cantidad;
            return this;
        }

        public Builder diasHastaVencimiento(Integer diasHastaVencimiento) {
            this.diasHastaVencimiento = diasHastaVencimiento;
            return this;
        }

        public Builder urgente(Boolean urgente) {
            this.urgente = urgente;
            return this;
        }

        public Builder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public Builder costoUnitarioProducto(java.math.BigDecimal costoUnitarioProducto) {
            this.costoUnitarioProducto = costoUnitarioProducto;
            return this;
        }

        public LoteStockDTO build() {
            return new LoteStockDTO(
                    codigoLote,
                    fechaVencimiento,
                    fechaExpedicion,
                    cantidad,
                    diasHastaVencimiento,
                    urgente,
                    estado,
                    costoUnitarioProducto);
        }
    }
}
