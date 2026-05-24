package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.PedidoNotFoundException;
import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class IniciarPickingUseCase {

    private static final Logger log = LoggerFactory.getLogger(IniciarPickingUseCase.class);

    private final PedidoRepository pedidoRepository;

    public IniciarPickingUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public Pedido ejecutar(Long pedidoId, Long operarioId) {
        log.info("Iniciando picking para pedido {} por operario {}", pedidoId, operarioId);

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new PedidoNotFoundException(pedidoId.toString()));

        if (pedido.getEstado() != EstadoPedido.COMPROMETIDO) {
            throw new IllegalStateException(
                    "Solo se puede iniciar picking para pedidos en estado COMPROMETIDO. Estado actual: "
                            + pedido.getEstado());
        }

        // Verificar que el operario que inicia el picking es el asignado
        if (pedido.getOperarioPickingId() == null || !pedido.getOperarioPickingId().equals(operarioId)) {
            throw new IllegalStateException(
                    "El operario no tiene asignado este pedido o no hay operario asignado.");
        }

        pedido.setEstado(EstadoPedido.EN_PICKING);
        pedidoRepository.update(pedido);

        log.info("Picking iniciado exitosamente. Pedido {} ahora en estado EN_PICKING", pedido.getNumeroPedido());

        return pedido;
    }
}
