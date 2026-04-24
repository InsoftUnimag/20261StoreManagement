package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.Lote;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LoteRepository {
    Lote save(Lote lote);
    Optional<Lote> findById(String codigoLote);
    Optional<Lote> findBySkuIdAndCodigoLoteAndFechaVencimiento(String skuId, String codigoLote, LocalDate fechaVencimiento);
    List<Lote> findBySkuIdOrderByFechaVencimientoAsc(String skuId);
    List<Lote> findBySkuIdWithStock(String skuId);
    boolean existsBySkuIdAndCantidadGreaterThan(String skuId, int minCantidad);
    
    /**
     * Find all lots with stock for multiple SKUs (batch query).
     * Returns a map of SKU ID to list of lots.
     */
    Map<String, List<Lote>> findBySkuIdsWithStock(List<String> skuIds);
    
    /**
     * Count lots expiring within days from now.
     */
    Integer countLotesExpiringWithinDays(int daysFromNow);
    
    /**
     * Find lots expiring within days from now.
     */
    List<Lote> findLotesCriticos(int daysFromNow);
    
    /**
     * Count all lots with stock > 0.
     */
    Integer countLotesWithStock();
    
    /**
     * Sum total stock across all lots.
     */
    Integer sumTotalStock();

    /**
     * Find lots with expiration date before current date and with stock > 0.
     */
    List<Lote> findByFechaVencimientoBeforeAndCantidadGreaterThan(LocalDate fechaLimite, int cantidadMinima);
}
