package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.ProductoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para Producto.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
@Repository
public interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, String> {

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProductoJpaEntity p WHERE p.activo = true AND LOWER(p.marca) = LOWER(:marca) AND LOWER(p.presentacion) = LOWER(:presentacion)")
    boolean existsByMarcaAndPresentacion(@Param("marca") String marca, @Param("presentacion") String presentacion);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProductoJpaEntity p WHERE p.activo = true AND LOWER(p.marca) = LOWER(:marca) AND LOWER(p.presentacion) = LOWER(:presentacion) AND p.skuId <> :skuId")
    boolean existsByMarcaAndPresentacionAndSkuIdNot(@Param("marca") String marca, @Param("presentacion") String presentacion, @Param("skuId") String skuId);

    List<ProductoJpaEntity> findByMarcaContainingIgnoreCaseOrPresentacionContainingIgnoreCase(
            String marca, String presentacion);

    org.springframework.data.domain.Page<ProductoJpaEntity> findByMarcaContainingIgnoreCaseOrPresentacionContainingIgnoreCase(
            String marca, String presentacion, org.springframework.data.domain.Pageable pageable);

    /**
     * Búsqueda que incluye skuId además de marca y presentación.
     */
    @Query("SELECT p FROM ProductoJpaEntity p WHERE p.activo = true AND (LOWER(p.skuId) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR LOWER(p.marca) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR LOWER(p.presentacion) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    List<ProductoJpaEntity> findByBusquedaAll(@Param("busqueda") String busqueda);

    @Query("SELECT p FROM ProductoJpaEntity p WHERE p.activo = true AND (LOWER(p.skuId) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR LOWER(p.marca) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR LOWER(p.presentacion) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    org.springframework.data.domain.Page<ProductoJpaEntity> findByBusquedaAll(@Param("busqueda") String busqueda, org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT p FROM ProductoJpaEntity p WHERE p.activo = true")
    List<ProductoJpaEntity> findAllActivos();

    @Query("SELECT p FROM ProductoJpaEntity p WHERE p.activo = true")
    org.springframework.data.domain.Page<ProductoJpaEntity> findAllActivos(org.springframework.data.domain.Pageable pageable);

    /**
     * Find multiple products by their IDs (batch query).
     */
    List<ProductoJpaEntity> findBySkuIdIn(List<String> skuIds);
    
    /**
     * Count active SKUs (those with lots with stock > 0).
     */
    @Query("SELECT COUNT(DISTINCT l.skuId) FROM LoteJpaEntity l WHERE l.cantidad > 0")
    Integer countActiveSkus();

    /**
     * Find maximum SKU number from all products (extracts number from format "SKU-XXX").
     * Returns null if no products exist.
     */
    @Query("SELECT MAX(CAST(SUBSTRING(p.skuId, 5) AS int)) FROM ProductoJpaEntity p WHERE p.skuId LIKE 'SKU-%'")
    Integer findMaxSkuNumero();
}
