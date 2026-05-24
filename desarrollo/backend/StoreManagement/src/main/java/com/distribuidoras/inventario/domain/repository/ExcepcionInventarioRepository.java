package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.ExcepcionInventario;
import com.distribuidoras.inventario.domain.model.enums.TipoExcepcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto del dominio para ExcepcionInventario.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
public interface ExcepcionInventarioRepository {
    ExcepcionInventario save(ExcepcionInventario excepcion);
    Optional<ExcepcionInventario> findById(Long excepcionId);
    List<ExcepcionInventario> findAll();
    List<ExcepcionInventario> findByFilters(TipoExcepcion tipo, String skuId);
    List<ExcepcionInventario> findByCodigoLote(String codigoLote);
    List<ExcepcionInventario> findByFechaRegistroAfter(LocalDateTime fecha);
    Page<ExcepcionInventario> findByFiltersWithPagination(TipoExcepcion tipo, String skuId, 
            LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
    
    /**
     * Count open exceptions.
     */
    Integer countOpenExceptions();
}
