package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.StockMultipleDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.StockResumenDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Use Case: Consultar stock de múltiples SKUs (batch query).
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 * Spec 05: Consultar Inventario
 * 
 * Optimized for performance with single query using IN clause.
 */
@Service
public class ConsultarStockMultipleSkusUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarStockMultipleSkusUseCase.class);

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;

    public ConsultarStockMultipleSkusUseCase(ProductoRepository productoRepository,
                                             LoteRepository loteRepository) {
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
    }

    /**
     * Ejecuta el caso de uso de consulta de stock múltiple.
     * 
     * @param skuIds Lista de Strings de productos (formato SKU-001, etc.)
     * @param includeZeroStock Si true, incluye SKUs con stock = 0
     * @return StockMultipleDTO con resumen de stock
     */
    @Transactional(readOnly = true)
    public StockMultipleDTO ejecutar(List<String> skuIds, boolean includeZeroStock) {
        log.info("Consultando stock para {} SKUs, includeZeroStock={}", skuIds.size(), includeZeroStock);

        // 1. Query eficiente con JOIN: Producto + SUM(Lote.cantidad) GROUP BY sku_id
        Map<String, List<Lote>> lotesPorSku = loteRepository.findBySkuIdsWithStock(skuIds);

        // 2. Obtener productos existentes
        Map<String, Producto> productos = productoRepository.findByIds(skuIds);

        // 3. Construir lista de resumen - Functional approach with stream composition
        List<StockResumenDTO> stocks = skuIds.stream()
                .filter(productos::containsKey) // Solo productos existentes
                .map(skuId -> {
                    List<Lote> lotes = lotesPorSku.getOrDefault(skuId, List.of());
                    Integer stockTotal = lotes.stream()
                            .mapToInt(Lote::getCantidad)
                            .sum();
                    return Map.entry(skuId, stockTotal);
                })
                .filter(entry -> includeZeroStock || entry.getValue() > 0) // Filter by stock
                .map(entry -> {
                    String skuId = entry.getKey();
                    Producto producto = productos.get(skuId);
                    return StockResumenDTO.builder()
                            .skuId(skuId)
                            .marca(producto.getMarca())
                            .presentacion(producto.getPresentacion())
                            .fisicoTotal(entry.getValue())
                            .build();
                })
                .sorted((a, b) -> { // Ordenar por marca, presentacion
                    int marcaCmp = a.marca().compareToIgnoreCase(b.marca());
                    return marcaCmp != 0 ? marcaCmp : a.presentacion().compareToIgnoreCase(b.presentacion());
                })
                .toList();

        StockMultipleDTO resultado = StockMultipleDTO.builder()
                .stocks(stocks)
                .build();

        log.info("Stock múltiple consultado: {} SKUs con stock", stocks.size());

        return resultado;
    }
}
