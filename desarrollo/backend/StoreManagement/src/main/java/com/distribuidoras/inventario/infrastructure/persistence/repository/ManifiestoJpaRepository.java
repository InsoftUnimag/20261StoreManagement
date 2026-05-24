package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.ManifiestoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import java.time.LocalDate;

@Repository
public interface ManifiestoJpaRepository extends JpaRepository<ManifiestoJpaEntity, Long> {
    List<ManifiestoJpaEntity> findByEstadoInOrderByFechaEmisionAsc(List<String> estados);
    List<ManifiestoJpaEntity> findByFechaEmisionBetweenOrderByFechaEmisionDesc(LocalDate desde, LocalDate hasta);
    List<ManifiestoJpaEntity> findTop100ByFechaEmisionBetweenOrderByFechaEmisionDesc(LocalDate desde, LocalDate hasta);
    
    @Query(value = "SELECT CAST(regexp_replace(numero_manifiesto, '^.*-', '') AS INTEGER) FROM manifiesto WHERE fecha_emision = :fecha ORDER BY numero_manifiesto DESC LIMIT 1", nativeQuery = true)
    Optional<Integer> findMaxNumeroManifiestoByFecha(@Param("fecha") LocalDate fecha);
}
