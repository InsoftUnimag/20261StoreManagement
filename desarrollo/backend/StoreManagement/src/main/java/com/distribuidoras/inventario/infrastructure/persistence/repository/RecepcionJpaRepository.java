package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.RecepcionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecepcionJpaRepository extends JpaRepository<RecepcionJpaEntity, UUID> {
    @Query(value = "SELECT CAST(regexp_replace(numero_recepcion, '^.*-', '') AS INTEGER) FROM recepcion WHERE DATE(fecha_recepcion) = :fecha ORDER BY numero_recepcion DESC LIMIT 1", nativeQuery = true)
    Optional<Integer> findMaxNumeroRecepcionByFecha(@Param("fecha") LocalDate fecha);
    java.util.List<RecepcionJpaEntity> findAllByOrderByFechaRecepcionDesc();
}
