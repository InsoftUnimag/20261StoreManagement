package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Puerto del dominio para persistencia de Producto.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 * Interface pura sin dependencias de framework.
 */
public interface ProductoRepository {

    Producto save(Producto producto);

    Optional<Producto> findById(String skuId);

    List<Producto> findAll();

    Page<Producto> findAllWithPagination(Pageable pageable);

    List<Producto> findByBusqueda(String busqueda);

    Page<Producto> findByBusquedaWithPagination(String busqueda, Pageable pageable);

    boolean existsByMarcaAndPresentacion(String marca, String presentacion);

    boolean existsByMarcaAndPresentacionAndSkuIdNot(String marca, String presentacion, String skuId);

    void deleteById(String skuId);
    
    /**
     * Find multiple products by their IDs (batch query).
     */
    Map<String, Producto> findByIds(List<String> skuIds);
    
    /**
     * Count active SKUs (those with lots with stock > 0).
     */
    Integer countActiveSkus();

    /**
     * Find the maximum SKU number from all products (for auto-increment).
     * Extracts number from format "SKU-***" and returns it.
     */
    Optional<Integer> findMaxSkuNumero();
}
