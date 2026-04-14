package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.Recepcion;
import com.distribuidoras.inventario.domain.repository.RecepcionRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.RecepcionJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.RecepcionJpaRepository;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class RecepcionRepositoryAdapter implements RecepcionRepository {
    private final RecepcionJpaRepository jpa;
    public RecepcionRepositoryAdapter(RecepcionJpaRepository jpa) { this.jpa = jpa; }

    @Override public Recepcion save(Recepcion r) { return toDomain(jpa.save(toEntity(r))); }
    @Override public Optional<Recepcion> findById(UUID id) { return jpa.findById(id).map(this::toDomain); }

    private RecepcionJpaEntity toEntity(Recepcion r) {
        return RecepcionJpaEntity.builder().recepcionId(r.getRecepcionId()).manifiestoId(r.getManifiestoId())
                .operarioId(r.getOperarioId()).fechaRecepcion(r.getFechaRecepcion()).notas(r.getNotas()).build();
    }
    private Recepcion toDomain(RecepcionJpaEntity e) {
        return new Recepcion(e.getRecepcionId(), e.getManifiestoId(), e.getOperarioId(),
                e.getFechaRecepcion(), e.getNotas());
    }
}
