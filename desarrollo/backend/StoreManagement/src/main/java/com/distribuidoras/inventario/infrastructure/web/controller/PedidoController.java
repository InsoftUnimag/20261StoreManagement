package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.*;
import com.distribuidoras.inventario.application.usecase.ComprometerInventarioUseCase.ComprometerCommand;
import com.distribuidoras.inventario.application.usecase.ComprometerInventarioUseCase.ComprometerResult;
import com.distribuidoras.inventario.application.usecase.ConsultarListaPedidosUseCase.FiltrosDTO;
import com.distribuidoras.inventario.application.usecase.RealizarPedidoUseCase.PedidoCommand;
import com.distribuidoras.inventario.application.usecase.RealizarPedidoUseCase.LineaCommand;
import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.infrastructure.messaging.SolicitudRutaProducer;
import com.distribuidoras.inventario.infrastructure.web.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for order management.
 * Spec 08: Realizar Pedido
 * Spec 09: Consultar Detalle de Pedido
 * Spec 10: Listar Pedidos Comprometidos
 */
@RestController
@RequestMapping("/api/v1/pedidos")
@Validated
public class PedidoController {

        private static final Logger log = LoggerFactory.getLogger(PedidoController.class);

        private final RealizarPedidoUseCase realizarPedidoUseCase;
        private final ComprometerInventarioUseCase comprometerInventarioUseCase;
        private final ConsultarDetallePedidoUseCase consultarDetallePedidoUseCase;
        private final ConsultarListaPedidosUseCase consultarListaPedidosUseCase;
        private final SolicitudRutaProducer solicitudRutaProducer;
        private final AsignarPedidoUseCase asignarPedidoUseCase;

        public PedidoController(RealizarPedidoUseCase realizarPedidoUseCase,
                        ComprometerInventarioUseCase comprometerInventarioUseCase,
                        ConsultarDetallePedidoUseCase consultarDetallePedidoUseCase,
                        ConsultarListaPedidosUseCase consultarListaPedidosUseCase,
                        SolicitudRutaProducer solicitudRutaProducer,
                        AsignarPedidoUseCase asignarPedidoUseCase) {
                this.realizarPedidoUseCase = realizarPedidoUseCase;
                this.comprometerInventarioUseCase = comprometerInventarioUseCase;
                this.consultarDetallePedidoUseCase = consultarDetallePedidoUseCase;
                this.consultarListaPedidosUseCase = consultarListaPedidosUseCase;
                this.solicitudRutaProducer = solicitudRutaProducer;
                this.asignarPedidoUseCase = asignarPedidoUseCase;
        }

        /**
         * POST /api/v1/pedidos
         * Crear pedido sin comprometer inventario.
         */
        @PostMapping
        public ResponseEntity<Map<String, Object>> crearPedido(
                        @Valid @RequestBody PedidoRequestDTO request) {

                log.info("REST: Creando pedido para cliente {}", request.clienteCc());

                PedidoCommand command = new PedidoCommand(
                                request.clienteCc(),
                                request.asesorId(),
                                request.lineas().stream()
                                                .map(linea -> new LineaCommand(linea.skuId(),
                                                                linea.cantidadSolicitada()))
                                                .toList());

                Pedido pedido = realizarPedidoUseCase.ejecutar(command);

                Map<String, Object> response = Map.of(
                                "pedido_id", pedido.getPedidoId().toString(),
                                "numero_pedido", pedido.getNumeroPedido(),
                                "estado", pedido.getEstado().name(),
                                "fecha_creacion", pedido.getFechaCreacion().toString(),
                                "message", "Pedido creado exitosamente en estado ESPERANDO_RUTA");

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * POST /api/v1/pedidos/{pedidoId}/comprometer
         * Comprometer inventario cuando Módulo 2 asigna ruta.
         */
        @PostMapping("/{pedidoId}/comprometer")
        public ResponseEntity<Map<String, Object>> comprometerInventario(
                        @PathVariable @NotBlank String pedidoId,
                        @RequestBody Map<String, String> request) {

                log.info("REST: Comprometiendo inventario para pedido {}", pedidoId);

                Long rutaId = Long.parseLong(request.get("ruta_id"));

                ComprometerCommand command = new ComprometerCommand(
                                Long.parseLong(pedidoId),
                                rutaId);

                ComprometerResult result = comprometerInventarioUseCase.ejecutar(command);

                Map<String, Object> response = Map.of(
                                "numero_pedido", result.numeroPedido(),
                                "exitoso", result.exitoso(),
                                "alertas", result.alertas());

                return ResponseEntity.ok(response);
        }

        /**
         * GET /api/v1/pedidos/{id}
         * Consultar detalle de pedido por ID o número.
         */
        @GetMapping("/{id}")
        public ResponseEntity<PedidoResponseDTO> consultarDetalle(@PathVariable @NotBlank String id) {
                log.info("REST: Consultando detalle de pedido {}", id);

                PedidoResponseDTO detalle = consultarDetallePedidoUseCase.ejecutar(id);

                return ResponseEntity.ok(detalle);
        }

        /**
         * GET /api/v1/pedidos
         * Listar pedidos con filtros y paginación.
         */
        @GetMapping
        public ResponseEntity<PedidosListResponseDTO> listarPedidos(
                        @RequestParam(required = false) String estado,
                        @RequestParam(required = false) String clienteCc,
                        @RequestParam(required = false) String numeroPedido,
                        @RequestParam(required = false) LocalDate fechaDesde,
                        @RequestParam(required = false) LocalDate fechaHasta,
                        @RequestParam(defaultValue = "0") @Min(0) Integer page,
                        @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size) {

                log.info("REST: Listando pedidos con filtros");

                FiltrosDTO filtros = new FiltrosDTO(
                                estado != null && !estado.isBlank()
                                                ? EstadoPedido.valueOf(estado)
                                                : null,
                                clienteCc,
                                numeroPedido,
                                fechaDesde,
                                fechaHasta,
                                page,
                                size);

                PedidosListResponseDTO response = consultarListaPedidosUseCase.ejecutar(filtros);

                return ResponseEntity.ok(response);
        }

        /**
         * GET /api/v1/pedidos/comprometidos
         * Listar pedidos comprometidos listos para picking (FIFO).
         */
        @GetMapping("/comprometidos")
        public ResponseEntity<List<PedidoResumenDTO>> listarPedidosComprometidos() {
                log.info("REST: Listando pedidos comprometidos (FIFO)");

                List<PedidoResumenDTO> pedidos = consultarListaPedidosUseCase.listarPedidosComprometidos();

                return ResponseEntity.ok(pedidos);
        }

        /**
         * POST /api/v1/pedidos/{pedidoId}/solicitar-ruta
         * Solicitar ruta a Módulo 2 de Logística.
         * Spec 13: Solicitar Ruta
         */
        @PostMapping("/{pedidoId}/solicitar-ruta")
        public ResponseEntity<Map<String, Object>> solicitarRuta(@PathVariable @NotBlank Long pedidoId) {
                log.info("REST: Solicitando ruta para pedido {}", pedidoId);

                solicitudRutaProducer.enviarSolicitudRuta(pedidoId);

                Map<String, Object> response = Map.of(
                                "mensaje", "Solicitud de ruta enviada a logística de transporte",
                                "pedido_id", pedidoId);

                return ResponseEntity.ok(response);
        }

        @PutMapping("/{pedidoId}/asignar")
        public ResponseEntity<PedidoAsignadoResponse> asignarOperarios(
                        @PathVariable Long pedidoId,
                        @Valid @RequestBody AsignarPedidoRequest request) {
                log.info("REST: Asignando operarios a pedido {}", pedidoId);

                Pedido pedido = asignarPedidoUseCase.ejecutar(
                                pedidoId,
                                request.getOperarioPickingId(),
                                request.getOperarioDespachoId());

                PedidoAsignadoResponse response = PedidoAsignadoResponse.builder()
                                .pedidoId(pedido.getPedidoId())
                                .numeroPedido(pedido.getNumeroPedido())
                                .clienteCc(pedido.getClienteCc())
                                .estado(pedido.getEstado().name())
                                .operarioPickingId(pedido.getOperarioPickingId())
                                .operarioDespachoId(pedido.getOperarioDespachoId())
                                .build();

                return ResponseEntity.ok(response);
        }
}
