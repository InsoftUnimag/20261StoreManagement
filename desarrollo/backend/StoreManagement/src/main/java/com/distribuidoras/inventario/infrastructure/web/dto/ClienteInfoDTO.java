package com.distribuidoras.inventario.infrastructure.web.dto;

/**
 * Client info DTO for order responses.
 */
public record ClienteInfoDTO(
        String cedula,
        String nombre,
        String telefono,
        String direccion
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String cedula;
        private String nombre;
        private String telefono;
        private String direccion;

        public Builder cedula(String cedula) {
            this.cedula = cedula;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder telefono(String telefono) {
            this.telefono = telefono;
            return this;
        }

        public Builder direccion(String direccion) {
            this.direccion = direccion;
            return this;
        }

        public ClienteInfoDTO build() {
            return new ClienteInfoDTO(cedula, nombre, telefono, direccion);
        }
    }
}
