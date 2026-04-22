package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.StockGlobalSkuJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockGlobalSkuJpaRepository extends JpaRepository<StockGlobalSkuJpaEntity, String> {
    int countByDisponiblesLessThan(Integer disponibles);
}
