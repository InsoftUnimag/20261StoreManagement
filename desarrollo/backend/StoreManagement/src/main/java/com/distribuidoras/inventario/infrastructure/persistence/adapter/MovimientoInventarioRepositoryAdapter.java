package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.MovimientoInventario;
import com.distribuidoras.inventario.domain.model.enums.TipoMovimiento;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.MovimientoInventarioRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.MovimientoInventarioJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.MovimientoInventarioJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Adapter for MovimientoInventarioRepository.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
@Component
public class MovimientoInventarioRepositoryAdapter implements MovimientoInventarioRepository {
    private final MovimientoInventarioJpaRepository jpa;
    private final LoteRepository loteRepository;
    
    public MovimientoInventarioRepositoryAdapter(MovimientoInventarioJpaRepository jpa, LoteRepository loteRepository) { 
        this.jpa = jpa;
        this.loteRepository = loteRepository;
    }

    @Override public MovimientoInventario save(MovimientoInventario m) { return toDomain(jpa.save(Objects.requireNonNull(toEntity(m)))); }
    @Override public List<MovimientoInventario> findByLoteId(String codigoLote) {
        return jpa.findByCodigoLoteOrderByFechaMovimientoDesc(codigoLote).stream().map(this::toDomain).toList();
    }
    
    @Override
    public List<MovimientoInventario> findByFilters(
            String skuId,
            String codigoLote,
            TipoMovimiento tipoMovimiento,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            int page,
            int size
    ) {
        // Functional approach: filter in memory after fetching
        List<MovimientoInventario> allMovimientos = jpa.findAll(
                PageRequest.of(0, 10000, Sort.by("fechaMovimiento").descending())
        ).getContent().stream()
                .map(this::toDomain)
                .toList();

        // Apply functional filters
        return allMovimientos.stream()
                .filter(m -> skuId == null || matchesSkuId(m, skuId))
                .filter(m -> codigoLote == null || m.getCodigoLote().equals(codigoLote))
                .filter(m -> tipoMovimiento == null || m.getTipoMovimiento() == tipoMovimiento)
                .filter(m -> fechaDesde == null || !m.getFechaMovimiento().isBefore(fechaDesde))
                .filter(m -> fechaHasta == null || !m.getFechaMovimiento().isAfter(fechaHasta))
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }
    
    @Override
    public Long countByFilters(
            String skuId,
            String codigoLote,
            TipoMovimiento tipoMovimiento,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    ) {
        // Functional approach: filter in memory after fetching
        List<MovimientoInventario> allMovimientos = jpa.findAll().stream()
                .map(this::toDomain)
                .toList();

        return allMovimientos.stream()
                .filter(m -> skuId == null || matchesSkuId(m, skuId))
                .filter(m -> codigoLote == null || m.getCodigoLote().equals(codigoLote))
                .filter(m -> tipoMovimiento == null || m.getTipoMovimiento() == tipoMovimiento)
                .filter(m -> fechaDesde == null || !m.getFechaMovimiento().isBefore(fechaDesde))
                .filter(m -> fechaHasta == null || !m.getFechaMovimiento().isAfter(fechaHasta))
                .count();
    }
    
    /**
     * Helper to check if a movement matches a SKU ID (requires joining with Lote).
     */
    private boolean matchesSkuId(MovimientoInventario mov, String skuId) {
        return loteRepository.findById(mov.getCodigoLote())
                .map(lote -> lote.getSkuId().equals(skuId))
                .orElse(false);
    }
    
    @Override
    public Integer countByTipoMovimientoAndFechaAfter(TipoMovimiento tipo, LocalDateTime fecha) {
        return Optional.ofNullable(jpa.countByTipoMovimientoAndFechaAfter(tipo.name(), fecha)).orElse(0);
    }

    private MovimientoInventarioJpaEntity toEntity(MovimientoInventario m) {
        return MovimientoInventarioJpaEntity.builder().movimientoId(m.getMovimientoId()).codigoLote(m.getCodigoLote())
                .tipoMovimiento(m.getTipoMovimiento().name()).cantidad(m.getCantidad())
                .fechaMovimiento(m.getFechaMovimiento()).pedidoId(m.getPedidoId())
                .excepcionId(m.getExcepcionId()).operarioId(m.getOperarioId()).observaciones(m.getObservaciones()).build();
    }
    private MovimientoInventario toDomain(MovimientoInventarioJpaEntity e) {
        return MovimientoInventario.builder()
                .movimientoId(e.getMovimientoId())
                .codigoLote(e.getCodigoLote())
                .tipoMovimiento(TipoMovimiento.valueOf(e.getTipoMovimiento()))
                .cantidad(e.getCantidad())
                .fechaMovimiento(e.getFechaMovimiento())
                .pedidoId(e.getPedidoId())
                .excepcionId(e.getExcepcionId())
                .operarioId(e.getOperarioId())
                .observaciones(e.getObservaciones())
                .build();
    }
}
