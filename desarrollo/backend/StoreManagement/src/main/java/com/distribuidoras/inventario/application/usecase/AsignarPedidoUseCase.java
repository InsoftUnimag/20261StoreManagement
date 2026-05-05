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
            pedido.setOperarioPickingId(operarioPickingId);
        }
        if (operarioDespachoId != null) {
            pedido.setOperarioDespachoId(operarioDespachoId);
        }

        if (pedido.getOperarioPickingId() != null || pedido.getOperarioDespachoId() != null) {
            if (pedido.getEstado() == EstadoPedido.ESPERANDO_RUTA) {
                pedido.setEstado(EstadoPedido.COMPROMETIDO);
                log.info("Pedido {} actualizado a estado COMPROMETIDO", pedidoId);
            }
        }

        pedidoRepository.update(pedido);
        return pedido;
    }
}