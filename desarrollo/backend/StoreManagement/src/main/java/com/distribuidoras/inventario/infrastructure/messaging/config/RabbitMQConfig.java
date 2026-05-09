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
    public static final String INVENTARIO_PEDIDOS_EXCHANGE = "inventario.pedidos";
    public static final String LOGISTICA_EVENTOS_EXCHANGE = "logistica.eventos";
    public static final String INVENTARIO_ALERTAS_EXCHANGE = "inventario.alertas";

    // ===== Queue names =====
    public static final String RUTA_ASIGNADA_QUEUE = "inventario.ruta-asignada";
    public static final String RUTA_ASIGNADA_DLQ_QUEUE = "inventario.ruta-asignada.dlq";
    public static final String RUTA_SOLICITAR_QUEUE = "inventario.ruta-solicitar";
    public static final String ALERTA_INVENTARIO_QUEUE = "inventario.alertas-supervisor";
    public static final String ENTREGA_CONFIRMADA_QUEUE = "inventario.entrega-confirmada";
    public static final String ENTREGA_CONFIRMADA_DLQ_QUEUE = "inventario.entrega-confirmada.dlq";

    // ===== Routing keys =====
    public static final String RUTA_SOLICITAR_KEY = "ruta.solicitar";
    public static final String RUTA_ASIGNADA_KEY = "ruta.asignada";
    public static final String PEDIDO_CREADO_KEY = "pedido.creado";
    public static final String ALERTA_INVENTARIO_KEY = "alerta.inventario";
    public static final String ENTREGA_CONFIRMADA_KEY = "entrega.confirmada";

    // ===== Exchanges =====

    @Bean
    public TopicExchange inventarioPedidosExchange() {
        return new TopicExchange(INVENTARIO_PEDIDOS_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange logisticaEventosExchange() {
        return new TopicExchange(LOGISTICA_EVENTOS_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange inventarioAlertasExchange() {
        return new TopicExchange(INVENTARIO_ALERTAS_EXCHANGE, true, false);
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
    public Queue rutaSolicitarQueue() {
        return QueueBuilder.durable(RUTA_SOLICITAR_QUEUE).build();
    }

    @Bean
    public Queue alertaInventarioQueue() {
        return QueueBuilder.durable(ALERTA_INVENTARIO_QUEUE).build();
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
                .to(logisticaEventosExchange())
                .with(RUTA_ASIGNADA_KEY);
    }

    @Bean
    public Binding rutaSolicitarBinding() {
        return BindingBuilder.bind(rutaSolicitarQueue())
                .to(inventarioPedidosExchange())
                .with(RUTA_SOLICITAR_KEY);
    }

    @Bean
    public Binding alertaInventarioBinding() {
        return BindingBuilder.bind(alertaInventarioQueue())
                .to(inventarioAlertasExchange())
                .with(ALERTA_INVENTARIO_KEY);
    }

    @Bean
    public Binding entregaConfirmadaBinding() {
        return BindingBuilder.bind(entregaConfirmadaQueue())
                .to(logisticaEventosExchange())
                .with(ENTREGA_CONFIRMADA_KEY);
    }
}
