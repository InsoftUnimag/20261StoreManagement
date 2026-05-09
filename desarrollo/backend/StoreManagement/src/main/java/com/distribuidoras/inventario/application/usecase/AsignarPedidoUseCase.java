package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.PedidoNotFoundException;
import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AsignarPedidoUseCase {

    private static final Logger log = LoggerFactory.getLogger(AsignarPedidoUseCase.class);

    private final PedidoRepository pedidoRepository;

    public AsignarPedidoUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido ejecutar(UUID pedidoId, UUID operarioPickingId, UUID operarioDespachoId) {
        log.info("Asignando pedido {} - picking: {}, despacho: {}", pedidoId, operarioPickingId, operarioDespachoId);

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new PedidoNotFoundException(pedidoId.toString()));

        if (operarioPickingId != null) {
            if (pedido.getEstado() != EstadoPedido.COMPROMETIDO) {
                throw new IllegalStateException(
                        "Solo se pueden asignar pedidos a picking en estado COMPROMETIDO. Estado actual: " + pedido.getEstado()
                );
            }
            if (pedido.getOperarioPickingId() != null) {
                throw new IllegalStateException(
                        "El pedido ya tiene operario de picking asignado: " + pedido.getOperarioPickingId()
                );
            }
            pedido.setOperarioPickingId(operarioPickingId);
        }

        if (operarioDespachoId != null) {
            if (pedido.getEstado() != EstadoPedido.PICKUP) {
                throw new IllegalStateException(
                        "Solo se puede asignar operario de despacho desde estado PICKUP. Estado actual: " + pedido.getEstado()
                );
            }
            if (pedido.getOperarioDespachoId() != null) {
                throw new IllegalStateException(
                        "El pedido ya tiene operario de despacho asignado: " + pedido.getOperarioDespachoId()
                );
            }
            pedido.setOperarioDespachoId(operarioDespachoId);
        }

        log.info("Pedido {} asignado exitosamente (Picking: {}, Despacho: {})", 
                pedidoId, pedido.getOperarioPickingId(), pedido.getOperarioDespachoId());

        pedidoRepository.update(pedido);
        return pedido;
    }
}