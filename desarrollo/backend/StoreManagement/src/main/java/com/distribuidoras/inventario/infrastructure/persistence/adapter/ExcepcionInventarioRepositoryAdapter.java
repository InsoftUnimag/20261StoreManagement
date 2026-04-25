package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.ExcepcionInventario;
import com.distribuidoras.inventario.domain.model.enums.*;
import com.distribuidoras.inventario.domain.repository.ExcepcionInventarioRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.ExcepcionInventarioJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.ExcepcionInventarioJpaRepository;
import org.springframework.stereotype.Component;
import java.util.Objects;
import java.util.*;

@Component
public class ExcepcionInventarioRepositoryAdapter implements ExcepcionInventarioRepository {
    private final ExcepcionInventarioJpaRepository jpa;
    public ExcepcionInventarioRepositoryAdapter(ExcepcionInventarioJpaRepository jpa) { this.jpa = jpa; }

    @Override public ExcepcionInventario save(ExcepcionInventario e) { return toDomain(jpa.save(Objects.requireNonNull(toEntity(e)))); }
    @Override public Optional<ExcepcionInventario> findById(UUID id) { return jpa.findById(Objects.requireNonNull(id)).map(this::toDomain); }
    @Override public List<ExcepcionInventario> findAll() { return jpa.findAll().stream().map(this::toDomain).toList(); }
    @Override public List<ExcepcionInventario> findByFilters(TipoExcepcion tipo, String skuId) {
        if (tipo != null) {
            return jpa.findByTipoExcepcion(tipo.name()).stream().map(this::toDomain).toList();
        } else if (skuId != null) {
            return jpa.findBySkuId(skuId).stream().map(this::toDomain).toList();
        }
        return findAll();
    }

    @Override
    public List<ExcepcionInventario> findByCodigoLote(String codigoLote) {
        return jpa.findByCodigoLote(codigoLote).stream().map(this::toDomain).toList();
    }
    
    @Override
    public Integer countOpenExceptions() {
        return Optional.ofNullable(jpa.countOpenExceptions()).orElse(0);
    }

    private ExcepcionInventarioJpaEntity toEntity(ExcepcionInventario e) {
        return ExcepcionInventarioJpaEntity.builder().excepcionId(e.getExcepcionId())
                .tipoExcepcion(e.getTipoExcepcion().name()).codigoLote(e.getCodigoLote()).skuId(e.getSkuId())
                .cantidadAfectada(e.getCantidadAfectada()).fechaRegistro(e.getFechaRegistro())
                .operarioId(e.getOperarioId()).descripcion(e.getDescripcion())
                .evidenciaUrl(e.getEvidenciaUrl()).build();
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
