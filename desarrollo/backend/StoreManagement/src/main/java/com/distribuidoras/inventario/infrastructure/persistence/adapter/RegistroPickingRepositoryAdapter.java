package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.RegistroPicking;
import com.distribuidoras.inventario.domain.repository.RegistroPickingRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.RegistroPickingJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.RegistroPickingJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class RegistroPickingRepositoryAdapter implements RegistroPickingRepository {

    private final RegistroPickingJpaRepository jpaRepository;

    public RegistroPickingRepositoryAdapter(RegistroPickingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RegistroPicking save(RegistroPicking registroPicking) {
        RegistroPickingJpaEntity entity = jpaRepository.save(Objects.requireNonNull(toEntity(registroPicking)));
        return toDomain(entity);
    }

    @Override
    public Optional<RegistroPicking> findByPedidoId(Long pedidoId) {
        return jpaRepository.findByPedidoId(pedidoId).map(this::toDomain);
    }

    private RegistroPickingJpaEntity toEntity(RegistroPicking domain) {
        return RegistroPickingJpaEntity.builder()
                .registroPickingId(domain.getRegistroPickingId())
                .pedidoId(domain.getPedidoId())
                .operarioId(domain.getOperarioId())
                .fechaPicking(domain.getFechaPicking())
                .observaciones(domain.getObservaciones())
                .build();
    }

    private RegistroPicking toDomain(RegistroPickingJpaEntity entity) {
        return RegistroPicking.builder()
                .registroPickingId(entity.getRegistroPickingId())
                .pedidoId(entity.getPedidoId())
                .operarioId(entity.getOperarioId())
                .fechaPicking(entity.getFechaPicking())
                .observaciones(entity.getObservaciones())
                .build();
    }
}