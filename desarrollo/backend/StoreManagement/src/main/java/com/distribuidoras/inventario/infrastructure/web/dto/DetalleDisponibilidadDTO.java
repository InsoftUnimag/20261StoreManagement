package com.distribuidoras.inventario.infrastructure.web.dto;

import java.util.UUID;

/**
 * DTO representing availability detail for a single product in a pedido.
 */
public record DetalleDisponibilidadDTO(
        String skuId,
        String marca,
        String presentacion,
        Integer cantidadSolicitada,
        Integer cantidadDisponible,
        Boolean cumple,
        String mensaje
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String skuId;
        private String marca;
        private String presentacion;
        private Integer cantidadSolicitada;
        private Integer cantidadDisponible;
        private Boolean cumple;
        private String mensaje;

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

        public Builder cantidadSolicitada(Integer cantidadSolicitada) {
            this.cantidadSolicitada = cantidadSolicitada;
            return this;
        }

        public Builder cantidadDisponible(Integer cantidadDisponible) {
            this.cantidadDisponible = cantidadDisponible;
            return this;
        }

        public Builder cumple(Boolean cumple) {
            this.cumple = cumple;
            return this;
        }

        public Builder mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public DetalleDisponibilidadDTO build() {
            return new DetalleDisponibilidadDTO(
                    skuId,
                    marca,
                    presentacion,
                    cantidadSolicitada,
                    cantidadDisponible,
                    cumple,
                    mensaje
            );
        }
    }
}
