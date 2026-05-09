package com.distribuidoras.inventario.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for order creation and details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
public class PedidoResponseDTO {
    private UUID pedidoId;
    private String numeroPedido;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCompromiso;
    private UUID rutaId;
    private ClienteInfoDTO cliente;
    private String clienteNombre;
    private AsesorInfoDTO asesor;
    private OperarioInfoDTO operarioPicking;
    private OperarioInfoDTO operarioDespacho;
    private String direccionEntrega;
    private List<LineaPedidoResponseDTO> lineas;
    private Integer totalSolicitado;
    private Integer totalConfirmado;
    private BigDecimal pesoLogisticoTotal;
    private String tipoCumplimiento;
}
