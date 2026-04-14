package com.distribuidoras.inventario.infrastructure.messaging;

import com.distribuidoras.inventario.infrastructure.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * RabbitMQ Producer for notifying Module 3 (Finanzas) when a pedido is created.
 * Spec 15: Ofrecer Datos Pedido
 * 
 * FR-048: Enviar datos a logística de finanzas luego de crear el pedido
 * FR-049: Esperar verificación del buen recibido
 * FR-051: Reenviar datos si ocurre algún problema
 * 
 * Uses lightweight message pattern: Finanzas consultará detalles completos por REST.
 */
@Component
public class PedidoCreadoProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoCreadoProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public PedidoCreadoProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes a "pedido.creado" event to the financiero module.
     * FR-048: Must send after pedido creation.
     * 
     * @param pedidoId UUID del pedido creado
     * @param numeroPedido Número de pedido (ej: PED-20260403-001)
     */
    public void publicarPedidoCreado(UUID pedidoId, String numeroPedido) {
        log.info("Publicando evento pedido.creado: {}", numeroPedido);

        // Lightweight message - Finanzas consulta detalles por REST
        Map<String, Object> mensaje = Map.of(
                "pedido_id", pedidoId.toString(),
                "numero_pedido", numeroPedido,
                "evento", "PEDIDO_CREADO",
                "timestamp", LocalDateTime.now().toString()
        );

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVENTARIO_PEDIDOS_EXCHANGE,
                    RabbitMQConfig.PEDIDO_CREADO_KEY,
                    mensaje
            );
            log.info("Evento publicado exitosamente: pedido.creado - {}", numeroPedido);
        } catch (Exception e) {
            log.error("Error publicando evento pedido.creado para {}: {}", numeroPedido, e.getMessage());
            // FR-051: No fallar creación de pedido, solo log error
            // Reintento manual podría implementarse aquí
        }
    }

    /**
     * Publishes a "ruta.solicitar" event to Module 2 (Logística).
     * Spec 13: Solicitar Ruta
     * 
     * @param pedidoId UUID del pedido
     * @param numeroPedido Número de pedido
     */
    public void solicitarRuta(UUID pedidoId, String numeroPedido) {
        log.info("Solicitando ruta para pedido: {}", numeroPedido);

        Map<String, Object> mensaje = Map.of(
                "pedido_id", pedidoId.toString(),
                "numero_pedido", numeroPedido,
                "estado", "ESPERANDO_RUTA",
                "timestamp", LocalDateTime.now().toString()
        );

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVENTARIO_PEDIDOS_EXCHANGE,
                    RabbitMQConfig.RUTA_SOLICITAR_KEY,
                    mensaje
            );
            log.info("Ruta solicitada exitosamente para: {}", numeroPedido);
        } catch (Exception e) {
            log.error("Error solicitando ruta para {}: {}", numeroPedido, e.getMessage());
        }
    }
}
