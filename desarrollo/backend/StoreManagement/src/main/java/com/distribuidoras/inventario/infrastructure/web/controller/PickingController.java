package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.ConfirmarPickingUseCase;
import com.distribuidoras.inventario.application.usecase.ConfirmarPickingUseCase.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for picking confirmation.
 * Spec 11: Confirmar Picking
 */
@RestController
@RequestMapping("/api/v1/picking")
public class PickingController {

    private static final Logger log = LoggerFactory.getLogger(PickingController.class);

    private final ConfirmarPickingUseCase confirmarPickingUseCase;

    public PickingController(ConfirmarPickingUseCase confirmarPickingUseCase) {
        this.confirmarPickingUseCase = confirmarPickingUseCase;
    }

    /**
     * POST /api/v1/picking/confirmar
     * Confirmar picking de pedido comprometido.
     */
    @PostMapping("/confirmar")
    public ResponseEntity<Map<String, Object>> confirmarPicking(
            @RequestBody ConfirmarPickingRequestDTO request) {
        
        log.info("REST: Confirmando picking para pedido {}", request.pedidoId());
        
        List<LineaPickingCommand> lineas = request.lineasRecolectadas().stream()
                .map(l -> new LineaPickingCommand(l.productoPedidoId(), l.cantidadRecolectada()))
                .toList();
        
        ConfirmarPickingCommand command = new ConfirmarPickingCommand(
                request.pedidoId(),
                request.operarioId(),
                lineas
        );
        
        ConfirmarPickingResult result = confirmarPickingUseCase.ejecutar(command);
        
        Map<String, Object> response = Map.of(
                "exitoso", result.exitoso(),
                "numero_pedido", result.numeroPedido(),
                "nuevo_estado", result.nuevoEstado().name(),
                "alertas", result.alertas()
        );
        
        return ResponseEntity.ok(response);
    }

    public record ConfirmarPickingRequestDTO(
            UUID pedidoId,
            UUID operarioId,
            List<LineaPickingRequestDTO> lineasRecolectadas
    ) {}

    public record LineaPickingRequestDTO(
            UUID productoPedidoId,
            Integer cantidadRecolectada
    ) {}
}
