package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.Producto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto del dominio para persistencia de Producto.
 * Interface pura sin dependencias de framework.
 */
public interface ProductoRepository {

    Producto save(Producto producto);

    Optional<Producto> findById(UUID skuId);

    List<Producto> findAll();

    List<Producto> findByBusqueda(String busqueda);

    boolean existsByMarcaAndPresentacion(String marca, String presentacion);

    boolean existsByMarcaAndPresentacionAndSkuIdNot(String marca, String presentacion, UUID skuId);

    void deleteById(UUID skuId);
    
    /**
     * Find multiple products by their IDs (batch query).
     */
    Map<UUID, Producto> findByIds(List<UUID> skuIds);
    
    /**
     * Count active SKUs (those with lots with stock > 0).
     */
    Integer countActiveSkus();
}
