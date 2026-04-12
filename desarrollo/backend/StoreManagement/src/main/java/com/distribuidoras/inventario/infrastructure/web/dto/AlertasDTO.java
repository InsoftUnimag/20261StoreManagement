package com.distribuidoras.inventario.infrastructure.web.dto;

/**
 * DTO representing inventory alerts.
 */
public record AlertasDTO(
        Integer proximosVencer30Dias,
        Integer stockBajo,
        Integer excepcionesAbiertas
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer proximosVencer30Dias;
        private Integer stockBajo;
        private Integer excepcionesAbiertas;

        public Builder proximosVencer30Dias(Integer proximosVencer30Dias) {
            this.proximosVencer30Dias = proximosVencer30Dias;
            return this;
        }

        public Builder stockBajo(Integer stockBajo) {
            this.stockBajo = stockBajo;
            return this;
        }

        public Builder excepcionesAbiertas(Integer excepcionesAbiertas) {
            this.excepcionesAbiertas = excepcionesAbiertas;
            return this;
        }

        public AlertasDTO build() {
            return new AlertasDTO(
                    proximosVencer30Dias,
                    stockBajo,
                    excepcionesAbiertas
            );
        }
    }
}
