package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoConLotesActivosException;
import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.MovimientoInventarioRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Eliminar Producto.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 * Spec: 02_modificar_plantilla_producto.md (FR-011)
 *
 * Valida que el producto no tenga lotes activos antes de eliminar.
 * FR-011: DEBE impedir eliminación de producto con lotes activos o historial.
 * SC-006: 0% de productos con lotes activos pueden ser eliminados.
 */
@Service
public class EliminarProductoUseCase {

    private static final Logger log = LoggerFactory.getLogger(EliminarProductoUseCase.class);

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public EliminarProductoUseCase(ProductoRepository productoRepository,
                                    LoteRepository loteRepository,
                                    MovimientoInventarioRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Transactional
    public void ejecutar(String skuId) {
        // Verificar que el producto existe
        var productoOptional = productoRepository.findById(skuId);
        if (productoOptional.isEmpty()) {
            throw new ProductoNotFoundException(skuId);
        }
        
        var producto = productoOptional.get();

        // FR-011: Validar que no tenga historial completo (cualquier lote, incluso con stock 0)
        // Esto incluye lotes activos (stock > 0) o lotes históricos (stock = 0 pero con registros de movimientos)
        
        // 1. Validar lotes activos (con stock > 0)
        var lotesActivos = loteRepository.findBySkuIdWithStock(skuId);
        if (!lotesActivos.isEmpty()) {
            throw new ProductoConLotesActivosException(skuId);
        }
        
        // 2. Validar cualquier lote asociado al producto (incluso con stock 0)
        // Buscamos lotes por SKU sin importar la cantidad
        var lotesPorSku = loteRepository.findBySkuIdOrderByFechaVencimientoAsc(skuId);
        if (!lotesPorSku.isEmpty()) {
            throw new ProductoConLotesActivosException(skuId + " - Producto tiene lotes históricos registrados");
        }
        
        // 3. Validar movimientos de inventario asociados al producto
        // Para esto necesitamos buscar lotes y verificar si tienen movimientos
        // O buscar directamente movimientos por SKU si el repositorio lo soporta
        // Asumimos que si hay lotes, ya están siendo validados arriba
        
        // 4. Verificar si hay movimientos registrados que hagan referencia al SKU
        // Buscamos movimientos que puedan estar relacionados con este SKU
        // (esto depende de cómo se implemente findMovimientosPorSkuId)
        Long movimientosCount = movimientoRepository.countByFilters(skuId, null, null, null, null);
        if (movimientosCount != null && movimientosCount > 0) {
            throw new ProductoConLotesActivosException(skuId + " - Producto tiene movimientos de inventario registrados");
        }

        producto.setActivo(false);
        productoRepository.save(producto);

        log.info("Producto desactivado (borrado lógico): SKU={}", skuId);
    }
}
