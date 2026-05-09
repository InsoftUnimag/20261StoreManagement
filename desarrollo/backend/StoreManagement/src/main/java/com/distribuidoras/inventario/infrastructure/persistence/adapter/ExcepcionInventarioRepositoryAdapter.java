package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.ExcepcionInventario;
import com.distribuidoras.inventario.domain.model.enums.TipoExcepcion;
import com.distribuidoras.inventario.domain.repository.ExcepcionInventarioRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.ExcepcionInventarioJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.ExcepcionInventarioJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class ExcepcionInventarioRepositoryAdapter implements ExcepcionInventarioRepository {
    private final ExcepcionInventarioJpaRepository jpa;

    public ExcepcionInventarioRepositoryAdapter(ExcepcionInventarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ExcepcionInventario save(ExcepcionInventario e) {
        return toDomain(jpa.save(Objects.requireNonNull(toEntity(e))));
    }

    @Override
    public Optional<ExcepcionInventario> findById(UUID id) {
        return jpa.findById(Objects.requireNonNull(id)).map(this::toDomain);
    }

    @Override
    public java.util.List<ExcepcionInventario> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public java.util.List<ExcepcionInventario> findByFilters(TipoExcepcion tipo, String skuId) {
        if (tipo != null) {
            return jpa.findByTipoExcepcion(tipo.name()).stream().map(this::toDomain).toList();
        } else if (skuId != null) {
            return jpa.findBySkuId(skuId).stream().map(this::toDomain).toList();
        }
        return findAll();
    }

    @Override
    public java.util.List<ExcepcionInventario> findByCodigoLote(String codigoLote) {
        return jpa.findByCodigoLote(codigoLote).stream().map(this::toDomain).toList();
    }

    @Override
    public java.util.List<ExcepcionInventario> findByFechaRegistroAfter(LocalDateTime fecha) {
        return jpa.findByFechaRegistroAfter(fecha).stream().map(this::toDomain).toList();
    }

    @Override
    public Page<ExcepcionInventario> findByFiltersWithPagination(
            TipoExcepcion tipo, String skuId,
            LocalDateTime desde, LocalDateTime hasta,
            Pageable pageable) {
        String tipoStr = tipo != null ? tipo.name() : null;
        Page<ExcepcionInventarioJpaEntity> result = jpa.findByFiltersWithPagination(
                tipoStr, skuId, desde, hasta, pageable);
        return result.map(this::toDomain);
    }

    @Override
    public Integer countOpenExceptions() {
        return Optional.ofNullable(jpa.countOpenExceptions()).orElse(0);
    }

    private ExcepcionInventarioJpaEntity toEntity(ExcepcionInventario e) {
        return ExcepcionInventarioJpaEntity.builder()
                .excepcionId(e.getExcepcionId())
                .tipoExcepcion(e.getTipoExcepcion().name())
                .codigoLote(e.getCodigoLote())
                .skuId(e.getSkuId())
                .cantidadAfectada(e.getCantidadAfectada())
                .fechaRegistro(e.getFechaRegistro())
                .operarioId(e.getOperarioId())
                .descripcion(e.getDescripcion())
                .evidenciaUrl(e.getEvidenciaUrl())
                .build();
    }

    private ExcepcionInventario toDomain(ExcepcionInventarioJpaEntity e) {
        return ExcepcionInventario.builder()
                .excepcionId(e.getExcepcionId())
                .tipoExcepcion(TipoExcepcion.valueOf(e.getTipoExcepcion()))
                .codigoLote(e.getCodigoLote())
                .skuId(e.getSkuId())
                .cantidadAfectada(e.getCantidadAfectada())
                .fechaRegistro(e.getFechaRegistro())
                .operarioId(e.getOperarioId())
                .descripcion(e.getDescripcion())
                .evidenciaUrl(e.getEvidenciaUrl())
                .build();
    }
}