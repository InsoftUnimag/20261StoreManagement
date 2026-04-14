package com.distribuidoras.inventario.infrastructure.web.dto;

import java.util.List;

/**
 * DTO representing availability check result for a pedido (order).
 * Spec 06: Consultar Disponibilidad
 */
public record DisponibilidadDTO(
        String pedidoId,
        Boolean disponible,
        List<DetalleDisponibilidadDTO> detalles,
        String mensajeAlerta
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String pedidoId;
        private Boolean disponible;
        private List<DetalleDisponibilidadDTO> detalles;
        private String mensajeAlerta;

        public Builder pedidoId(String pedidoId) {
            this.pedidoId = pedidoId;
            return this;
        }

        public Builder disponible(Boolean disponible) {
            this.disponible = disponible;
            return this;
        }

        public Builder detalles(List<DetalleDisponibilidadDTO> detalles) {
            this.detalles = detalles;
            return this;
        }

        public Builder mensajeAlerta(String mensajeAlerta) {
            this.mensajeAlerta = mensajeAlerta;
            return this;
        }

        public DisponibilidadDTO build() {
            return new DisponibilidadDTO(
                    pedidoId,
                    disponible,
                    detalles,
                    mensajeAlerta
            );
        }
    }
}
