package com.distribuidoras.inventario.infrastructure.web.dto;

/**
 * Client info DTO for external API responses.
 */
public record ClienteExternalDTO(
        String cedula,
        String nombre,
        String direccion,
        String telefono
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String cedula;
        private String nombre;
        private String direccion;
        private String telefono;

        public Builder cedula(String cedula) { this.cedula = cedula; return this; }
        public Builder nombre(String nombre) { this.nombre = nombre; return this; }
        public Builder direccion(String direccion) { this.direccion = direccion; return this; }
        public Builder telefono(String telefono) { this.telefono = telefono; return this; }

        public ClienteExternalDTO build() {
            return new ClienteExternalDTO(cedula, nombre, direccion, telefono);
        }
    }
}
