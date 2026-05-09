package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.Recepcion;
import com.distribuidoras.inventario.domain.repository.RecepcionRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.RecepcionJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.RecepcionJpaRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.Objects;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RecepcionRepositoryAdapter implements RecepcionRepository {
    private final RecepcionJpaRepository jpa;

    public RecepcionRepositoryAdapter(RecepcionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Recepcion save(Recepcion r) {
        return toDomain(jpa.save(Objects.requireNonNull(toEntity(r))));
    }

    @Override
    public Optional<Recepcion> findById(UUID id) {
        return jpa.findById(Objects.requireNonNull(id)).map(this::toDomain);
    }

    @Override
    public Optional<Integer> findMaxNumeroRecepcionByFecha(LocalDate fecha) {
        return jpa.findMaxNumeroRecepcionByFecha(fecha);
    }

    @Override
    public List<Recepcion> findAll() {
        return jpa.findAllByOrderByFechaRecepcionDesc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private RecepcionJpaEntity toEntity(Recepcion r) {
        return RecepcionJpaEntity.builder()
                .recepcionId(r.getRecepcionId())
                .manifiestoId(r.getManifiestoId())
                .operarioId(r.getOperarioId())
                .fechaRecepcion(r.getFechaRecepcion())
                .notas(r.getNotas())
                .numeroRecepcion(r.getNumeroRecepcion())
                .build();
    }

    private Recepcion toDomain(RecepcionJpaEntity e) {
        return new Recepcion(e.getRecepcionId(), e.getManifiestoId(), e.getOperarioId(),
                e.getFechaRecepcion(), e.getNotas(), e.getNumeroRecepcion());
    }
}
