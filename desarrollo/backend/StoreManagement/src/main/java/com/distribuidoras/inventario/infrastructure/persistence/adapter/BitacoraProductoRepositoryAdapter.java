package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.BitacoraProducto;
import com.distribuidoras.inventario.domain.repository.BitacoraProductoRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.BitacoraProductoJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.BitacoraProductoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Adaptador que implementa el puerto BitacoraProductoRepository del dominio.
 * Formato skuIdRef: SKU-001, SKU-012, SKU-111, etc.
 */
@Component
public class BitacoraProductoRepositoryAdapter implements BitacoraProductoRepository {

    private final BitacoraProductoJpaRepository jpaRepository;

    public BitacoraProductoRepositoryAdapter(BitacoraProductoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BitacoraProducto save(BitacoraProducto bitacora) {
        BitacoraProductoJpaEntity entity = toEntity(bitacora);
        BitacoraProductoJpaEntity saved = jpaRepository.save(Objects.requireNonNull(entity));
        return toDomain(saved);
    }

    @Override
    public List<BitacoraProducto> findBySkuIdRef(String skuIdRef) {
        return jpaRepository.findBySkuIdRefOrderByFechaDesc(skuIdRef).stream()
                .map(this::toDomain)
                .toList();
    }

    // --- Mappers ---

    private BitacoraProductoJpaEntity toEntity(BitacoraProducto domain) {
        return BitacoraProductoJpaEntity.builder()
                .id(domain.getId())
                .skuIdRef(domain.getSkuIdRef())
                .campo(domain.getCampo())
                .valorAnterior(domain.getValorAnterior())
                .valorNuevo(domain.getValorNuevo())
                .descripcion(domain.getDescripcion())
                .fecha(domain.getFecha())
                .usuario(domain.getUsuario())
                .build();
    }

    private BitacoraProducto toDomain(BitacoraProductoJpaEntity entity) {
        return new BitacoraProducto(
                entity.getId(),
                entity.getSkuIdRef(),
                entity.getCampo(),
                entity.getValorAnterior(),
                entity.getValorNuevo(),
                entity.getDescripcion(),
                entity.getFecha(),
                entity.getUsuario()
        );
    }
}
