package com.distribuidoras.inventario.infrastructure.web.dto;

import java.util.List;

/**
 * Order line response DTO.
 */
public record LineaPedidoResponseDTO(
        Long productoPedidoId,
        ProductoInfoDTO producto,
        Integer cantidadSolicitada,
        Integer cantidadConfirmada,
        String skuId,
        List<LoteComprometidoDTO> lotesComprometidos
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long productoPedidoId;
        private ProductoInfoDTO producto;
        private Integer cantidadSolicitada;
        private Integer cantidadConfirmada;
        private String skuId;
        private List<LoteComprometidoDTO> lotesComprometidos;

        public Builder productoPedidoId(Long productoPedidoId) {
            this.productoPedidoId = productoPedidoId;
            return this;
        }

        public Builder skuId(String skuId) {
            this.skuId = skuId;
            return this;
        }

        public Builder producto(ProductoInfoDTO producto) {
            this.producto = producto;
            return this;
        }

        public Builder cantidadSolicitada(Integer cantidadSolicitada) {
            this.cantidadSolicitada = cantidadSolicitada;
            return this;
        }

        public Builder cantidadConfirmada(Integer cantidadConfirmada) {
            this.cantidadConfirmada = cantidadConfirmada;
            return this;
        }

        public Builder lotesComprometidos(List<LoteComprometidoDTO> lotesComprometidos) {
            this.lotesComprometidos = lotesComprometidos;
            return this;
        }

        public LineaPedidoResponseDTO build() {
            return new LineaPedidoResponseDTO(
                    productoPedidoId,
                    producto,
                    cantidadSolicitada,
                    cantidadConfirmada,
                    skuId,
                    lotesComprometidos);
        }
    }
}
