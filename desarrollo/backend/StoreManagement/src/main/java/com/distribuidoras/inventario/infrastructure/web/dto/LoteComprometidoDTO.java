package com.distribuidoras.inventario.infrastructure.web.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Committed lot info DTO.
 */
public record LoteComprometidoDTO(
        UUID compromisoId,
        String codigoLote,
        LocalDate fechaVencimiento,
        Integer cantidadComprometida
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID compromisoId;
        private String codigoLote;
        private LocalDate fechaVencimiento;
        private Integer cantidadComprometida;

        public Builder compromisoId(UUID compromisoId) {
            this.compromisoId = compromisoId;
            return this;
        }

        public Builder codigoLote(String codigoLote) {
            this.codigoLote = codigoLote;
            return this;
        }

        public Builder fechaVencimiento(LocalDate fechaVencimiento) {
            this.fechaVencimiento = fechaVencimiento;
            return this;
        }

        public Builder cantidadComprometida(Integer cantidadComprometida) {
            this.cantidadComprometida = cantidadComprometida;
            return this;
        }

        public LoteComprometidoDTO build() {
            return new LoteComprometidoDTO(compromisoId, codigoLote, fechaVencimiento, cantidadComprometida);
        }
    }
}
