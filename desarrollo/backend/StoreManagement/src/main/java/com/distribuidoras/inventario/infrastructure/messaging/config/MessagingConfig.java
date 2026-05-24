package com.distribuidoras.inventario.infrastructure.messaging.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración adicional de mensajería para que los mensajes se envíen/reciban en formato JSON.
 *
 * Al declarar un bean {@link MessageConverter} de tipo {@link Jackson2JsonMessageConverter}
 * Spring Boot lo detecta automáticamente y lo asocia al {@link org.springframework.amqp.rabbit.core.RabbitTemplate}
 * que ya está en el contexto. De esta forma los {@code Map<String,Object>} que se envían
 * desde {@code SolicitudRutaProducer} se serializan como JSON y el consumidor
 * {@code RouteRequestListener} puede deserializarlos en {@code RouteRequestEvent} sin errores.
 */
@Configuration
public class MessagingConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
