package com.distribuidoras.inventario.infrastructure.messaging;

import com.distribuidoras.inventario.domain.model.Cliente;
import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.repository.ClienteServicePort;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.infrastructure.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class PedidoCreadoProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoCreadoProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final PedidoRepository pedidoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;
    private final LoteRepository loteRepository;
    private final ClienteServicePort clienteServicePort;

    public PedidoCreadoProducer(RabbitTemplate rabbitTemplate,
                                PedidoRepository pedidoRepository,
                                ProductoPedidoRepository productoPedidoRepository,
                                LoteRepository loteRepository,
                                ClienteServicePort clienteServicePort) {
        this.rabbitTemplate = rabbitTemplate;
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
        this.loteRepository = loteRepository;
        this.clienteServicePort = clienteServicePort;
    }

    @Async
    public void publicarPedidoCreado(UUID pedidoId, String numeroPedido) {
        log.info("Publicando evento pedido.creado: {}", numeroPedido);

        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("pedido_id", pedidoId.toString());
        mensaje.put("numero_pedido", numeroPedido);
        mensaje.put("evento", "PEDIDO_CREADO");
        mensaje.put("timestamp", LocalDateTime.now().toString());

        try {
            var pedidoOpt = pedidoRepository.findById(pedidoId);
            if (pedidoOpt.isPresent()) {
                var pedido = pedidoOpt.get();

                var lineas = productoPedidoRepository.findByPedidoId(pedidoId);
                
                BigDecimal precioTotal = lineas.stream()
                        .map(linea -> {
                            // Obtener costo del lote más reciente para este SKU
                            BigDecimal costoUnitario = loteRepository.findBySkuIdOrderByFechaVencimientoAsc(linea.getSkuId())
                                .stream()
                                .findFirst()
                                .map(Lote::getCostoUnitarioProducto)
                                .orElse(BigDecimal.ZERO);
                            
                            return costoUnitario.multiply(BigDecimal.valueOf(linea.getCantidadSolicitada()));
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                mensaje.put("precio_total", precioTotal.doubleValue());

                String direccionEntrega = "No especificada";
                try {
                    Cliente cliente = clienteServicePort.findByCedula(pedido.getClienteCc()).orElse(null);
                    if (cliente != null && cliente.getDireccion() != null) {
                        direccionEntrega = cliente.getDireccion();
                    }
                } catch (Exception e) {
                    log.warn("No se pudo obtener dirección del cliente {}: {}", pedido.getClienteCc(), e.getMessage());
                }
                mensaje.put("direccion_entrega", direccionEntrega);
            }

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVENTARIO_PEDIDOS_EXCHANGE,
                    RabbitMQConfig.PEDIDO_CREADO_KEY,
                    mensaje
            );
            log.info("Evento publicado exitosamente: pedido.creado - {}", numeroPedido);
        } catch (Exception e) {
            log.error("Error publicando evento pedido.creado para {}: {}", numeroPedido, e.getMessage());
        }
    }
}
