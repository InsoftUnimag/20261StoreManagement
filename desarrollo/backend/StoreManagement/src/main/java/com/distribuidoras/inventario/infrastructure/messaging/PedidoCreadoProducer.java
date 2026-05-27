package com.distribuidoras.inventario.infrastructure.messaging;

import com.distribuidoras.inventario.domain.model.Cliente;
import com.distribuidoras.inventario.domain.repository.ClienteServicePort;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.infrastructure.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class PedidoCreadoProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoCreadoProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final PedidoRepository pedidoRepository;
    private final ClienteServicePort clienteServicePort;

    public PedidoCreadoProducer(RabbitTemplate rabbitTemplate,
            PedidoRepository pedidoRepository,
            ClienteServicePort clienteServicePort) {
        this.rabbitTemplate = rabbitTemplate;
        this.pedidoRepository = pedidoRepository;
        this.clienteServicePort = clienteServicePort;
    }

    @Async
    public void publicarPedidoCreado(Long pedidoId, String numeroPedido) {
        log.info("Publicando evento pedido.creado: {}", numeroPedido);

        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("idPedido", pedidoId);

        try {
            var pedidoOpt = pedidoRepository.findById(pedidoId);
            if (pedidoOpt.isPresent()) {
                var pedido = pedidoOpt.get();

                BigDecimal costoTotal = pedido.getCostoTotal();
                if (costoTotal == null) costoTotal = BigDecimal.ZERO;

                mensaje.put("totalPedido", costoTotal.longValue());
                mensaje.put("idCliente", Long.parseLong(pedido.getClienteCc().trim()));

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
