package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.application.usecase.mapper.InventarioMapper;
import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.model.MovimientoInventario;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.model.enums.TipoMovimiento;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.MovimientoInventarioRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.MovimientoInventarioDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.MovimientosInventarioResponseDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.PaginacionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case: Consultar kardex (movimientos de inventario) con filtros.
 * Spec 05: Consultar Inventario - FR-041 (historial de MovimientosInventario)
 * 
 * FR-043: Consultar kardex con filtros por SKU, lote, tipo y fecha
 * FR-044: Kardex incluye información contextual: producto, operario, pedido, excepción
 */
@Service
public class ConsultarMovimientosInventarioUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarMovimientosInventarioUseCase.class);

    private final MovimientoInventarioRepository movimientoRepository;
    private final LoteRepository loteRepository;
    private final ProductoRepository productoRepository;

    public ConsultarMovimientosInventarioUseCase(MovimientoInventarioRepository movimientoRepository,
                                                  LoteRepository loteRepository,
                                                  ProductoRepository productoRepository) {
        this.movimientoRepository = movimientoRepository;
        this.loteRepository = loteRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Command object for query filters.
     */
    public record FiltrosKardexDTO(
            UUID skuId,
            String codigoLote,
            TipoMovimiento tipoMovimiento,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Integer page,
            Integer size
    ) {
        public FiltrosKardexDTO {
            // Defaults
            if (page == null) page = 0;
            if (size == null) size = 20;
            // Max size limit
            size = Math.min(size, 100);
        }
    }

    /**
     * Ejecuta el caso de uso de consulta de movimientos con filtros.
     * 
     * @param filtros Filtros de búsqueda
     * @return MovimientosInventarioResponseDTO con resultados paginados
     */
    @Transactional(readOnly = true)
    public MovimientosInventarioResponseDTO ejecutar(FiltrosKardexDTO filtros) {
        log.info("Consultando kardex con filtros: skuId={}, codigoLote={}, tipo={}, desde={}, hasta={}",
                filtros.skuId(), filtros.codigoLote(), filtros.tipoMovimiento(), 
                filtros.fechaDesde(), filtros.fechaHasta());

        // Convert LocalDate to LocalDateTime
        LocalDateTime fechaDesde = filtros.fechaDesde() != null ? 
                filtros.fechaDesde().atStartOfDay() : null;
        LocalDateTime fechaHasta = filtros.fechaHasta() != null ? 
                filtros.fechaHasta().atTime(LocalTime.MAX) : null;

        // If skuId is provided, we need to get lot codes first
        List<String> codigosLote = null;
        if (filtros.skuId() != null) {
            List<Lote> lotes = loteRepository.findBySkuIdOrderByFechaVencimientoAsc(filtros.skuId());
            codigosLote = lotes.stream()
                    .map(Lote::getCodigoLote)
                    .toList();
            
            // If no lotes exist for this SKU, return empty result
            if (codigosLote.isEmpty()) {
                return createEmptyResponse(filtros.page(), filtros.size());
            }
        }

        // Use the specific codigoLote filter if provided, otherwise use sku-derived codes
        String codigoLoteFilter = filtros.codigoLote() != null ? 
                filtros.codigoLote() : null;
        
        // If we have sku-derived codes and no specific code filter, 
        // we need to query for each code
        List<MovimientoInventario> movimientos;
        Long totalElements;
        
        if (filtros.skuId() != null && filtros.codigoLote() == null) {
            // Multiple lot codes - query each and merge
            movimientos = codigosLote.stream()
                    .flatMap(codigo -> movimientoRepository.findByFilters(
                            null, codigo, filtros.tipoMovimiento(), 
                            fechaDesde, fechaHasta, 0, Integer.MAX_VALUE).stream())
                    .sorted((m1, m2) -> m2.getFechaMovimiento().compareTo(m1.getFechaMovimiento()))
                    .skip((long) filtros.page() * filtros.size())
                    .limit(filtros.size())
                    .toList();
            
            totalElements = movimientoRepository.countByFilters(
                    null, null, filtros.tipoMovimiento(), 
                    fechaDesde, fechaHasta);
        } else {
            // Single query with filters
            movimientos = movimientoRepository.findByFilters(
                    null, codigoLoteFilter, filtros.tipoMovimiento(),
                    fechaDesde, fechaHasta, filtros.page(), filtros.size());
            
            totalElements = movimientoRepository.countByFilters(
                    null, codigoLoteFilter, filtros.tipoMovimiento(),
                    fechaDesde, fechaHasta);
        }

        // Get related entities for context - Functional approach
        Set<UUID> skuIds = movimientos.stream()
                .map(MovimientoInventario::getCodigoLote)
                .distinct()
                .map(codigo -> loteRepository.findById(codigo))
                .filter(Optional::isPresent)
                .map(opt -> opt.get().getSkuId())
                .collect(Collectors.toSet());

        Map<UUID, Producto> productos = productoRepository.findByIds(List.copyOf(skuIds));

        // Transform movements to DTOs using functional mapping
        List<MovimientoInventarioDTO> movimientosDTO = movimientos.stream()
                .map(mov -> toMovimientoDTO(mov, productos))
                .toList();

        // Build pagination info
        int totalPages = (int) Math.ceil((double) totalElements / filtros.size());

        MovimientosInventarioResponseDTO resultado = MovimientosInventarioResponseDTO.builder()
                .movimientos(movimientosDTO)
                .paginacion(PaginacionDTO.builder()
                        .totalElements(totalElements)
                        .totalPages(totalPages)
                        .currentPage(filtros.page())
                        .pageSize(filtros.size())
                        .build())
                .build();

        log.info("Kardex consultado: {} movimientos de {} totales", movimientosDTO.size(), totalElements);

        return resultado;
    }

    /**
     * Creates an empty response with pagination.
     */
    private MovimientosInventarioResponseDTO createEmptyResponse(int page, int size) {
        return MovimientosInventarioResponseDTO.builder()
                .movimientos(List.of())
                .paginacion(PaginacionDTO.builder()
                        .totalElements(0L)
                        .totalPages(0)
                        .currentPage(page)
                        .pageSize(size)
                        .build())
                .build();
    }

    /**
     * Transform a single movement to DTO with context.
     */
    private MovimientoInventarioDTO toMovimientoDTO(MovimientoInventario mov, 
                                                     Map<UUID, Producto> productos) {
        // Get lote context
        Lote lote = loteRepository.findById(mov.getCodigoLote()).orElse(null);
        Producto producto = lote != null ? productos.get(lote.getSkuId()) : null;

        MovimientoInventarioDTO.LoteInfo loteInfo = lote != null ?
                MovimientoInventarioDTO.LoteInfo.builder()
                        .codigoLote(lote.getCodigoLote())
                        .codigoLoteInterno(lote.getCodigoLote())
                        .build() : null;

        MovimientoInventarioDTO.ProductoInfo productoInfo = producto != null ?
                MovimientoInventarioDTO.ProductoInfo.builder()
                        .skuId(producto.getSkuId().toString())
                        .marca(producto.getMarca())
                        .presentacion(producto.getPresentacion())
                        .build() : null;

        return MovimientoInventarioDTO.builder()
                .movimientoId(mov.getMovimientoId().toString())
                .tipoMovimiento(mov.getTipoMovimiento().name())
                .cantidad(mov.getCantidad())
                .fechaMovimiento(mov.getFechaMovimiento())
                .lote(loteInfo)
                .producto(productoInfo)
                .operarioNombre(mov.getOperarioId() != null ? 
                        "Operario-" + mov.getOperarioId().toString().substring(0, 8) : null)
                .pedidoId(mov.getPedidoId() != null ? mov.getPedidoId().toString() : null)
                .observaciones(mov.getObservaciones())
                .build();
    }
}
