package com.distribuidoras.inventario.infrastructure.messaging;

import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.domain.repository.ClienteServicePort;
import com.distribuidoras.inventario.domain.model.Cliente;
import com.distribuidoras.inventario.infrastructure.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.distribuidoras.inventario.infrastructure.persistence.repository.StockGlobalSkuJpaRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private final ClienteServicePort clienteServicePort;

    @Value("${rabbitmq.exchange.solicitud-ruta:inventario.logistica}")
    private String exchange;

    @Value("${rabbitmq.routing-key.solicitud-ruta:solicitud.ruta}")
    private String routingKey;

    private final StockGlobalSkuJpaRepository stockGlobalSkuRepository;

    public SolicitudRutaProducer(RabbitTemplate rabbitTemplate,
            PedidoRepository pedidoRepository,
            ProductoPedidoRepository productoPedidoRepository,
            ProductoRepository productoRepository,
            ClienteServicePort clienteServicePort,
            StockGlobalSkuJpaRepository stockGlobalSkuRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
        this.productoRepository = productoRepository;
        this.clienteServicePort = clienteServicePort;
        this.stockGlobalSkuRepository = stockGlobalSkuRepository;
    }

    /**
     * Envía solicitud de ruta al Módulo 2 de Logística.
     * Envía datos clave: pedido_id, cliente_cc, peso_logistico, direccion_entrega
     */
    @Async
    public void enviarSolicitudRuta(String pedidoId) {
        log.info("Enviando solicitud de ruta para pedido: {}", pedidoId);

        Pedido pedido = pedidoRepository.findById(UUID.fromString(pedidoId))
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + pedidoId));

        // Obtener líneas del pedido
        var lineas = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());

        // GAP-04: Calcular peso logístico total multiplicando por cantidad solicitada
        BigDecimal pesoTotal = lineas.stream()
                .map(linea -> {
                    Optional<Producto> prod = productoRepository.findById(Objects.requireNonNull(linea.getSkuId()));
                    BigDecimal peso = prod.map(Producto::getPesoLogisticoKg)
                            .filter(Objects::nonNull)
                            .orElse(BigDecimal.ZERO);
                    return peso.multiply(BigDecimal.valueOf(linea.getCantidadSolicitada()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular costo total del pedido (total_pedido) usando el precio en
        // StockGlobalSku
        BigDecimal precioTotal = lineas.stream()
                .map(linea -> {
                    BigDecimal costoUnitario = stockGlobalSkuRepository.findById(Objects.requireNonNull(linea.getSkuId()))
                            .map(com.distribuidoras.inventario.infrastructure.persistence.entity.StockGlobalSkuJpaEntity::getPrecio)
                            .orElse(BigDecimal.ZERO);
                    if (costoUnitario == null)
                        costoUnitario = BigDecimal.ZERO;

                    return costoUnitario.multiply(BigDecimal.valueOf(linea.getCantidadSolicitada()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // GAP-05: Obtener dirección de entrega del cliente
        String direccionEntrega = "No especificada";
        try {
            Cliente cliente = clienteServicePort.findByCedula(pedido.getClienteCc()).orElse(null);
            if (cliente != null && cliente.getDireccion() != null) {
                direccionEntrega = cliente.getDireccion();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener dirección del cliente {}: {}", pedido.getClienteCc(), e.getMessage());
        }

        // Construir mensaje explícitamente con lo que exige Módulo 2
        // Spec 13: id_pedido, id_cliente, total_pedido, direccion, peso_total
        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("id_pedido", pedido.getPedidoId().toString());
        mensaje.put("id_cliente", pedido.getClienteCc());
        mensaje.put("total_pedido", precioTotal.doubleValue());
        mensaje.put("direccion", direccionEntrega);
        mensaje.put("peso_logistico_kg", pesoTotal.doubleValue());
        
        // Enviar a RabbitMQ (fire-and-forget)
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.INVENTARIO_PEDIDOS_EXCHANGE,
                    RabbitMQConfig.RUTA_SOLICITAR_KEY,
                    mensaje);
            log.info("Solicitud de ruta enviada para pedido {}: peso={}kg, lineas={}",
                    pedidoId, pesoTotal, lineas.size());
        } catch (Exception e) {
            log.error("Error enviando solicitud de ruta para pedido {}: {}", pedidoId, e.getMessage());
            // No lanzar excepción para no bloquear creación de pedido
        }
    }
}
