package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.LoteComprometido;
import com.distribuidoras.inventario.domain.repository.LoteComprometidoRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.LoteComprometidoJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.LoteComprometidoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LoteComprometidoRepositoryAdapter implements LoteComprometidoRepository {
    
    private final LoteComprometidoJpaRepository jpa;
    
    public LoteComprometidoRepositoryAdapter(LoteComprometidoJpaRepository jpa) {
        this.jpa = jpa;
    }
    
    @Override
    public List<LoteComprometido> saveAll(List<LoteComprometido> compromisos) {
        return jpa.saveAll(Objects.requireNonNull(compromisos.stream().map(this::toEntity).toList())).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<LoteComprometido> findById(Long compromisoId) {
        return jpa.findById(Objects.requireNonNull(compromisoId)).map(this::toDomain);
    }
    
    @Override
    public List<LoteComprometido> findByProductoPedidoId(Long productoPedidoId) {
        return jpa.findByProductoPedidoId(productoPedidoId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoteComprometido> findByPedidoId(Long pedidoId) {
        return jpa.findByProductoPedidoId(pedidoId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoteComprometido> findByCodigoLote(String codigoLote) {
        return jpa.findByCodigoLote(codigoLote).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    private LoteComprometidoJpaEntity toEntity(LoteComprometido lc) {
        return LoteComprometidoJpaEntity.builder()
                .compromisoId(lc.getCompromisoId())
                .productoPedidoId(lc.getProductoPedidoId())
                .codigoLote(lc.getCodigoLote())
                .cantidadComprometida(lc.getCantidadComprometida())
                .fechaCompromiso(lc.getFechaCompromiso())
                .build();
    }
    
    private LoteComprometido toDomain(LoteComprometidoJpaEntity e) {
        return LoteComprometido.builder()
                .compromisoId(e.getCompromisoId())
                .productoPedidoId(e.getProductoPedidoId())
                .codigoLote(e.getCodigoLote())
                .cantidadComprometida(e.getCantidadComprometida())
                .fechaCompromiso(e.getFechaCompromiso())
                .build();
    }
}
