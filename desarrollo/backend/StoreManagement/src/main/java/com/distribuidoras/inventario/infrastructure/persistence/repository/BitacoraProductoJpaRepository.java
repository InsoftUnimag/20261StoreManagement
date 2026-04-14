package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.BitacoraProductoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para BitacoraProducto.
 */
@Repository
public interface BitacoraProductoJpaRepository extends JpaRepository<BitacoraProductoJpaEntity, Long> {

    List<BitacoraProductoJpaEntity> findBySkuIdRefOrderByFechaDesc(UUID skuIdRef);
}
