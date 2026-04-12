package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.application.usecase.mapper.InventarioMapper;
import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.LoteStockDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.StockDisponibleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use Case: Consultar stock disponible por SKU con detalle de lotes FEFO.
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
    private static final int UMBRAL_CRITICO_DIAS = 7;

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;

    public ConsultarStockPorSkuUseCase(ProductoRepository productoRepository,
                                       LoteRepository loteRepository) {
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
    }

    /**
     * Ejecuta el caso de uso de consulta de stock por SKU.
     * 
     * @param skuId UUID del producto
     * @return StockDisponibleDTO con información completa del stock
     */
    @Transactional(readOnly = true)
    public StockDisponibleDTO ejecutar(UUID skuId) {
        log.info("Consultando stock para SKU: {}", skuId);

        // 1. Buscar Producto por sku_id (404 si no existe)
        Producto producto = productoRepository.findById(skuId)
                .orElseThrow(() -> new ProductoNotFoundException(skuId));

        // 2. Buscar Lotes con cantidad > 0, ordenados por fecha_vencimiento ASC (FEFO)
        List<Lote> lotes = loteRepository.findBySkuIdWithStock(skuId);

        // 3. Calcular fisico_total = SUM(cantidad) - Functional approach
        Integer fisicoTotal = lotes.stream()
                .mapToInt(Lote::getCantidad)
                .sum();

        // 4. Transformar lotes a DTOs usando mapper funcional
        List<LoteStockDTO> lotesDTO = lotes.stream()
                .map(InventarioMapper.toLoteStockDTO())
                .toList();

        // 5. Identificar proximo_vencimiento (primer lote en lista FEFO)
        StockDisponibleDTO.ProximoVencimientoInfo proximoVencimiento = lotes.stream()
                .findFirst()
                .map(lote -> StockDisponibleDTO.ProximoVencimientoInfo.builder()
                        .codigoLote(lote.getCodigoLote())
                        .fechaVencimiento(lote.getFechaVencimiento())
                        .diasRestantes(InventarioMapper.calcularDiasHastaVencimiento(lote.getFechaVencimiento()))
                        .build())
                .orElse(null);

        // 6. Construir resultado usando builder pattern
        StockDisponibleDTO resultado = StockDisponibleDTO.builder()
                .sku(InventarioMapper.toProductoInfo().apply(producto))
                .fisicoTotal(fisicoTotal)
                .lotes(lotesDTO)
                .proximoVencimiento(proximoVencimiento)
                .build();

        log.info("SKU {} - Stock total: {}, Lotes: {}", skuId, fisicoTotal, lotesDTO.size());

        return resultado;
    }
}
