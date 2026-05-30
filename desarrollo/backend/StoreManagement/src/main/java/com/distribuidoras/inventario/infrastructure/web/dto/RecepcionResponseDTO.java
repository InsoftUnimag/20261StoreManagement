package com.distribuidoras.inventario.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecepcionResponseDTO {
    private Long recepcionId;
    private Long manifiestoId;
    private Long operarioId;
    private String operarioNombre;
    private String operarioCedula;
    private LocalDateTime fechaRecepcion;
    private String notas;
    private String numeroRecepcion;
}
