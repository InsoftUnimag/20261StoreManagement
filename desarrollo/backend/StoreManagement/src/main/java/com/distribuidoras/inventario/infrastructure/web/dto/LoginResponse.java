package com.distribuidoras.inventario.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long operarioId;
    private String nombre;
    private String cedula;
    private String rol;
    private Long expiresIn;
}
