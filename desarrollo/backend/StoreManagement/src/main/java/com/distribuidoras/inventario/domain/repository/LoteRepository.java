package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.Lote;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface LoteRepository {
    Lote save(Lote lote);
    Optional<Lote> findById(String codigoLote);
    Optional<Lote> findBySkuIdAndCodigoLoteAndFechaVencimiento(UUID skuId, String codigoLote, LocalDate fechaVencimiento);
    List<Lote> findBySkuIdOrderByFechaVencimientoAsc(UUID skuId);
    List<Lote> findBySkuIdWithStock(UUID skuId);
    boolean existsBySkuIdAndCantidadGreaterThan(UUID skuId, int minCantidad);
    
    /**
     * Find all lots with stock for multiple SKUs (batch query).
     * Returns a map of SKU ID to list of lots.
     */
    Map<UUID, List<Lote>> findBySkuIdsWithStock(List<UUID> skuIds);
    
    /**
     * Count lots expiring within days from now.
     */
    Integer countLotesExpiringWithinDays(int daysFromNow);
    
    /**
     * Count all lots with stock > 0.
     */
    Integer countLotesWithStock();
    
    /**
     * Sum total stock across all lots.
     */
    Integer sumTotalStock();
}
