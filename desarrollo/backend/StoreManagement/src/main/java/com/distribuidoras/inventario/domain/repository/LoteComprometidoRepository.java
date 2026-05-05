package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.LoteComprometido;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for LoteComprometido persistence.
 */
public interface LoteComprometidoRepository {
    
    List<LoteComprometido> saveAll(List<LoteComprometido> compromisos);
    
    Optional<LoteComprometido> findById(UUID compromisoId);
    
    List<LoteComprometido> findByProductoPedidoId(UUID productoPedidoId);

    List<LoteComprometido> findByPedidoId(UUID pedidoId);

    List<LoteComprometido> findByCodigoLote(String codigoLote);
}
