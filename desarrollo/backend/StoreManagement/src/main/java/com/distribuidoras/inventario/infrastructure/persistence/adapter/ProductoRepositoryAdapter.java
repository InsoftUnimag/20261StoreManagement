package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.ProductoJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.ProductoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador que implementa el puerto ProductoRepository del dominio
 * delegando a Spring Data JPA.
 * Convierte entre ProductoJpaEntity <-> Producto (domain).
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
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
        ProductoJpaEntity saved = jpaRepository.save(Objects.requireNonNull(entity));
        return toDomain(saved);
    }

    @Override
    public Optional<Producto> findById(String skuId) {
        return jpaRepository.findById(Objects.requireNonNull(skuId)).map(this::toDomain);
    }

    @Override
    public List<Producto> findAll() {
        return jpaRepository.findAllActivos().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public org.springframework.data.domain.Page<Producto> findAllWithPagination(org.springframework.data.domain.Pageable pageable) {
        return jpaRepository.findAllActivos(Objects.requireNonNull(pageable)).map(this::toDomain);
    }

    @Override
    public List<Producto> findByBusqueda(String busqueda) {
        return jpaRepository
                .findByBusquedaAll(busqueda)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public org.springframework.data.domain.Page<Producto> findByBusquedaWithPagination(String busqueda, org.springframework.data.domain.Pageable pageable) {
        return jpaRepository
                .findByBusquedaAll(busqueda, pageable)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByMarcaAndPresentacion(String marca, String presentacion) {
        return jpaRepository.existsByMarcaAndPresentacion(marca, presentacion);
    }

    @Override
    public boolean existsByMarcaAndPresentacionAndSkuIdNot(String marca, String presentacion, String skuId) {
        return jpaRepository.existsByMarcaAndPresentacionAndSkuIdNot(marca, presentacion, skuId);
    }

    @Override
    public void deleteById(String skuId) {
        jpaRepository.deleteById(Objects.requireNonNull(skuId));
    }
    
    @Override
    public Map<String, Producto> findByIds(List<String> skuIds) {
        // Functional approach: convert list to map
        return jpaRepository.findBySkuIdIn(skuIds).stream()
                .map(this::toDomain)
                .collect(Collectors.toMap(Producto::getSkuId, p -> p));
    }
    
    @Override
    public Integer countActiveSkus() {
        return Optional.ofNullable(jpaRepository.countActiveSkus()).orElse(0);
    }

    @Override
    public Optional<Integer> findMaxSkuNumero() {
        return Optional.ofNullable(jpaRepository.findMaxSkuNumero());
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
                .activo(domain.isActivo())
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
                .activo(entity.isActivo())
                .build();
    }
}
