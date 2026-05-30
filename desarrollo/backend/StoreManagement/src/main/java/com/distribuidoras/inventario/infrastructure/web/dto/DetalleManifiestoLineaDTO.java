package com.distribuidoras.inventario.infrastructure.web.dto;

public record DetalleManifiestoLineaDTO(
        String detalleId,
        String skuId,
        String marca,
        String presentacion,
        Integer contenidoMl,
        Integer cantidadEsperada,
        Integer cantidadRecibida) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String detalleId;
        private String skuId;
        private String marca;
        private String presentacion;
        private Integer contenidoMl;
        private Integer cantidadEsperada;
        private Integer cantidadRecibida;

        public Builder detalleId(String id) {
            this.detalleId = id;
            return this;
        }

        public Builder skuId(String s) {
            this.skuId = s;
            return this;
        }

        public Builder marca(String m) {
            this.marca = m;
            return this;
        }

        public Builder presentacion(String p) {
            this.presentacion = p;
            return this;
        }

        public Builder contenidoMl(Integer c) {
            this.contenidoMl = c;
            return this;
        }

        public Builder cantidadEsperada(Integer c) {
            this.cantidadEsperada = c;
            return this;
        }

        public Builder cantidadRecibida(Integer c) {
            this.cantidadRecibida = c;
            return this;
        }

        public DetalleManifiestoLineaDTO build() {
            return new DetalleManifiestoLineaDTO(detalleId, skuId, marca, presentacion, contenidoMl, cantidadEsperada,
                    cantidadRecibida);
        }
    }
}
