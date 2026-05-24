package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.DetalleManifiestoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface DetalleManifiestoJpaRepository extends JpaRepository<DetalleManifiestoJpaEntity, Long> {
    List<DetalleManifiestoJpaEntity> findByManifiestoId(Long manifiestoId);
    List<DetalleManifiestoJpaEntity> findByManifiestoIdIn(List<Long> manifiestoIds);
}
