package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.ProductoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para Producto.
 */
@Repository
public interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, UUID> {

    boolean existsByMarcaAndPresentacion(String marca, String presentacion);

    boolean existsByMarcaAndPresentacionAndSkuIdNot(String marca, String presentacion, UUID skuId);

    List<ProductoJpaEntity> findByMarcaContainingIgnoreCaseOrPresentacionContainingIgnoreCase(
            String marca, String presentacion);
    
    /**
     * Find multiple products by their IDs (batch query).
     */
    List<ProductoJpaEntity> findBySkuIdIn(List<UUID> skuIds);
    
    /**
     * Count active SKUs (those with lots with stock > 0).
     */
    @Query("SELECT COUNT(DISTINCT l.skuId) FROM LoteJpaEntity l WHERE l.cantidad > 0")
    Integer countActiveSkus();
}
