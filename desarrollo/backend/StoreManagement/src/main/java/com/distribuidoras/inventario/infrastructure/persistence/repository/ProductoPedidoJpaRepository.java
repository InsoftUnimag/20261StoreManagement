package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.ProductoPedidoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductoPedidoJpaRepository extends JpaRepository<ProductoPedidoJpaEntity, UUID> {
    
    List<ProductoPedidoJpaEntity> findByPedidoId(UUID pedidoId);
}
