package com.distribuidoras.inventario.infrastructure.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for external module integration.
 * Spec 13: Solicitar Ruta
 * Spec 15: Ofrecer Datos Pedido
 */
@Configuration
public class RabbitMQConfig {

    // ===== Exchange names =====
    public static final String INVENTARIO_PEDIDOS_EXCHANGE = "pedidos.inventario";
    public static final String SOLICITUD_RUTA_EXCHANGE = "solicitud-ruta.request";
    // ===== Queue names =====
    public static final String RUTA_ASIGNADA_QUEUE = "inventario.ruta-asignada";
    public static final String RUTA_ASIGNADA_DLQ_QUEUE = "inventario.ruta-asignada.dlq";
    public static final String RUTA_DESPACHADA_QUEUE = "inventario.ruta-despachada";
    public static final String ENTREGA_CONFIRMADA_QUEUE = "inventario.entrega-confirmada";
    public static final String ENTREGA_CONFIRMADA_DLQ_QUEUE = "inventario.entrega-confirmada.dlq";

    // ===== Routing keys =====
    public static final String PEDIDO_CREADO_KEY = "pedido.creado";

    // ===== Exchanges =====

    @Bean
    public TopicExchange inventarioPedidosExchange() {
        return new TopicExchange(INVENTARIO_PEDIDOS_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange solicitudRutaExchange() {
        return new TopicExchange(SOLICITUD_RUTA_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange rutaAsignadaResponseExchange() {
        return new TopicExchange("ruta-asignada.response", true, false);
    }

    @Bean
    public TopicExchange rutaDespachadaEventExchange() {
        return new TopicExchange("ruta-despachada.event", true, false);
    }

    // ===== Queues for Consumer (Módulo 2 Logística) =====

    @Bean
    public Queue rutaAsignadaQueue() {
        return QueueBuilder.durable(RUTA_ASIGNADA_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", RUTA_ASIGNADA_DLQ_QUEUE)
                .build();
    }

    @Bean
    public Queue rutaAsignadaDlqQueue() {
        return QueueBuilder.durable(RUTA_ASIGNADA_DLQ_QUEUE).build();
    }

    @Bean
    public Queue rutaDespachadaQueue() {
        return QueueBuilder.durable(RUTA_DESPACHADA_QUEUE).build();
    }

    @Bean
    public Queue entregaConfirmadaQueue() {
        return QueueBuilder.durable(ENTREGA_CONFIRMADA_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ENTREGA_CONFIRMADA_DLQ_QUEUE)
                .build();
    }

    @Bean
    public Queue entregaConfirmadaDlqQueue() {
        return QueueBuilder.durable(ENTREGA_CONFIRMADA_DLQ_QUEUE).build();
    }

    // ===== Bindings =====

    @Bean
    public Binding rutaAsignadaBinding() {
        return BindingBuilder.bind(rutaAsignadaQueue())
                .to(rutaAsignadaResponseExchange())
                .with("#");
    }

    @Bean
    public Binding rutaDespachadaBinding() {
        return BindingBuilder.bind(rutaDespachadaQueue())
                .to(rutaDespachadaEventExchange())
                .with("#");
    }

    // Binding de entrega-confirmada se agregará cuando se defina el publicador
}
