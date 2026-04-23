package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.ExcepcionInventario;
import com.distribuidoras.inventario.domain.model.enums.TipoExcepcion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto del dominio para ExcepcionInventario.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
public interface ExcepcionInventarioRepository {
    ExcepcionInventario save(ExcepcionInventario excepcion);
    Optional<ExcepcionInventario> findById(UUID excepcionId);
    List<ExcepcionInventario> findAll();
    List<ExcepcionInventario> findByFilters(TipoExcepcion tipo, String skuId);
    List<ExcepcionInventario> findByCodigoLote(String codigoLote);
    
    /**
     * Count open exceptions.
     */
    Integer countOpenExceptions();
}
