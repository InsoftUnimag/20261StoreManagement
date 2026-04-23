package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.PedidoResumenDTO;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.domain.repository.LoteComprometidoRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.LineaResumenDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.LoteResumenDTO;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.model.ProductoPedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsultarPedidosParaPickingUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarPedidosParaPickingUseCase.class);

    private final PedidoRepository pedidoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;
    private final ProductoRepository productoRepository;
    private final LoteComprometidoRepository loteComprometidoRepository;
    private final ConsultarClienteUseCase consultarClienteUseCase;

    public ConsultarPedidosParaPickingUseCase(PedidoRepository pedidoRepository,
                                              ProductoPedidoRepository productoPedidoRepository,
                                              ProductoRepository productoRepository,
                                              LoteComprometidoRepository loteComprometidoRepository,
                                              ConsultarClienteUseCase consultarClienteUseCase) {
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
        this.productoRepository = productoRepository;
        this.loteComprometidoRepository = loteComprometidoRepository;
        this.consultarClienteUseCase = consultarClienteUseCase;
    }

    @Transactional(readOnly = true)
    public List<PedidoResumenDTO> ejecutar() {
        log.info("Consultando pedidos para picking (COMPROMETIDO)");
        List<Pedido> pedidos = pedidoRepository.findByEstadoOrderByFechaCompromisoAsc(EstadoPedido.COMPROMETIDO);
        
        return pedidos.stream().map(this::toPedidoResumenDTO).toList();
    }

    private PedidoResumenDTO toPedidoResumenDTO(Pedido pedido) {
        List<ProductoPedido> lineas = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());
        
        Integer totalUnidades = lineas.stream()
                .mapToInt(ProductoPedido::getCantidadSolicitada)
                .sum();

        List<LineaResumenDTO> lineasResumen = lineas.stream().map(this::toLineaResumenDTO).toList();

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
                .fechaCreacion(pedido.getFechaCreacion())
                .estado(pedido.getEstado().name())
                .totalLineas(lineas.size())
                .totalUnidades(totalUnidades)
                .lineas(lineasResumen)
                .build();
    }

    private LineaResumenDTO toLineaResumenDTO(ProductoPedido linea) {
        Producto producto = productoRepository.findById(linea.getSkuId()).orElse(null);
        String marca = producto != null ? producto.getMarca() : "Desconocido";
        String presentacion = producto != null ? producto.getPresentacion() : "Desconocido";

        List<LoteResumenDTO> lotes = loteComprometidoRepository.findByProductoPedidoId(linea.getProductoPedidoId()).stream()
                .map(lc -> new LoteResumenDTO(lc.getCodigoLote(), lc.getCantidadComprometida()))
                .toList();

        return new LineaResumenDTO(
                linea.getSkuId(),
                marca,
                presentacion,
                linea.getCantidadSolicitada(),
                linea.getCantidadConfirmada(),
                lotes
        );
    }
}
