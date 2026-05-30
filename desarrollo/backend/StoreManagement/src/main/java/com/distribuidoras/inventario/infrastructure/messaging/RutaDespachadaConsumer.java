package com.distribuidoras.inventario.infrastructure.messaging;

import com.distribuidoras.inventario.application.usecase.ComprometerInventarioUseCase;
import com.distribuidoras.inventario.application.usecase.ComprometerInventarioUseCase.ComprometerCommand;
import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RabbitMQ Consumer for receiving route dispatched message from Logistics Module.
 * Segundo mensaje: {idRuta, fechaDespacho}
 * Al recibirlo: compromete inventario y asigna fechaRecogida a los pedidos de esa ruta.
 */
@Component
public class RutaDespachadaConsumer {

    private static final Logger log = LoggerFactory.getLogger(RutaDespachadaConsumer.class);

    private final PedidoRepository pedidoRepository;
    private final ComprometerInventarioUseCase comprometerInventarioUseCase;

    public RutaDespachadaConsumer(PedidoRepository pedidoRepository,
            ComprometerInventarioUseCase comprometerInventarioUseCase) {
        this.pedidoRepository = pedidoRepository;
        this.comprometerInventarioUseCase = comprometerInventarioUseCase;
    }

    /**
     * Escucha mensajes de ruta despachada desde Módulo 2.
     * Message schema: {idRuta, fechaDespacho}
     */
    @Transactional
    @RabbitListener(queues = "inventario.ruta-despachada")
    public void recibirRutaDespachada(Map<String, Object> mensaje) {
        Object rutaIdObj = mensaje.get("idRuta");
        Object fechaDespachoObj = mensaje.get("fechaDespacho");

        if (rutaIdObj == null || fechaDespachoObj == null) {
            log.warn("Mensaje de ruta despachada incompleto: {}", mensaje);
            return;
        }

        Long rutaId = ((Number) rutaIdObj).longValue();
        LocalDateTime fechaRecogida = parseFecha(fechaDespachoObj);

        log.info("Recibida ruta despachada: ruta={}, fechaRecogida={}", rutaId, fechaRecogida);

        List<Pedido> pedidos = pedidoRepository.findByRutaId(rutaId);

        if (pedidos.isEmpty()) {
            log.warn("No se encontraron pedidos para la ruta {}. Reintentando...", rutaId);
            throw new IllegalStateException(
                "Pedidos no encontrados para ruta " + rutaId + ". Posible race condition - se reintentará.");
        }

        for (Pedido pedido : pedidos) {
            try {
                procesarPedido(pedido, rutaId, fechaRecogida);
            } catch (Exception e) {
                log.error("Error procesando pedido {} en ruta {}: {}",
                        pedido.getNumeroPedido(), rutaId, e.getMessage());
            }
        }
    }

    private void procesarPedido(Pedido pedido, Long rutaId, LocalDateTime fechaRecogida) {
        // Idempotencia: si ya está comprometido, skip
        if (pedido.getEstado() == EstadoPedido.COMPROMETIDO) {
            log.warn("Pedido {} ya está COMPROMETIDO. Saltando.", pedido.getNumeroPedido());
            return;
        }

        // Auto-recovery: si la ruta-asignada no se procesó, transicionar ahora
        if (pedido.getEstado() == EstadoPedido.ESPERANDO_RUTA) {
            if (!pedido.puedeTransicionarA(EstadoPedido.RUTA_ASIGNADA)) {
                log.warn("Pedido {} en {} no puede avanzar. Saltando.",
                        pedido.getNumeroPedido(), pedido.getEstado());
                return;
            }
            log.info("Auto-transicionando pedido {} de ESPERANDO_RUTA a RUTA_ASIGNADA",
                    pedido.getNumeroPedido());
            pedido.setEstado(EstadoPedido.RUTA_ASIGNADA);
            pedidoRepository.update(pedido);
        }

        // Solo procesar si está en RUTA_ASIGNADA
        if (pedido.getEstado() != EstadoPedido.RUTA_ASIGNADA) {
            log.warn("Pedido {} no está en RUTA_ASIGNADA (estado: {}). Saltando.",
                    pedido.getNumeroPedido(), pedido.getEstado());
            return;
        }

        comprometerInventarioUseCase.ejecutar(
                new ComprometerCommand(pedido.getPedidoId(), rutaId, fechaRecogida));

        log.info("Pedido {} comprometido para ruta {} con fechaRecogida={}",
                pedido.getNumeroPedido(), rutaId, fechaRecogida);
    }

    private LocalDateTime parseFecha(Object obj) {
        if (obj instanceof LocalDateTime dt) {
            return dt;
        }
        if (obj instanceof String str) {
            return LocalDateTime.parse(str);
        }
        return LocalDateTime.now();
    }
}
