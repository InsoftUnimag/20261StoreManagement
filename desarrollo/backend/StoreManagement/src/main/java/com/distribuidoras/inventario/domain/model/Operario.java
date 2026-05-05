package com.distribuidoras.inventario.domain.model;

import com.distribuidoras.inventario.domain.model.enums.RolUsuario;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Operario {
    private UUID id;
    private String nombre;
    private String cedula;
    private Boolean activo;
    private RolUsuario rol;
}