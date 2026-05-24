package com.distribuidoras.inventario.infrastructure.web.dto;

/**
 * Advisor info DTO for order responses.
 */
public record AsesorInfoDTO(
        Long asesorId,
        String nombre
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long asesorId;
        private String nombre;

        public Builder asesorId(Long asesorId) {
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
