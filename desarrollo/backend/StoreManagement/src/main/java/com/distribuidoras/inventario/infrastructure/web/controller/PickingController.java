package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.ConfirmarPickingUseCase;
import com.distribuidoras.inventario.application.usecase.ConfirmarPickingUseCase.*;
import com.distribuidoras.inventario.application.usecase.ListarPedidosPickingUseCase;
import com.distribuidoras.inventario.application.usecase.ListarPedidosAsignadosUseCase;
import com.distribuidoras.inventario.application.usecase.IniciarPickingUseCase;
import com.distribuidoras.inventario.domain.model.Pedido;
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
    private final ListarPedidosPickingUseCase listarPedidosPickingUseCase;
    private final ListarPedidosAsignadosUseCase listarPedidosAsignadosUseCase;
    private final IniciarPickingUseCase iniciarPickingUseCase;

    public PickingController(ConfirmarPickingUseCase confirmarPickingUseCase,
                              ListarPedidosPickingUseCase listarPedidosPickingUseCase,
                              ListarPedidosAsignadosUseCase listarPedidosAsignadosUseCase,
                              IniciarPickingUseCase iniciarPickingUseCase) {
        this.confirmarPickingUseCase = confirmarPickingUseCase;
        this.listarPedidosPickingUseCase = listarPedidosPickingUseCase;
        this.listarPedidosAsignadosUseCase = listarPedidosAsignadosUseCase;
        this.iniciarPickingUseCase = iniciarPickingUseCase;
    }

    /**
     * POST /api/v1/picking/iniciar
     * Inicia el proceso de picking, pasando el pedido de COMPROMETIDO a EN_PICKING.
     */
    @PostMapping("/iniciar")
    public ResponseEntity<Map<String, Object>> iniciarPicking(
            @RequestBody IniciarPickingRequestDTO request) {
        log.info("REST: Iniciando picking para pedido {} por operario {}", request.pedidoId(), request.operarioId());
        
        Pedido pedido = iniciarPickingUseCase.ejecutar(request.pedidoId(), request.operarioId());
        
        return ResponseEntity.ok(Map.of(
                "pedido_id", pedido.getPedidoId(),
                "nuevo_estado", pedido.getEstado().name()
        ));
    }

    public record IniciarPickingRequestDTO(UUID pedidoId, UUID operarioId) {}

    /**
     * GET /api/v1/picking/pedidos
     * Lista pedidos en estado COMPROMETIDO para picking.
     */
    @GetMapping("/pedidos")
    public ResponseEntity<List<ListarPedidosPickingUseCase.PedidoPickingDTO>> listarPedidosParaPicking() {
        log.info("REST: Listando pedidos para picking");
        List<ListarPedidosPickingUseCase.PedidoPickingDTO> pedidos = listarPedidosPickingUseCase.ejecutar();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/mis-pedidos/{operarioId}")
    public ResponseEntity<List<Pedido>> listarMisPedidos(@PathVariable UUID operarioId) {
        log.info("REST: Listando pedidos asignados a operario {}", operarioId);
        List<Pedido> pedidos = listarPedidosAsignadosUseCase.ejecutar(operarioId, "picking");
        return ResponseEntity.ok(pedidos);
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
