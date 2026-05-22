package com.distribuidoras.inventario.infrastructure.messaging;

import com.distribuidoras.inventario.application.usecase.ComprometerInventarioUseCase;
import com.distribuidoras.inventario.application.usecase.ComprometerInventarioUseCase.ComprometerCommand;
import com.distribuidoras.inventario.application.usecase.ComprometerInventarioUseCase.ComprometerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * RabbitMQ Consumer for receiving route assignment from Logistics Module.
 * Spec 08/13: Cuando Módulo 2 asigna ruta, se compromete inventario.
 */
@Component
public class RutaAsignadaConsumer {

    private static final Logger log = LoggerFactory.getLogger(RutaAsignadaConsumer.class);

    private final ComprometerInventarioUseCase comprometerInventarioUseCase;

    public RutaAsignadaConsumer(ComprometerInventarioUseCase comprometerInventarioUseCase) {
        this.comprometerInventarioUseCase = comprometerInventarioUseCase;
    }

    /**
     * Escucha mensajes de ruta asignada desde Módulo 2.
     * Message schema: {pedido_id, ruta_id, fecha_despacho}
     */
    @RabbitListener(queues = "inventario.ruta-asignada")
    public void recibirRutaAsignada(Map<String, String> mensaje) {
        String pedidoId = mensaje.get("idPedido");
        String rutaId = mensaje.get("idRuta");

        log.info("Recibida ruta asignada: pedido={}, ruta={}", pedidoId, rutaId);

        try {
            ComprometerCommand command = new ComprometerCommand(
                    UUID.fromString(pedidoId),
                    Long.parseLong(rutaId)
            );

            ComprometerResult result = comprometerInventarioUseCase.ejecutar(command);

            if (result.exitoso()) {
                log.info("Inventario comprometido exitosamente para pedido {}", pedidoId);
            } else {
                log.warn("Compromiso con alertas para pedido {}: {}", pedidoId, result.alertas());
            }

        } catch (Exception e) {
            log.error("Error comprometiendo inventario para pedido {}: {}", pedidoId, e.getMessage());
            // NACK con requeue automático (máx 3 reintentos)
            throw new RuntimeException("Error procesando ruta asignada", e);
        }
    }
}
