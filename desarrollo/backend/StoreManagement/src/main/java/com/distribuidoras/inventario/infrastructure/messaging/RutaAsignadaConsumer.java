package com.distribuidoras.inventario.infrastructure.messaging;

import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * RabbitMQ Consumer for receiving route assignment from Logistics Module.
 * Solo asigna la ruta al pedido (rutaId), sin comprometer inventario.
 * El compromiso se hará cuando llegue el segundo mensaje con la fecha de recogida.
 */
@Component
public class RutaAsignadaConsumer {

    private static final Logger log = LoggerFactory.getLogger(RutaAsignadaConsumer.class);

    private final PedidoRepository pedidoRepository;

    public RutaAsignadaConsumer(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Escucha mensajes de ruta asignada desde Módulo 2.
     * Message schema: {idPedido, idRuta}
     */
    @Transactional
    @RabbitListener(queues = "inventario.ruta-asignada")
    public void recibirRutaAsignada(Map<String, Object> mensaje) {
        Object pedidoIdObj = mensaje.get("idPedido");
        Object rutaIdObj = mensaje.get("idRuta");

        if (pedidoIdObj == null || rutaIdObj == null) {
            log.warn("Mensaje de ruta asignada incompleto: {}", mensaje);
            return;
        }

        Long pedidoId = ((Number) pedidoIdObj).longValue();
        Long rutaId = ((Number) rutaIdObj).longValue();

        log.info("Recibida ruta asignada: pedido={}, ruta={}", pedidoId, rutaId);

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + pedidoId));

        // Idempotencia: si ya tiene la misma ruta asignada, ignorar
        if (pedido.getEstado() == EstadoPedido.RUTA_ASIGNADA && rutaId.equals(pedido.getRutaId())) {
            log.warn("Pedido {} ya tiene la ruta {} asignada. Ignorando duplicado.",
                    pedido.getNumeroPedido(), rutaId);
            return;
        }

        // Validar transición de estado
        if (!pedido.puedeTransicionarA(EstadoPedido.RUTA_ASIGNADA)) {
            log.warn("Pedido {} en estado {} no puede transicionar a RUTA_ASIGNADA. Ignorando.",
                    pedido.getNumeroPedido(), pedido.getEstado());
            return;
        }

        pedido.setRutaId(rutaId);
        pedido.setEstado(EstadoPedido.RUTA_ASIGNADA);
        pedidoRepository.update(pedido);

        log.info("Ruta {} asignada al pedido {} (estado: {})", rutaId, pedido.getNumeroPedido(), pedido.getEstado());
    }
}
