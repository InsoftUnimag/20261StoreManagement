package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.ProductoPedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import com.distribuidoras.inventario.domain.repository.RegistroDespachoRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.PedidoAsignadoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarPedidosAsignadosUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListarPedidosAsignadosUseCase.class);

    private final PedidoRepository pedidoRepository;
    private final RegistroDespachoRepository registroDespachoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;

    public ListarPedidosAsignadosUseCase(PedidoRepository pedidoRepository,
                                          ProductoPedidoRepository productoPedidoRepository,
                                          RegistroDespachoRepository registroDespachoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
        this.registroDespachoRepository = registroDespachoRepository;
    }

    public List<PedidoAsignadoDTO> ejecutar(Long operarioId, String tipo) {
        log.info("Listando pedidos asignados a operario {} para tipo {}", operarioId, tipo);

        List<Pedido> pedidos;
        if ("picking".equalsIgnoreCase(tipo)) {
            pedidos = pedidoRepository.findByOperarioPickingId(operarioId);
        } else if ("despacho".equalsIgnoreCase(tipo)) {
            pedidos = pedidoRepository.findByOperarioDespachoId(operarioId);
        } else {
            return List.of();
        }

        return pedidos.stream()
                .map(this::toDTO)
                .toList();
    }

    private PedidoAsignadoDTO toDTO(Pedido p) {
        String clienteNombre = p.getClienteNombre() != null ? p.getClienteNombre() : "Cliente no disponible";

        int totalUnidades = productoPedidoRepository.findByPedidoId(p.getPedidoId()).stream()
                .mapToInt(ProductoPedido::getCantidadSolicitada)
                .sum();

        java.time.LocalDateTime fechaSalida = null;
        if ("DESPACHADO".equals(p.getEstado().name()) || "ENTREGADO".equals(p.getEstado().name())) {
            fechaSalida = registroDespachoRepository.findByPedidoId(p.getPedidoId())
                    .map(r -> r.getFechaDespacho())
                    .orElse(null);
        }

        return new PedidoAsignadoDTO(
                p.getPedidoId(),
                p.getNumeroPedido(),
                p.getClienteCc(),
                clienteNombre,
                p.getFechaCreacion(),
                p.getFechaCompromiso(),
                fechaSalida,
                p.getEstado().name(),
                totalUnidades
        );
    }
}