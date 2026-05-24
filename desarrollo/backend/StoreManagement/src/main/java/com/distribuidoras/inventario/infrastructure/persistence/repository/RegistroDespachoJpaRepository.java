package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.RegistroDespachoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistroDespachoJpaRepository extends JpaRepository<RegistroDespachoJpaEntity, Long> {
    Optional<RegistroDespachoJpaEntity> findByPedidoId(Long pedidoId);
}