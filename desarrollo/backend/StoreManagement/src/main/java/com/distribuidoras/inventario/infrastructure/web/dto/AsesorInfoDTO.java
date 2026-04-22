package com.distribuidoras.inventario.infrastructure.web.dto;

import java.util.UUID;

/**
 * Advisor info DTO for order responses.
 */
public record AsesorInfoDTO(
        UUID asesorId,
        String nombre
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID asesorId;
        private String nombre;

        public Builder asesorId(UUID asesorId) {
            this.asesorId = asesorId;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public AsesorInfoDTO build() {
            return new AsesorInfoDTO(asesorId, nombre);
        }
    }
}
