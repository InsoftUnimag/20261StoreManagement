package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.Manifiesto;
import com.distribuidoras.inventario.domain.model.enums.EstadoManifiesto;
import com.distribuidoras.inventario.domain.repository.ManifiestoRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.ManifiestoJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.ManifiestoJpaRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.Objects;
import java.util.*;

@Component
public class ManifiestoRepositoryAdapter implements ManifiestoRepository {
    private final ManifiestoJpaRepository jpa;
    public ManifiestoRepositoryAdapter(ManifiestoJpaRepository jpa) { this.jpa = jpa; }

    @Override public Optional<Manifiesto> findById(UUID id) { return jpa.findById(Objects.requireNonNull(id)).map(this::toDomain); }
    @Override public List<Manifiesto> findPendientes() {
        return jpa.findByEstadoInOrderByFechaEmisionAsc(List.of("PENDIENTE", "RECEPCIONADO_PARCIAL"))
                .stream().map(this::toDomain).toList();
    }
    @Override public List<Manifiesto> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }
    @Override public List<Manifiesto> findByFechaEmisionBetween(LocalDate desde, LocalDate hasta) {
        return jpa.findByFechaEmisionBetweenOrderByFechaEmisionDesc(desde, hasta).stream().map(this::toDomain).toList();
    }
    @Override public List<Manifiesto> findByFechaEmisionBetweenWithLimit(LocalDate desde, LocalDate hasta, int limit) {
        return jpa.findTop100ByFechaEmisionBetweenOrderByFechaEmisionDesc(desde, hasta).stream().map(this::toDomain).toList();
    }
    @Override public Optional<Integer> findMaxNumeroManifiestoByFecha(LocalDate fecha) {
        return jpa.findMaxNumeroManifiestoByFecha(fecha);
    }
    @Override public Manifiesto save(Manifiesto m) { return toDomain(jpa.save(Objects.requireNonNull(toEntity(m)))); }

    private ManifiestoJpaEntity toEntity(Manifiesto m) {
        return ManifiestoJpaEntity.builder().manifiestoId(m.getManifiestoId())
                .numeroManifiesto(m.getNumeroManifiesto()).fechaEmision(m.getFechaEmision())
                .proveedor(m.getProveedor()).estado(m.getEstado().name())
                .creadoEl(m.getCreadoEl())
                .build();
    }
    private Manifiesto toDomain(ManifiestoJpaEntity e) {
        return new Manifiesto(e.getManifiestoId(), e.getNumeroManifiesto(), e.getFechaEmision(),
                e.getProveedor(), EstadoManifiesto.valueOf(e.getEstado()), e.getCreadoEl());
    }
}
