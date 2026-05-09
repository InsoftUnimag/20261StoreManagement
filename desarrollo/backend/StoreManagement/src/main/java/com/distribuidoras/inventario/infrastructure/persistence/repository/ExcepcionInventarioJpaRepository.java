package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.ExcepcionInventarioJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExcepcionInventarioJpaRepository extends JpaRepository<ExcepcionInventarioJpaEntity, UUID> {
    List<ExcepcionInventarioJpaEntity> findByTipoExcepcion(String tipoExcepcion);
    List<ExcepcionInventarioJpaEntity> findBySkuId(String skuId);
    List<ExcepcionInventarioJpaEntity> findByCodigoLote(String codigoLote);
    
    @Query("SELECT e FROM ExcepcionInventarioJpaEntity e WHERE e.fechaRegistro >= :fecha ORDER BY e.fechaRegistro DESC")
    List<ExcepcionInventarioJpaEntity> findByFechaRegistroAfter(@Param("fecha") LocalDateTime fecha);
    
    @Query("SELECT e FROM ExcepcionInventarioJpaEntity e WHERE " +
            "(:tipo IS NULL OR e.tipoExcepcion = :tipo) AND " +
            "(:skuId IS NULL OR e.skuId = :skuId) AND " +
            "(:desde IS NULL OR e.fechaRegistro >= :desde) AND " +
            "(:hasta IS NULL OR e.fechaRegistro <= :hasta)")
    Page<ExcepcionInventarioJpaEntity> findByFiltersWithPagination(
            @Param("tipo") String tipo,
            @Param("skuId") String skuId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable);
    
    @Query("SELECT COUNT(e) FROM ExcepcionInventarioJpaEntity e")
    Integer countOpenExceptions();
}
