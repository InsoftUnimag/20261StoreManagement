package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.RegistroPickingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistroPickingJpaRepository extends JpaRepository<RegistroPickingJpaEntity, Long> {
    Optional<RegistroPickingJpaEntity> findByPedidoId(Long pedidoId);
}