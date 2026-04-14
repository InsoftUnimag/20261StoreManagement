package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.DetalleManifiesto;
import com.distribuidoras.inventario.domain.repository.DetalleManifiestoRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.DetalleManifiestoJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.DetalleManifiestoJpaRepository;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class DetalleManifiestoRepositoryAdapter implements DetalleManifiestoRepository {
    private final DetalleManifiestoJpaRepository jpa;
    public DetalleManifiestoRepositoryAdapter(DetalleManifiestoJpaRepository jpa) { this.jpa = jpa; }

    @Override public List<DetalleManifiesto> findByManifiestoId(UUID id) {
        return jpa.findByManifiestoId(id).stream().map(this::toDomain).toList();
    }
    @Override public DetalleManifiesto save(DetalleManifiesto d) { return toDomain(jpa.save(toEntity(d))); }

    private DetalleManifiestoJpaEntity toEntity(DetalleManifiesto d) {
        return DetalleManifiestoJpaEntity.builder().detalleId(d.getDetalleId()).manifiestoId(d.getManifiestoId())
                .skuId(d.getSkuId()).cantidadEsperada(d.getCantidadEsperada()).cantidadRecibida(d.getCantidadRecibida()).build();
    }
    private DetalleManifiesto toDomain(DetalleManifiestoJpaEntity e) {
        return new DetalleManifiesto(e.getDetalleId(), e.getManifiestoId(), e.getSkuId(),
                e.getCantidadEsperada(), e.getCantidadRecibida());
    }
}
