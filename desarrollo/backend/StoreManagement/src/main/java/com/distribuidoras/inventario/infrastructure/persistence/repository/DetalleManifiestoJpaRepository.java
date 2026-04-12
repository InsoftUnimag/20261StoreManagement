package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.DetalleManifiestoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DetalleManifiestoJpaRepository extends JpaRepository<DetalleManifiestoJpaEntity, UUID> {
    List<DetalleManifiestoJpaEntity> findByManifiestoId(UUID manifiestoId);
}
