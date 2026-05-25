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

        try {
            List<Pedido> pedidos = pedidoRepository.findByRutaId(rutaId);

            if (pedidos.isEmpty()) {
                log.warn("No se encontraron pedidos para la ruta {}", rutaId);
                return;
            }

            for (Pedido pedido : pedidos) {
                if (pedido.getEstado() != EstadoPedido.ESPERANDO_RUTA) {
                    log.warn("Pedido {} no está en ESPERANDO_RUTA (estado: {}). Saltando.",
                            pedido.getNumeroPedido(), pedido.getEstado());
                    continue;
                }

                comprometerInventarioUseCase.ejecutar(
                        new ComprometerCommand(pedido.getPedidoId(), rutaId, fechaRecogida));

                log.info("Pedido {} comprometido con fechaRecogida={}", pedido.getNumeroPedido(), fechaRecogida);
            }

        } catch (Exception e) {
            log.error("Error procesando ruta despachada {}: {}", rutaId, e.getMessage());
            throw new RuntimeException("Error procesando ruta despachada", e);
        }
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
