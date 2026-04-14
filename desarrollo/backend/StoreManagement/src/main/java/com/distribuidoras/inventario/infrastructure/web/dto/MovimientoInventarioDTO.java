package com.distribuidoras.inventario.infrastructure.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a single inventory movement.
 */
public record MovimientoInventarioDTO(
        String movimientoId,
        String tipoMovimiento,
        Integer cantidad,
        LocalDateTime fechaMovimiento,
        LoteInfo lote,
        ProductoInfo producto,
        String operarioNombre,
        String pedidoId,
        String observaciones
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String movimientoId;
        private String tipoMovimiento;
        private Integer cantidad;
        private LocalDateTime fechaMovimiento;
        private LoteInfo lote;
        private ProductoInfo producto;
        private String operarioNombre;
        private String pedidoId;
        private String observaciones;

        public Builder movimientoId(String movimientoId) {
            this.movimientoId = movimientoId;
            return this;
        }

        public Builder tipoMovimiento(String tipoMovimiento) {
            this.tipoMovimiento = tipoMovimiento;
            return this;
        }

        public Builder cantidad(Integer cantidad) {
            this.cantidad = cantidad;
            return this;
        }

        public Builder fechaMovimiento(LocalDateTime fechaMovimiento) {
            this.fechaMovimiento = fechaMovimiento;
            return this;
        }

        public Builder lote(LoteInfo lote) {
            this.lote = lote;
            return this;
        }

        public Builder producto(ProductoInfo producto) {
            this.producto = producto;
            return this;
        }

        public Builder operarioNombre(String operarioNombre) {
            this.operarioNombre = operarioNombre;
            return this;
        }

        public Builder pedidoId(String pedidoId) {
            this.pedidoId = pedidoId;
            return this;
        }

        public Builder observaciones(String observaciones) {
            this.observaciones = observaciones;
            return this;
        }

        public MovimientoInventarioDTO build() {
            return new MovimientoInventarioDTO(
                    movimientoId,
                    tipoMovimiento,
                    cantidad,
                    fechaMovimiento,
                    lote,
                    producto,
                    operarioNombre,
                    pedidoId,
                    observaciones
            );
        }
    }

    public record LoteInfo(
            String codigoLote,
            String codigoLoteInterno
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String codigoLote;
            private String codigoLoteInterno;

            public Builder codigoLote(String codigoLote) {
                this.codigoLote = codigoLote;
                return this;
            }

            public Builder codigoLoteInterno(String codigoLoteInterno) {
                this.codigoLoteInterno = codigoLoteInterno;
                return this;
            }

            public LoteInfo build() {
                return new LoteInfo(codigoLote, codigoLoteInterno);
            }
        }
    }

    public record ProductoInfo(
            String skuId,
            String marca,
            String presentacion
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String skuId;
            private String marca;
            private String presentacion;

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

            public ProductoInfo build() {
                return new ProductoInfo(skuId, marca, presentacion);
            }
        }
    }
}
