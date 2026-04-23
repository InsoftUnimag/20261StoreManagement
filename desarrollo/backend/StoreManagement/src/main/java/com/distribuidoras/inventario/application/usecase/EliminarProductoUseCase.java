package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoConLotesActivosException;
import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
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

    public EliminarProductoUseCase(ProductoRepository productoRepository,
                                    LoteRepository loteRepository) {
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
    }

    @Transactional
    public void ejecutar(String skuId) {
        // Verificar que el producto existe
        if (productoRepository.findById(skuId).isEmpty()) {
            throw new ProductoNotFoundException(skuId);
        }

        // FR-011: Validar que no tenga lotes activos (stock > 0)
        if (loteRepository.existsBySkuIdAndCantidadGreaterThan(skuId, 0)) {
            throw new ProductoConLotesActivosException(skuId);
        }

        productoRepository.deleteById(skuId);

        log.info("Producto eliminado: SKU={}", skuId);
    }
}
