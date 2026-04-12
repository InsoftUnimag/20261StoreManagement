package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.ConfirmarDespachoUseCase;
import com.distribuidoras.inventario.application.usecase.ConfirmarDespachoUseCase.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for dispatch confirmation.
 * Spec 14: Confirmar Despacho
 */
@RestController
@RequestMapping("/api/v1/despacho")
public class DespachoController {

    private static final Logger log = LoggerFactory.getLogger(DespachoController.class);

    private final ConfirmarDespachoUseCase confirmarDespachoUseCase;

    public DespachoController(ConfirmarDespachoUseCase confirmarDespachoUseCase) {
        this.confirmarDespachoUseCase = confirmarDespachoUseCase;
    }

    /**
     * POST /api/v1/despacho/confirmar
     * Confirmar despacho de pedido en estado "En Picking".
     */
    @PostMapping("/confirmar")
    public ResponseEntity<Map<String, Object>> confirmarDespacho(
            @RequestBody ConfirmarDespachoRequestDTO request) {
        
        log.info("REST: Confirmando despacho para pedido {}", request.pedidoId());
        
        ConfirmarDespachoCommand command = new ConfirmarDespachoCommand(
                request.pedidoId(),
                request.operarioId(),
                request.cantidadesDespachadas()
        );
        
        ConfirmarDespachoResult result = confirmarDespachoUseCase.ejecutar(command);
        
        Map<String, Object> response = Map.of(
                "exitoso", result.exitoso(),
                "numero_pedido", result.numeroPedido(),
                "nuevo_estado", result.nuevoEstado().name(),
                "despacho_parcial", result.despachoParcial(),
                "alertas", result.alertas()
        );
        
        return ResponseEntity.ok(response);
    }

    public record ConfirmarDespachoRequestDTO(
            UUID pedidoId,
            UUID operarioId,
            Map<UUID, Integer> cantidadesDespachadas
    ) {}
}
