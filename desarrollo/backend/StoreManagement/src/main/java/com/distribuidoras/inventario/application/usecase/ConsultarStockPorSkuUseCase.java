package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.application.usecase.mapper.InventarioMapper;
import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.infrastructure.persistence.repository.StockGlobalSkuJpaRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.StockGlobalSkuJpaEntity;
import com.distribuidoras.inventario.infrastructure.web.dto.LoteStockDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.StockDisponibleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Use Case: Consultar stock disponible por SKU con detalle de lotes FEFO.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 * Spec 05: Consultar Inventario
 * 
 * FR-035: Calcular stock total como suma de lotes Disponibles
 * FR-036: Ordenar lotes por fecha_vencimiento ASC (FEFO)
 * FR-037: Resaltar lotes con fecha de vencimiento dentro del umbral crítico
 * FR-038: Diferenciar stock "Disponible" vs "Comprometido"
 */
@Service
public class ConsultarStockPorSkuUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarStockPorSkuUseCase.class);

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final StockGlobalSkuJpaRepository stockGlobalSkuRepository;

    public ConsultarStockPorSkuUseCase(ProductoRepository productoRepository,
                                       LoteRepository loteRepository,
                                       StockGlobalSkuJpaRepository stockGlobalSkuRepository) {
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
        this.stockGlobalSkuRepository = stockGlobalSkuRepository;
    }

    /**
     * Ejecuta el caso de uso de consulta de stock por SKU.
     * 
     * @param skuId String del producto (formato SKU-001, SKU-012, etc.)
     * @return StockDisponibleDTO con información completa del stock
     */
    @Transactional(readOnly = true)
    public StockDisponibleDTO ejecutar(String skuId) {
        return ejecutarConFiltros(skuId, null, null, null);
    }

    /**
     * Ejecuta el caso de uso de consulta de stock por SKU con filtros FEFO.
     * 
     * @param skuId String del producto (formato SKU-001, SKU-012, etc.)
     * @param estadoFiltro Estado del lote (disponible, vencido, critico)
     * @param fechaDesde Fecha de vencimiento desde
     * @param fechaHasta Fecha de vencimiento hasta
     * @return StockDisponibleDTO con información filtrada
     */
    @Transactional(readOnly = true)
    public StockDisponibleDTO ejecutarConFiltros(String skuId, String estadoFiltro, 
                                                 LocalDate fechaDesde, LocalDate fechaHasta) {
        log.info("Consultando stock para SKU: {} con filtros: estado={}, fechaDesde={}, fechaHasta={}", 
                skuId, estadoFiltro, fechaDesde, fechaHasta);

        // 1. Buscar Producto por sku_id (404 si no existe)
        Producto producto = productoRepository.findById(skuId)
                .orElseThrow(() -> new ProductoNotFoundException(skuId));

        // 2. Buscar Lotes con cantidad > 0, ordenados por fecha_vencimiento ASC (FEFO)
        List<Lote> lotes = loteRepository.findBySkuIdWithStock(skuId);

        // 3. Aplicar filtros FEFO según parámetros
        List<Lote> lotesFiltrados = lotes.stream()
                .filter(lote -> {
                    boolean cumpleFiltro = true;
                    
                    // Filtro por estado
                    if (estadoFiltro != null && !estadoFiltro.isBlank()) {
                        switch (estadoFiltro.toLowerCase()) {
                            case "disponible":
                                cumpleFiltro = lote.getDisponible() && lote.getCantidad() > 0;
                                break;
                            case "critico":
                                cumpleFiltro = lote.getFlagUrgenciaFefo();
                                break;
                            case "vencido":
                                cumpleFiltro = lote.getFechaVencimiento().isBefore(LocalDate.now());
                                break;
                            case "proximoa_vencer":
                                cumpleFiltro = lote.getFechaVencimiento().isAfter(LocalDate.now()) &&
                                              lote.getFechaVencimiento().isBefore(LocalDate.now().plusDays(30));
                                break;
                        }
                    }
                    
                    // Filtro por rango de fechas de vencimiento
                    if (fechaDesde != null) {
                        cumpleFiltro = cumpleFiltro && !lote.getFechaVencimiento().isBefore(fechaDesde);
                    }
                    if (fechaHasta != null) {
                        cumpleFiltro = cumpleFiltro && !lote.getFechaVencimiento().isAfter(fechaHasta);
                    }
                    
                    return cumpleFiltro;
                })
                .toList();

        // 4. Calcular fisico_total = SUM(cantidad) de lotes filtrados
        Integer fisicoTotal = lotesFiltrados.stream()
                .mapToInt(Lote::getCantidad)
                .sum();
                
        // FR-038: Diferenciar stock "Disponible" vs "Comprometido"
        java.util.Optional<StockGlobalSkuJpaEntity> stockGlobalOpt = stockGlobalSkuRepository.findById(Objects.requireNonNull(skuId));
        Integer disponibles = stockGlobalOpt.map(StockGlobalSkuJpaEntity::getDisponibles).orElse(fisicoTotal);
        Integer comprometidos = stockGlobalOpt.map(StockGlobalSkuJpaEntity::getComprometidos).orElse(0);

        // 5. Transformar lotes a DTOs usando mapper funcional
        List<LoteStockDTO> lotesDTO = lotesFiltrados.stream()
                .map(InventarioMapper.toLoteStockDTO())
                .toList();

        // 6. Identificar proximo_vencimiento (primer lote en lista FEFO filtrada)
        StockDisponibleDTO.ProximoVencimientoInfo proximoVencimiento = lotesFiltrados.stream()
                .findFirst()
                .map(lote -> StockDisponibleDTO.ProximoVencimientoInfo.builder()
                        .codigoLote(lote.getCodigoLote())
                        .fechaVencimiento(lote.getFechaVencimiento())
                        .diasRestantes(InventarioMapper.calcularDiasHastaVencimiento(lote.getFechaVencimiento()))
                        .build())
                .orElse(null);

        // 7. Construir resultado usando builder pattern
        StockDisponibleDTO resultado = StockDisponibleDTO.builder()
                .sku(InventarioMapper.toProductoInfo().apply(producto))
                .fisicoTotal(fisicoTotal)
                .disponibles(disponibles)
                .comprometidos(comprometidos)
                .lotes(lotesDTO)
                .proximoVencimiento(proximoVencimiento)
                .filtrosAplicados(estadoFiltro != null || fechaDesde != null || fechaHasta != null)
                .build();

        log.info("SKU {} - Stock total: {}, Lotes filtrados: {}", skuId, fisicoTotal, lotesDTO.size());

        return resultado;
    }
}
