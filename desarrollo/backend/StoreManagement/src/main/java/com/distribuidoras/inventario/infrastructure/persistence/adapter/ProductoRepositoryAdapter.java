package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.ProductoJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.ProductoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador que implementa el puerto ProductoRepository del dominio
 * delegando a Spring Data JPA.
 * Convierte entre ProductoJpaEntity <-> Producto (domain).
 */
@Component
public class ProductoRepositoryAdapter implements ProductoRepository {

    private final ProductoJpaRepository jpaRepository;

    public ProductoRepositoryAdapter(ProductoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Producto save(Producto producto) {
        ProductoJpaEntity entity = toEntity(producto);
        ProductoJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Producto> findById(UUID skuId) {
        return jpaRepository.findById(skuId).map(this::toDomain);
    }

    @Override
    public List<Producto> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Producto> findByBusqueda(String busqueda) {
        return jpaRepository
                .findByMarcaContainingIgnoreCaseOrPresentacionContainingIgnoreCase(busqueda, busqueda)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByMarcaAndPresentacion(String marca, String presentacion) {
        return jpaRepository.existsByMarcaAndPresentacion(marca, presentacion);
    }

    @Override
    public boolean existsByMarcaAndPresentacionAndSkuIdNot(String marca, String presentacion, UUID skuId) {
        return jpaRepository.existsByMarcaAndPresentacionAndSkuIdNot(marca, presentacion, skuId);
    }

    @Override
    public void deleteById(UUID skuId) {
        jpaRepository.deleteById(skuId);
    }
    
    @Override
    public Map<UUID, Producto> findByIds(List<UUID> skuIds) {
        // Functional approach: convert list to map
        return jpaRepository.findBySkuIdIn(skuIds).stream()
                .map(this::toDomain)
                .collect(Collectors.toMap(Producto::getSkuId, p -> p));
    }
    
    @Override
    public Integer countActiveSkus() {
        return Optional.ofNullable(jpaRepository.countActiveSkus()).orElse(0);
    }

    // --- Mappers ---

    private ProductoJpaEntity toEntity(Producto domain) {
        return ProductoJpaEntity.builder()
                .skuId(domain.getSkuId())
                .marca(domain.getMarca())
                .presentacion(domain.getPresentacion())
                .contenidoMl(domain.getContenidoMl())
                .pesoLogisticoKg(domain.getPesoLogisticoKg())
                .creadoEl(domain.getCreadoEl())
                .build();
    }

    private Producto toDomain(ProductoJpaEntity entity) {
        return Producto.builder()
                .skuId(entity.getSkuId())
                .marca(entity.getMarca())
                .presentacion(entity.getPresentacion())
                .contenidoMl(entity.getContenidoMl())
                .pesoLogisticoKg(entity.getPesoLogisticoKg())
                .creadoEl(entity.getCreadoEl())
                .build();
    }
}
