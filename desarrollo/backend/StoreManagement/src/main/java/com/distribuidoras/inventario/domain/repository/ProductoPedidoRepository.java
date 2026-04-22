package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.ProductoPedido;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for ProductoPedido persistence.
 */
public interface ProductoPedidoRepository {
    
    List<ProductoPedido> saveAll(List<ProductoPedido> lineas);
    
    Optional<ProductoPedido> findById(UUID productoPedidoId);
    
    List<ProductoPedido> findByPedidoId(UUID pedidoId);
    
    void update(ProductoPedido productoPedido);
}
