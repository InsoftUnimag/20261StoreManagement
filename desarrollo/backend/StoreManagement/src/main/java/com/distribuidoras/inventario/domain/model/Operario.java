package com.distribuidoras.inventario.domain.model;

import com.distribuidoras.inventario.domain.model.enums.RolUsuario;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Operario {
    @NotNull(message = "El ID del operario no puede ser nulo")
    private UUID operarioId;
    
    @NotNull(message = "El nombre del operario no puede ser nulo")

    @NotNull(message = "La cédula del operario no puede ser nula")
    private String cedula;

    @NotNull(message = "El nombre del operario no puede ser nulo")
    private String nombre;

    @NotNull(message = "El rol del operario no puede ser nulo")
    private Boolean activo;

    @NotNull(message = "El rol del operario no puede ser nulo")
    private RolUsuario rol;
    
}