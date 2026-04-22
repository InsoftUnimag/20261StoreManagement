package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.LoteJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.LoteJpaRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.Objects;
import java.util.*;

@Component
public class LoteRepositoryAdapter implements LoteRepository {
    private final LoteJpaRepository jpa;
    public LoteRepositoryAdapter(LoteJpaRepository jpa) { this.jpa = jpa; }

    @Override public Lote save(Lote l) { return toDomain(jpa.save(Objects.requireNonNull(toEntity(l)))); }
    @Override public Optional<Lote> findById(String id) { return jpa.findById(Objects.requireNonNull(id)).map(this::toDomain); }
    @Override public Optional<Lote> findBySkuIdAndCodigoLoteAndFechaVencimiento(String skuId, String codigo, LocalDate fv) {
        return jpa.findBySkuIdAndCodigoLoteAndFechaVencimiento(skuId, codigo, fv).map(this::toDomain);
    }
    @Override public List<Lote> findBySkuIdOrderByFechaVencimientoAsc(String skuId) {
        return jpa.findBySkuIdOrderByFechaVencimientoAsc(skuId).stream().map(this::toDomain).toList();
    }
    @Override public List<Lote> findBySkuIdWithStock(String skuId) {
        return jpa.findAvailableBySkuOrderByFEFO(skuId).stream().map(this::toDomain).toList();
    }
    @Override public boolean existsBySkuIdAndCantidadGreaterThan(String skuId, int min) {
        return jpa.existsBySkuIdAndCantidadGreaterThan(skuId, min);
    }

    @Override
    public Map<String, List<Lote>> findBySkuIdsWithStock(List<String> skuIds) {
        // Functional approach: group by SKU ID using collectors
        return jpa.findBySkuIdsWithStock(skuIds).stream()
                .map(this::toDomain)
                .collect(java.util.stream.Collectors.groupingBy(
                        Lote::getSkuId, 
                        java.util.LinkedHashMap::new, 
                        java.util.stream.Collectors.toList()));
    }
    
    @Override
    public Integer countLotesExpiringWithinDays(int daysFromNow) {
        LocalDate fechaLimite = LocalDate.now().plusDays(daysFromNow);
        return Optional.ofNullable(jpa.countLotesExpiringWithinDays(fechaLimite)).orElse(0);
    }
    
    @Override
    public List<Lote> findLotesCriticos(int daysFromNow) {
        LocalDate fechaLimite = LocalDate.now().plusDays(daysFromNow);
        return jpa.findLotesCriticos(fechaLimite).stream().map(this::toDomain).toList();
    }
    
    @Override
    public Integer countLotesWithStock() {
        return Optional.ofNullable(jpa.countLotesWithStock()).orElse(0);
    }
    
    @Override
    public Integer sumTotalStock() {
        return Optional.ofNullable(jpa.sumTotalStock()).orElse(0);
    }

    @Override
    public List<Lote> findByFechaVencimientoBeforeAndCantidadGreaterThan(LocalDate fechaLimite, int cantidadMinima) {
        return jpa.findByFechaVencimientoBeforeAndCantidadGreaterThan(fechaLimite, cantidadMinima).stream().map(this::toDomain).toList();
    }

    private LoteJpaEntity toEntity(Lote l) {
        return LoteJpaEntity.builder()
                .codigoLote(l.getCodigoLote())
                .skuId(l.getSkuId())
                .cantidad(l.getCantidad())
                .fechaVencimiento(l.getFechaVencimiento())
                .fechaExpedicion(l.getFechaExpedicion())
                .disponible(l.getDisponible())
                .flagUrgenciaFefo(l.getFlagUrgenciaFefo())
                .costoUnitarioProducto(l.getCostoUnitarioProducto())
                .recepcionId(l.getRecepcionId())
                .creadoEl(l.getCreadoEl())
                .build();
    }
    
    private Lote toDomain(LoteJpaEntity e) {
        return Lote.builder()
                .codigoLote(e.getCodigoLote())
                .skuId(e.getSkuId())
                .cantidad(e.getCantidad())
                .fechaVencimiento(e.getFechaVencimiento())
                .fechaExpedicion(e.getFechaExpedicion())
                .disponible(e.getDisponible())
                .flagUrgenciaFefo(e.getFlagUrgenciaFefo())
                .costoUnitarioProducto(e.getCostoUnitarioProducto())
                .recepcionId(e.getRecepcionId())
                .creadoEl(e.getCreadoEl())
                .build();
    }
}
