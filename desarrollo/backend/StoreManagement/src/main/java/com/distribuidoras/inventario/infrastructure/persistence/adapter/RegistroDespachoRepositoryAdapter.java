package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.RegistroDespacho;
import com.distribuidoras.inventario.domain.repository.RegistroDespachoRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.RegistroDespachoJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.RegistroDespachoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class RegistroDespachoRepositoryAdapter implements RegistroDespachoRepository {

    private final RegistroDespachoJpaRepository jpaRepository;

    public RegistroDespachoRepositoryAdapter(RegistroDespachoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RegistroDespacho save(RegistroDespacho registroDespacho) {
        RegistroDespachoJpaEntity entity = jpaRepository.save(Objects.requireNonNull(toEntity(registroDespacho)));
        return toDomain(entity);
    }

    @Override
    public Optional<RegistroDespacho> findByPedidoId(UUID pedidoId) {
        return jpaRepository.findByPedidoId(pedidoId).map(this::toDomain);
    }

    private RegistroDespachoJpaEntity toEntity(RegistroDespacho domain) {
        return RegistroDespachoJpaEntity.builder()
                .registroDespachoId(domain.getRegistroDespachoId())
                .pedidoId(domain.getPedidoId())
                .operarioId(domain.getOperarioId())
                .fechaDespacho(domain.getFechaDespacho())
                .transportista(domain.getTransportista())
                .placaVehiculo(domain.getPlacaVehiculo())
                .observaciones(domain.getObservaciones())
                .build();
    }

    private RegistroDespacho toDomain(RegistroDespachoJpaEntity entity) {
        return RegistroDespacho.builder()
                .registroDespachoId(entity.getRegistroDespachoId())
                .pedidoId(entity.getPedidoId())
                .operarioId(entity.getOperarioId())
                .fechaDespacho(entity.getFechaDespacho())
                .transportista(entity.getTransportista())
                .placaVehiculo(entity.getPlacaVehiculo())
                .observaciones(entity.getObservaciones())
                .build();
    }
}