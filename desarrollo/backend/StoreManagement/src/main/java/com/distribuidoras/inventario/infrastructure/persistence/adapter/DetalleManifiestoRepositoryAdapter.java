package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.DetalleManifiesto;
import com.distribuidoras.inventario.domain.repository.DetalleManifiestoRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.DetalleManifiestoJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.DetalleManifiestoJpaRepository;
import org.springframework.stereotype.Component;
import java.util.Objects;
import java.util.*;

@Component
public class DetalleManifiestoRepositoryAdapter implements DetalleManifiestoRepository {
    private final DetalleManifiestoJpaRepository jpa;
    public DetalleManifiestoRepositoryAdapter(DetalleManifiestoJpaRepository jpa) { this.jpa = jpa; }

    @Override public List<DetalleManifiesto> findByManifiestoId(UUID id) {
        return jpa.findByManifiestoId(id).stream().map(this::toDomain).toList();
    }
    @Override public DetalleManifiesto save(DetalleManifiesto d) { return toDomain(jpa.save(Objects.requireNonNull(toEntity(d)))); }
    @Override public java.util.Map<UUID, List<DetalleManifiesto>> findByManifiestoIds(List<UUID> ids) {
        return jpa.findByManifiestoIdIn(ids).stream()
                .map(this::toDomain)
                .collect(java.util.stream.Collectors.groupingBy(DetalleManifiesto::getManifiestoId));
    }

    private DetalleManifiestoJpaEntity toEntity(DetalleManifiesto d) {
        return DetalleManifiestoJpaEntity.builder()
                .detalleId(d.getDetalleId())
                .manifiestoId(d.getManifiestoId())
                .skuId(d.getSkuId())
                .cantidadEsperada(d.getCantidadEsperada())
                .cantidadRecibida(d.getCantidadRecibida())
                .build();
    }
    private DetalleManifiesto toDomain(DetalleManifiestoJpaEntity e) {
        return DetalleManifiesto.builder()
                .detalleId(e.getDetalleId())
                .manifiestoId(e.getManifiestoId())
                .skuId(e.getSkuId())
                .cantidadEsperada(e.getCantidadEsperada())
                .cantidadRecibida(e.getCantidadRecibida())
                .build();
    }
}