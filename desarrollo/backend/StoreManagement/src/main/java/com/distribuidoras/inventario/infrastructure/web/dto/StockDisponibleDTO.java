package com.distribuidoras.inventario.infrastructure.web.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing available stock for a SKU with FEFO lot details.
 * Spec 05: Consultar Inventario
 */
public record StockDisponibleDTO(
        ProductoInfo sku,
        Integer fisicoTotal,
        Integer disponibles,
        Integer comprometidos,
        List<LoteStockDTO> lotes,
        ProximoVencimientoInfo proximoVencimiento
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ProductoInfo sku;
        private Integer fisicoTotal;
        private Integer disponibles;
        private Integer comprometidos;
        private List<LoteStockDTO> lotes;
        private ProximoVencimientoInfo proximoVencimiento;

        public Builder sku(ProductoInfo sku) {
            this.sku = sku;
            return this;
        }

        public Builder fisicoTotal(Integer fisicoTotal) {
            this.fisicoTotal = fisicoTotal;
            return this;
        }
        
        public Builder disponibles(Integer disponibles) {
            this.disponibles = disponibles;
            return this;
        }
        
        public Builder comprometidos(Integer comprometidos) {
            this.comprometidos = comprometidos;
            return this;
        }

        public Builder lotes(List<LoteStockDTO> lotes) {
            this.lotes = lotes;
            return this;
        }

        public Builder proximoVencimiento(ProximoVencimientoInfo proximoVencimiento) {
            this.proximoVencimiento = proximoVencimiento;
            return this;
        }

        public StockDisponibleDTO build() {
            return new StockDisponibleDTO(
                    sku,
                    fisicoTotal,
                    disponibles,
                    comprometidos,
                    lotes,
                    proximoVencimiento
            );
        }
    }

    public record ProductoInfo(
            String skuId,
            String marca,
            String presentacion,
            Integer contenidoMl,
            Double pesoLogisticoKg
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String skuId;
            private String marca;
            private String presentacion;
            private Integer contenidoMl;
            private Double pesoLogisticoKg;

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

            public Builder pesoLogisticoKg(Double pesoLogisticoKg) {
                this.pesoLogisticoKg = pesoLogisticoKg;
                return this;
            }

            public ProductoInfo build() {
                return new ProductoInfo(
                        skuId,
                        marca,
                        presentacion,
                        contenidoMl,
                        pesoLogisticoKg
                );
            }
        }
    }

    public record ProximoVencimientoInfo(
            String codigoLote,
            LocalDate fechaVencimiento,
            Integer diasRestantes
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String codigoLote;
            private LocalDate fechaVencimiento;
            private Integer diasRestantes;

            public Builder codigoLote(String codigoLote) {
                this.codigoLote = codigoLote;
                return this;
            }

            public Builder fechaVencimiento(LocalDate fechaVencimiento) {
                this.fechaVencimiento = fechaVencimiento;
                return this;
            }

            public Builder diasRestantes(Integer diasRestantes) {
                this.diasRestantes = diasRestantes;
                return this;
            }

            public ProximoVencimientoInfo build() {
                return new ProximoVencimientoInfo(
                        codigoLote,
                        fechaVencimiento,
                        diasRestantes
                );
            }
        }
    }
}
