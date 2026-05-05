package com.distribuidoras.inventario.domain.model;

import lombok.*;

/**
 * External entity: Cliente from Módulo de Usuarios.
 * Read-only, not persisted locally.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    private String cedula;
    private String nombre;
    private String telefono;
    private String email;
    private String direccion;
    private Boolean activo;
}
