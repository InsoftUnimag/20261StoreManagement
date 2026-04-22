package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.LoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteJpaRepository extends JpaRepository<LoteJpaEntity, String> {

    Optional<LoteJpaEntity> findBySkuIdAndCodigoLoteAndFechaVencimiento(
            String skuId, String codigoLote, LocalDate fechaVencimiento);

    List<LoteJpaEntity> findBySkuIdOrderByFechaVencimientoAsc(String skuId);

    @Query("SELECT l FROM LoteJpaEntity l WHERE l.skuId = :skuId AND l.disponible = true ORDER BY l.fechaVencimiento ASC")
    List<LoteJpaEntity> findAvailableBySkuOrderByFEFO(@Param("skuId") String skuId);

    boolean existsBySkuIdAndCantidadGreaterThan(String skuId, int minCantidad);
    
    /**
     * Find all lots with stock for multiple SKUs.
     */
    @Query("SELECT l FROM LoteJpaEntity l WHERE l.skuId IN :skuIds AND l.disponible = true ORDER BY l.skuId, l.fechaVencimiento ASC")
    List<LoteJpaEntity> findBySkuIdsWithStock(@Param("skuIds") List<String> skuIds);
    
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
     * Find lots expiring within a limit date with stock > 0
     */
    @Query("SELECT l FROM LoteJpaEntity l WHERE l.fechaVencimiento <= :fechaLimite AND l.cantidad > 0 ORDER BY l.fechaVencimiento ASC")
    List<LoteJpaEntity> findLotesCriticos(@Param("fechaLimite") LocalDate fechaLimite);
    
    /**
     * Sum total stock across all lots.
     */
    @Query("SELECT SUM(l.cantidad) FROM LoteJpaEntity l WHERE l.cantidad > 0")
    Integer sumTotalStock();

    /**
     * Find lots with expiration date before current date and with stock > 0.
     */
    @Query("SELECT l FROM LoteJpaEntity l WHERE l.fechaVencimiento < :fechaLimite AND l.cantidad > 0 ORDER BY l.fechaVencimiento ASC")
    List<LoteJpaEntity> findByFechaVencimientoBeforeAndCantidadGreaterThan(@Param("fechaLimite") LocalDate fechaLimite, @Param("cantidadMinima") int cantidadMinima);
}
