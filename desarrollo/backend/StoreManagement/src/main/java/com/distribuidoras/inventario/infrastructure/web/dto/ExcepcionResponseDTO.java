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
public class ExcepcionResponseDTO {
    private Long excepcionId;
    private String tipoExcepcion;
    private String codigoLote;
    private String skuId;
    private Integer cantidadAfectada;
    private LocalDateTime fechaRegistro;
    private Long operarioId;
    private String operarioNombre;
    private String operarioCedula;
    private String descripcion;
    private String evidenciaUrl;
}
