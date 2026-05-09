package com.distribuidoras.inventario.infrastructure.messaging;

import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class EntregaConfirmadaConsumer {

    private static final Logger log = LoggerFactory.getLogger(EntregaConfirmadaConsumer.class);

    private final PedidoRepository pedidoRepository;

    public EntregaConfirmadaConsumer(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @RabbitListener(queues = "inventario.entrega-confirmada")
    public void recibirEntregaConfirmada(Map<String, String> mensaje) {
        String pedidoId = mensaje.get("pedido_id");
        String fechaEntregaStr = mensaje.get("fecha_entrega");
        String observaciones = mensaje.getOrDefault("observaciones", "");

        log.info("Recibida confirmación de entrega: pedido={}, fecha={}", pedidoId, fechaEntregaStr);

        try {
            UUID pedidoUuid = UUID.fromString(pedidoId);
            Pedido pedido = pedidoRepository.findById(pedidoUuid)
                    .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + pedidoId));

            if (pedido.getEstado() != EstadoPedido.DESPACHADO) {
                log.warn("Pedido {} no está en estado DESPACHADO, estado actual: {}. Ignorando confirmación de entrega.",
                        pedido.getNumeroPedido(), pedido.getEstado());
                return;
            }

            LocalDateTime fechaEntrega = fechaEntregaStr != null
                    ? LocalDateTime.parse(fechaEntregaStr)
                    : LocalDateTime.now();

            pedido.setEstado(EstadoPedido.ENTREGADO);
            pedido.setFechaEntrega(fechaEntrega);
            if (observaciones != null && !observaciones.isBlank()) {
                pedido.setObservaciones(pedido.getObservaciones() != null
                        ? pedido.getObservaciones() + " | Entrega: " + observaciones
                        : "Entrega: " + observaciones);
            }
            pedidoRepository.update(pedido);

            log.info("Pedido {} marcado como ENTREGADO. Fecha: {}", pedido.getNumeroPedido(), fechaEntrega);

        } catch (Exception e) {
            log.error("Error procesando confirmación de entrega para pedido {}: {}", pedidoId, e.getMessage());
            throw new RuntimeException("Error procesando entrega confirmada", e);
        }
    }
}