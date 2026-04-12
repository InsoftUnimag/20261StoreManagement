package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.ExcepcionInventario;
import com.distribuidoras.inventario.domain.model.enums.TipoExcepcion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExcepcionInventarioRepository {
    ExcepcionInventario save(ExcepcionInventario excepcion);
    Optional<ExcepcionInventario> findById(UUID excepcionId);
    List<ExcepcionInventario> findAll();
    List<ExcepcionInventario> findByFilters(TipoExcepcion tipo, UUID skuId);
    
    /**
     * Count open exceptions.
     */
    Integer countOpenExceptions();
}
