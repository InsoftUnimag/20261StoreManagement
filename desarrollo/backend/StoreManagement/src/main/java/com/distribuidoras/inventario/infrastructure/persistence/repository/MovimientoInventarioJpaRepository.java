package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.MovimientoInventarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MovimientoInventarioJpaRepository extends 
        JpaRepository<MovimientoInventarioJpaEntity, UUID>,
        JpaSpecificationExecutor<MovimientoInventarioJpaEntity> {
    
    List<MovimientoInventarioJpaEntity> findByCodigoLoteOrderByFechaMovimientoDesc(String codigoLote);
    
    /**
     * Count movements by type after a specific date.
     */
    @Query("SELECT COUNT(m) FROM MovimientoInventarioJpaEntity m WHERE m.tipoMovimiento = :tipo AND m.fechaMovimiento >= :fecha")
    Integer countByTipoMovimientoAndFechaAfter(@Param("tipo") String tipo, @Param("fecha") LocalDateTime fecha);
}
