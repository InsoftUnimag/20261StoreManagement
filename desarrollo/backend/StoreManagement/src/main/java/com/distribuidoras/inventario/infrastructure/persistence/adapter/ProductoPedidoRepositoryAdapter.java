package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.ProductoPedido;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.ProductoPedidoJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.ProductoPedidoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductoPedidoRepositoryAdapter implements ProductoPedidoRepository {

    private final ProductoPedidoJpaRepository jpa;

    public ProductoPedidoRepositoryAdapter(ProductoPedidoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<ProductoPedido> saveAll(List<ProductoPedido> lineas) {
        return jpa.saveAll(Objects.requireNonNull(lineas.stream().map(this::toEntity).toList())).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductoPedido> findById(Long productoPedidoId) {
        return jpa.findById(Objects.requireNonNull(productoPedidoId)).map(this::toDomain);
    }

    @Override
    public List<ProductoPedido> findByPedidoId(Long pedidoId) {
        return jpa.findByPedidoId(Objects.requireNonNull(pedidoId)).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void update(ProductoPedido productoPedido) {
        jpa.save(Objects.requireNonNull(toEntity(productoPedido)));
    }

    private ProductoPedidoJpaEntity toEntity(ProductoPedido pp) {
        return ProductoPedidoJpaEntity.builder()
                .productoPedidoId(pp.getProductoPedidoId())
                .pedidoId(pp.getPedidoId())
                .skuId(pp.getSkuId())
                .cantidadSolicitada(pp.getCantidadSolicitada())
                .cantidadConfirmada(pp.getCantidadConfirmada())
                .precioUnitario(pp.getPrecioUnitario())
                .build();
    }

    private ProductoPedido toDomain(ProductoPedidoJpaEntity e) {
        return ProductoPedido.builder()
                .productoPedidoId(e.getProductoPedidoId())
                .pedidoId(e.getPedidoId())
                .skuId(e.getSkuId())
                .cantidadSolicitada(e.getCantidadSolicitada())
                .cantidadConfirmada(e.getCantidadConfirmada())
                .precioUnitario(e.getPrecioUnitario())
                .build();
    }
}
