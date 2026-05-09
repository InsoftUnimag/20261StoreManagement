package com.distribuidoras.inventario.infrastructure.web.dto;

import java.util.UUID;
import lombok.*;

/**
 * DTO for operario information in order details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperarioInfoDTO {
    private UUID id;
    private String nombre;
    private String cedula;
    private String rol;
}
