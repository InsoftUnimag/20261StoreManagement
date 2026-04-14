package com.distribuidoras.inventario.infrastructure.messaging;

import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * RabbitMQ Producer for sending route requests to Logistics Module.
 * Spec 13: Solicitar Ruta
 * 
 * FR-093: Enviar datos de dirección y peso a logística
 * SC-032: Pedidos se envían por cola
 */
@Component
public class SolicitudRutaProducer {

    private static final Logger log = LoggerFactory.getLogger(SolicitudRutaProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final PedidoRepository pedidoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;
    private final ProductoRepository productoRepository;

    @Value("${rabbitmq.exchange.solicitud-ruta:inventario.logistica}")
    private String exchange;

    @Value("${rabbitmq.routing-key.solicitud-ruta:solicitud.ruta}")
    private String routingKey;

    public SolicitudRutaProducer(RabbitTemplate rabbitTemplate,
                                  PedidoRepository pedidoRepository,
                                  ProductoPedidoRepository productoPedidoRepository,
                                  ProductoRepository productoRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Envía solicitud de ruta al Módulo 2 de Logística.
     * Envía datos clave: pedido_id, cliente_cc, peso_logistico, direccion_entrega
     */
    public void enviarSolicitudRuta(String pedidoId) {
        log.info("Enviando solicitud de ruta para pedido: {}", pedidoId);

        Pedido pedido = pedidoRepository.findById(UUID.fromString(pedidoId))
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + pedidoId));

        // Obtener líneas del pedido
        var lineas = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());

        // Calcular peso logístico total
        BigDecimal pesoTotal = lineas.stream()
                .map(linea -> productoRepository.findById(linea.getSkuId()))
                .filter(o -> o.isPresent())
                .map(o -> o.get().getPesoLogisticoKg())
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Construir mensaje
        Map<String, Object> mensaje = Map.of(
                "pedido_id", pedido.getPedidoId().toString(),
                "numero_pedido", pedido.getNumeroPedido(),
                "cliente_cc", pedido.getClienteCc(),
                "peso_logistico_kg", pesoTotal.doubleValue(),
                "total_lineas", lineas.size(),
                "fecha_solicitud", java.time.LocalDateTime.now().toString()
        );

        // Enviar a RabbitMQ (fire-and-forget)
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, mensaje);
            log.info("Solicitud de ruta enviada para pedido {}: peso={}kg, lineas={}", 
                    pedidoId, pesoTotal, lineas.size());
        } catch (Exception e) {
            log.error("Error enviando solicitud de ruta para pedido {}: {}", pedidoId, e.getMessage());
            // No lanzar excepción para no bloquear creación de pedido
        }
    }
}
