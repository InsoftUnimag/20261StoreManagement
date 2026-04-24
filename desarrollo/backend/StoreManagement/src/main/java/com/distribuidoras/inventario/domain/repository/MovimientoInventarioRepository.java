package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.MovimientoInventario;
import com.distribuidoras.inventario.domain.model.enums.TipoMovimiento;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Puerto del dominio para MovimientoInventario.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
public interface MovimientoInventarioRepository {
    MovimientoInventario save(MovimientoInventario movimiento);
    List<MovimientoInventario> findByLoteId(String codigoLote);
    
    /**
     * Find movements with dynamic filters (functional approach).
     * All parameters are optional - null means no filter.
     */
    List<MovimientoInventario> findByFilters(
            String skuId,
            String codigoLote,
            TipoMovimiento tipoMovimiento,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            int page,
            int size
    );
    
    /**
     * Count movements matching filters.
     */
    Long countByFilters(
            String skuId,
            String codigoLote,
            TipoMovimiento tipoMovimiento,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    );
    
    /**
     * Count movements by type for today.
     */
    Integer countByTipoMovimientoAndFechaAfter(TipoMovimiento tipo, LocalDateTime fecha);
}
