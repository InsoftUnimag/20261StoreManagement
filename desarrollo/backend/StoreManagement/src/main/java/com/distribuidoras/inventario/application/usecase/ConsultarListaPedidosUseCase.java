package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.ProductoPedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.domain.repository.LoteComprometidoRepository;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.infrastructure.web.dto.PaginacionDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.PedidoResumenDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.PedidosListResponseDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.LineaResumenDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.LoteResumenDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;



/**
 * Use Case: Consultar Lista de Pedidos con filtros.
 * Spec 10: Listar Pedidos Comprometidos
 */
@Service
public class ConsultarListaPedidosUseCase {

        private final PedidoRepository pedidoRepository;
        private final ProductoPedidoRepository productoPedidoRepository;
        private final ProductoRepository productoRepository;
        private final LoteComprometidoRepository loteComprometidoRepository;

        public ConsultarListaPedidosUseCase(PedidoRepository pedidoRepository,
                        ProductoPedidoRepository productoPedidoRepository,
                        ProductoRepository productoRepository,
                        LoteComprometidoRepository loteComprometidoRepository) {
                this.pedidoRepository = pedidoRepository;
                this.productoPedidoRepository = productoPedidoRepository;
                this.productoRepository = productoRepository;
                this.loteComprometidoRepository = loteComprometidoRepository;
        }

        public record FiltrosDTO(
                        EstadoPedido estado,
                        String clienteCc,
                        String numeroPedido,
                        LocalDate fechaDesde,
                        LocalDate fechaHasta,
                        Integer page,
                        Integer size) {
                public FiltrosDTO {
                        if (page == null)
                                page = 0;
                        if (size == null)
                                size = 20;
                        size = Math.min(size, 100);
                }
        }

        @Transactional(readOnly = true)
        public PedidosListResponseDTO ejecutar(FiltrosDTO filtros) {
                PageRequest pageable = PageRequest.of(filtros.page(), filtros.size(), Sort.by("fechaCreacion").descending());

                Page<Pedido> page = pedidoRepository.findByFilters(
                                filtros.estado(),
                                filtros.clienteCc(),
                                filtros.numeroPedido(),
                                filtros.fechaDesde(),
                                filtros.fechaHasta(),
                                pageable);

                List<PedidoResumenDTO> pedidos = page.getContent().stream()
                                .map(this::toPedidoResumenDTO)
                                .collect(Collectors.toList());

                PaginacionDTO paginacion = PaginacionDTO.builder()
                                .totalElements(page.getTotalElements())
                                .totalPages(page.getTotalPages())
                                .currentPage(page.getNumber())
                                .pageSize(page.getSize())
                                .build();

                return PedidosListResponseDTO.builder()
                                .pedidos(pedidos)
                                .paginacion(paginacion)
                                .build();
        }

        public List<PedidoResumenDTO> listarPedidosComprometidos() {
                return pedidoRepository.findByEstadoOrderByFechaCreacionAsc(EstadoPedido.COMPROMETIDO).stream()
                                .map(this::toPedidoResumenDTO)
                                .collect(Collectors.toList());
        }

        private PedidoResumenDTO toPedidoResumenDTO(Pedido pedido) {
                List<ProductoPedido> lineas = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());

                Integer totalUnidades = lineas.stream()
                                .mapToInt(ProductoPedido::getCantidadSolicitada)
                                .sum();

                List<LineaResumenDTO> lineasResumen = lineas.stream()
                                .map(this::toLineaResumenDTO)
                                .collect(Collectors.toList());

                String clienteNombre = pedido.getClienteNombre() != null ? pedido.getClienteNombre() : "Cliente no disponible";
                String clienteDireccion = null;
                // Si falta la dirección o el nombre, intentamos cargar para completar (opcional, priorizamos performance)
                if (pedido.getDireccionEntrega() != null) {
                        clienteDireccion = pedido.getDireccionEntrega();
                }

                return PedidoResumenDTO.builder()
                                .pedidoId(pedido.getPedidoId())
                                .numeroPedido(pedido.getNumeroPedido())
                                .clienteCc(pedido.getClienteCc())
                                .clienteNombre(clienteNombre)
                                .direccionEntrega(clienteDireccion)
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

                List<LoteResumenDTO> lotes = List.of();
                if (linea.getCantidadConfirmada() > 0) {
                        lotes = loteComprometidoRepository.findByProductoPedidoId(linea.getProductoPedidoId()).stream()
                                        .map(lc -> new LoteResumenDTO(lc.getCodigoLote(), lc.getCantidadComprometida()))
                                        .collect(Collectors.toList());
                }

                return new LineaResumenDTO(
                                linea.getSkuId(),
                                marca,
                                presentacion,
                                linea.getCantidadSolicitada(),
                                linea.getCantidadConfirmada(),
                                lotes);
        }
}
