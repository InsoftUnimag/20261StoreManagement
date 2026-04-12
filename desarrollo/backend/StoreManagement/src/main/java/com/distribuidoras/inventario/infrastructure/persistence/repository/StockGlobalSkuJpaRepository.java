package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.StockGlobalSkuJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StockGlobalSkuJpaRepository extends JpaRepository<StockGlobalSkuJpaEntity, UUID> {
}
