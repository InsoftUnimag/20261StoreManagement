package com.distribuidoras.inventario.infrastructure.messaging;

import com.distribuidoras.inventario.domain.model.Cliente;
import com.distribuidoras.inventario.domain.repository.ClienteServicePort;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import com.distribuidoras.inventario.infrastructure.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.distribuidoras.inventario.infrastructure.persistence.entity.StockGlobalSkuJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.StockGlobalSkuJpaRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class PedidoCreadoProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoCreadoProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final PedidoRepository pedidoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;
    private final ClienteServicePort clienteServicePort;
    private final StockGlobalSkuJpaRepository stockGlobalSkuRepository;

    public PedidoCreadoProducer(RabbitTemplate rabbitTemplate,
            PedidoRepository pedidoRepository,
            ProductoPedidoRepository productoPedidoRepository,
            ClienteServicePort clienteServicePort,
            com.distribuidoras.inventario.infrastructure.persistence.repository.StockGlobalSkuJpaRepository stockGlobalSkuRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
        this.clienteServicePort = clienteServicePort;
        this.stockGlobalSkuRepository = stockGlobalSkuRepository;
    }

    @Async
    public void publicarPedidoCreado(Long pedidoId, String numeroPedido) {
        log.info("Publicando evento pedido.creado: {}", numeroPedido);

        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("idPedido", pedidoId.toString());

        try {
            var pedidoOpt = pedidoRepository.findById(pedidoId);
            if (pedidoOpt.isPresent()) {
                var pedido = pedidoOpt.get();

                var lineas = productoPedidoRepository.findByPedidoId(pedidoId);

                BigDecimal precioTotal = lineas.stream()
                        .map(linea -> {
                            // Obtener costo directamente del stock global
                            BigDecimal costoUnitario = stockGlobalSkuRepository
                                    .findById(Objects.requireNonNull(linea.getSkuId()))
                                    .map(StockGlobalSkuJpaEntity::getPrecio)
                                    .orElse(BigDecimal.ZERO);
                            if (costoUnitario == null)
                                costoUnitario = BigDecimal.ZERO;

                            return costoUnitario.multiply(BigDecimal.valueOf(linea.getCantidadSolicitada()));
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                mensaje.put("totalPedido", precioTotal.doubleValue());
                mensaje.put("idCliente", pedido.getClienteCc());

                String direccionEntrega = "No especificada";
                try {
                    Cliente cliente = clienteServicePort.findByCedula(pedido.getClienteCc()).orElse(null);
                    if (cliente != null && cliente.getDireccion() != null) {
                        direccionEntrega = cliente.getDireccion();
                    }
                } catch (Exception e) {
                    log.warn("No se pudo obtener dirección del cliente {}: {}", pedido.getClienteCc(), e.getMessage());
                }
                mensaje.put("direccion", direccionEntrega);
            }

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVENTARIO_PEDIDOS_EXCHANGE,
                    RabbitMQConfig.PEDIDO_CREADO_KEY,
                    mensaje);
            log.info("Evento publicado exitosamente: pedido.creado - {}", numeroPedido);
        } catch (Exception e) {
            log.error("Error publicando evento pedido.creado para {}: {}", numeroPedido, e.getMessage());
        }
    }
}
