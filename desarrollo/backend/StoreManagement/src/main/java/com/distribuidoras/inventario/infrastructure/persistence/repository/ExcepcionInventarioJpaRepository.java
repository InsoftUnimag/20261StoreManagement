package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.ExcepcionInventarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExcepcionInventarioJpaRepository extends JpaRepository<ExcepcionInventarioJpaEntity, UUID> {
    List<ExcepcionInventarioJpaEntity> findByTipoExcepcion(String tipoExcepcion);
    List<ExcepcionInventarioJpaEntity> findBySkuId(String skuId);
    List<ExcepcionInventarioJpaEntity> findByCodigoLote(String codigoLote);
    
    /**
     * Count open exceptions (all exceptions are considered open until deleted).
     */
    @Query("SELECT COUNT(e) FROM ExcepcionInventarioJpaEntity e")
    Integer countOpenExceptions();
}
