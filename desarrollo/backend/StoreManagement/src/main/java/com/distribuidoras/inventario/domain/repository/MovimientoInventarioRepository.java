package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.MovimientoInventario;
import com.distribuidoras.inventario.domain.model.enums.TipoMovimiento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MovimientoInventarioRepository {
    MovimientoInventario save(MovimientoInventario movimiento);
    List<MovimientoInventario> findByLoteId(String codigoLote);
    
    /**
     * Find movements with dynamic filters (functional approach).
     * All parameters are optional - null means no filter.
     */
    List<MovimientoInventario> findByFilters(
            UUID skuId,
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
            UUID skuId,
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
