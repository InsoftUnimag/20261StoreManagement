package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.ConfirmarDespachoUseCase;
import com.distribuidoras.inventario.application.usecase.ConfirmarDespachoUseCase.*;
import com.distribuidoras.inventario.application.usecase.ListarPedidosDespachoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    private final ListarPedidosDespachoUseCase listarPedidosDespachoUseCase;

    public DespachoController(ConfirmarDespachoUseCase confirmarDespachoUseCase,
                               ListarPedidosDespachoUseCase listarPedidosDespachoUseCase) {
        this.confirmarDespachoUseCase = confirmarDespachoUseCase;
        this.listarPedidosDespachoUseCase = listarPedidosDespachoUseCase;
    }

    /**
     * GET /api/v1/despacho/pedidos
     * Lista pedidos en estado EN_PICKING para despacho.
     */
    @GetMapping("/pedidos")
    public ResponseEntity<List<ListarPedidosDespachoUseCase.PedidoDespachoDTO>> listarPedidosParaDespacho() {
        log.info("REST: Listando pedidos para despacho");
        List<ListarPedidosDespachoUseCase.PedidoDespachoDTO> pedidos = listarPedidosDespachoUseCase.ejecutar();
        return ResponseEntity.ok(pedidos);
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
                request.transportista(),
                request.placaVehiculo(),
                request.observaciones(),
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
            String transportista,
            String placaVehiculo,
            String observaciones,
            Map<UUID, Integer> cantidadesDespachadas
    ) {}
}
