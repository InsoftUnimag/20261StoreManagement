package com.distribuidoras.inventario.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperarioDTO {
    private Long id;
    private String nombre;
    private String cedula;
    private String rol;
    private Boolean activo;
}