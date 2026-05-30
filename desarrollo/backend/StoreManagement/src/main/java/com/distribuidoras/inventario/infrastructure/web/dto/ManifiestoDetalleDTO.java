package com.distribuidoras.inventario.infrastructure.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ManifiestoDetalleDTO(
        String manifiestoId,
        String numeroManifiesto,
        LocalDateTime fechaEmision,
        String proveedor,
        String estado,
        List<DetalleManifiestoLineaDTO> lineas) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String manifiestoId;
        private String numeroManifiesto;
        private LocalDateTime fechaEmision;
        private String proveedor;
        private String estado;
        private List<DetalleManifiestoLineaDTO> lineas;

        public Builder manifiestoId(String id) {
            this.manifiestoId = id;
            return this;
        }

        public Builder numeroManifiesto(String n) {
            this.numeroManifiesto = n;
            return this;
        }

        public Builder fechaEmision(LocalDateTime f) {
            this.fechaEmision = f;
            return this;
        }

        public Builder proveedor(String p) {
            this.proveedor = p;
            return this;
        }

        public Builder estado(String e) {
            this.estado = e;
            return this;
        }

        public Builder lineas(List<DetalleManifiestoLineaDTO> l) {
            this.lineas = l;
            return this;
        }

        public ManifiestoDetalleDTO build() {
            return new ManifiestoDetalleDTO(manifiestoId, numeroManifiesto, fechaEmision, proveedor, estado,
                    lineas);
        }
    }
}
