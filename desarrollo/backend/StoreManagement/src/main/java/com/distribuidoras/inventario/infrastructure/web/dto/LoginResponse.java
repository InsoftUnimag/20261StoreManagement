package com.distribuidoras.inventario.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UUID operarioId;
    private String nombre;
    private String cedula;
    private String rol;
    private Long expiresIn;
}
