package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.PedidoResumenDTO;
import com.distribuidoras.inventario.domain.repository.RegistroPickingRepository;
import com.distribuidoras.inventario.domain.model.RegistroPicking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case: Consultar Pedidos para Despacho (Spec 13).
 * FR-070: Mostrar pedidos en estado EN_PICKING.
 */
@Service
public class ConsultarPedidosParaDespachoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarPedidosParaDespachoUseCase.class);

    private final PedidoRepository pedidoRepository;
    private final RegistroPickingRepository registroPickingRepository;
    private final ConsultarClienteUseCase consultarClienteUseCase;

    public ConsultarPedidosParaDespachoUseCase(PedidoRepository pedidoRepository,
                                              RegistroPickingRepository registroPickingRepository,
                                              ConsultarClienteUseCase consultarClienteUseCase) {
        this.pedidoRepository = pedidoRepository;
        this.registroPickingRepository = registroPickingRepository;
        this.consultarClienteUseCase = consultarClienteUseCase;
    }

    @Transactional(readOnly = true)
    public List<PedidoResumenDTO> ejecutar() {
        log.info("Consultando pedidos para despacho (PICKUP)");
        List<Pedido> pedidos = pedidoRepository.findByEstadoWithPickingOrderByFechaPickingAsc(EstadoPedido.PICKUP);
        
        return pedidos.stream().map(this::toPedidoResumenDTO).toList();
    }

    private PedidoResumenDTO toPedidoResumenDTO(Pedido pedido) {
        RegistroPicking picking = registroPickingRepository.findByPedidoId(pedido.getPedidoId()).orElse(null);

        // GAP-06: Consultar nombre real del cliente desde Módulo Usuarios
        String clienteNombre = "Cliente";
        String direccionEntrega = null;
        try {
            var cliente = consultarClienteUseCase.ejecutar(pedido.getClienteCc());
            clienteNombre = cliente.getNombre();
            direccionEntrega = cliente.getDireccion();
        } catch (Exception e) {
            log.warn("No se pudo obtener datos del cliente {}: {}", pedido.getClienteCc(), e.getMessage());
        }

        return PedidoResumenDTO.builder()
                .pedidoId(pedido.getPedidoId())
                .numeroPedido(pedido.getNumeroPedido())
                .clienteCc(pedido.getClienteCc())
                .clienteNombre(clienteNombre)
                .direccionEntrega(direccionEntrega)
                .fechaCreacion(picking != null ? picking.getFechaPicking() : pedido.getFechaCreacion())
                .estado(pedido.getEstado().name())
                .totalLineas(0)
                .totalUnidades(0)
                .build();
    }
}