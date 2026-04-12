package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.LoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoteJpaRepository extends JpaRepository<LoteJpaEntity, String> {

    Optional<LoteJpaEntity> findBySkuIdAndCodigoLoteAndFechaVencimiento(
            UUID skuId, String codigoLote, LocalDate fechaVencimiento);

    List<LoteJpaEntity> findBySkuIdOrderByFechaVencimientoAsc(UUID skuId);

    @Query("SELECT l FROM LoteJpaEntity l WHERE l.skuId = :skuId AND l.cantidad > 0 ORDER BY l.fechaVencimiento ASC")
    List<LoteJpaEntity> findAvailableBySkuOrderByFEFO(@Param("skuId") UUID skuId);

    boolean existsBySkuIdAndCantidadGreaterThan(UUID skuId, int minCantidad);
    
    /**
     * Find all lots with stock for multiple SKUs.
     */
    @Query("SELECT l FROM LoteJpaEntity l WHERE l.skuId IN :skuIds AND l.cantidad > 0 ORDER BY l.skuId, l.fechaVencimiento ASC")
    List<LoteJpaEntity> findBySkuIdsWithStock(@Param("skuIds") List<UUID> skuIds);
    
    /**
     * Count lots expiring within days from now.
     */
    @Query("SELECT COUNT(l) FROM LoteJpaEntity l WHERE l.fechaVencimiento <= :fechaLimite AND l.cantidad > 0")
    Integer countLotesExpiringWithinDays(@Param("fechaLimite") LocalDate fechaLimite);
    
    /**
     * Count all lots with stock > 0.
     */
    @Query("SELECT COUNT(l) FROM LoteJpaEntity l WHERE l.cantidad > 0")
    Integer countLotesWithStock();
    
    /**
     * Sum total stock across all lots.
     */
    @Query("SELECT SUM(l.cantidad) FROM LoteJpaEntity l WHERE l.cantidad > 0")
    Integer sumTotalStock();
}
