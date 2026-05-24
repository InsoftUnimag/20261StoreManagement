package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.ProductoPedido;

import java.util.List;
import java.util.Optional;

/**
 * Domain port for ProductoPedido persistence.
 */
public interface ProductoPedidoRepository {

    List<ProductoPedido> saveAll(List<ProductoPedido> lineas);

    Optional<ProductoPedido> findById(Long productoPedidoId);

    List<ProductoPedido> findByPedidoId(Long pedidoId);

    void update(ProductoPedido productoPedido);
}
