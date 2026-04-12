package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.ProductoPedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.PaginacionDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.PedidoResumenDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.PedidosListResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Use Case: Consultar Lista de Pedidos con filtros.
 * Spec 10: Listar Pedidos Comprometidos
 * 
 * FR-063: Mostrar pedidos comprometidos
 * FR-064: Mostrar número, cliente, fecha, productos, lotes
 * FR-065: Ordenar por fecha creación ASC (FIFO)
 * FR-066: Filtrar por cliente, fecha
 */
@Service
public class ConsultarListaPedidosUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarListaPedidosUseCase.class);

    private final PedidoRepository pedidoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;

    public ConsultarListaPedidosUseCase(PedidoRepository pedidoRepository,
                                         ProductoPedidoRepository productoPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
    }

    /**
     * Filtros para consulta de pedidos.
     */
    public record FiltrosDTO(
            EstadoPedido estado,
            String clienteCc,
            String numeroPedido,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Integer page,
            Integer size
    ) {
        public FiltrosDTO {
            if (page == null) page = 0;
            if (size == null) size = 20;
            size = Math.min(size, 100);
        }
    }

    /**
     * Ejecuta la consulta con filtros y paginación.
     */
    @Transactional(readOnly = true)
    public PedidosListResponseDTO ejecutar(FiltrosDTO filtros) {
        log.info("Consultando lista de pedidos con filtros: estado={}, cliente={}, numero={}",
                filtros.estado(), filtros.clienteCc(), filtros.numeroPedido());

        PageRequest pageable = PageRequest.of(filtros.page(), filtros.size());

        Page<Pedido> page = pedidoRepository.findByFilters(
                filtros.estado(),
                filtros.clienteCc(),
                filtros.numeroPedido(),
                filtros.fechaDesde(),
                filtros.fechaHasta(),
                pageable
        );

        // Transformar a DTOs
        List<PedidoResumenDTO> pedidos = page.getContent().stream()
                .map(this::toPedidoResumenDTO)
                .toList();

        PaginacionDTO paginacion = PaginacionDTO.builder()
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .build();

        log.info("Lista de pedidos consultada: {} pedidos de {} totales", 
                pedidos.size(), page.getTotalElements());

        return PedidosListResponseDTO.builder()
                .pedidos(pedidos)
                .paginacion(paginacion)
                .build();
    }

    /**
     * Obtiene pedidos comprometidos ordenados por fecha ASC (FIFO).
     * Spec 10 - FR-065
     */
    public List<PedidoResumenDTO> listarPedidosComprometidos() {
        log.info("Listando pedidos comprometidos (FIFO)");
        
        return pedidoRepository.findByEstadoOrderByFechaCreacionAsc(EstadoPedido.COMPROMETIDO).stream()
                .map(this::toPedidoResumenDTO)
                .toList();
    }

    /**
     * Transforma Pedido a PedidoResumenDTO.
     */
    private PedidoResumenDTO toPedidoResumenDTO(Pedido pedido) {
        List<ProductoPedido> lineas = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());
        
        Integer totalUnidades = lineas.stream()
                .mapToInt(ProductoPedido::getCantidadSolicitada)
                .sum();

        return PedidoResumenDTO.builder()
                .pedidoId(pedido.getPedidoId())
                .numeroPedido(pedido.getNumeroPedido())
                .clienteCc(pedido.getClienteCc())
                .clienteNombre("Cliente")  // Se podría consultar módulo usuarios
                .fechaCreacion(pedido.getFechaCreacion())
                .estado(pedido.getEstado().name())
                .totalLineas(lineas.size())
                .totalUnidades(totalUnidades)
                .build();
    }
}
