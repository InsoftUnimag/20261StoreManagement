package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListarPedidosAsignadosUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListarPedidosAsignadosUseCase.class);

    private final PedidoRepository pedidoRepository;

    public ListarPedidosAsignadosUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public List<Pedido> ejecutar(UUID operarioId, String tipo) {
        log.info("Listando pedidos asignados a operario {} para tipo {}", operarioId, tipo);

        if ("picking".equalsIgnoreCase(tipo)) {
            return pedidoRepository.findByOperarioPickingId(operarioId);
        } else if ("despacho".equalsIgnoreCase(tipo)) {
            return pedidoRepository.findByOperarioDespachoId(operarioId);
        }

        return List.of();
    }
}