package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.ManifiestoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ManifiestoJpaRepository extends JpaRepository<ManifiestoJpaEntity, UUID> {
    List<ManifiestoJpaEntity> findByEstadoInOrderByFechaEmisionAsc(List<String> estados);
}
