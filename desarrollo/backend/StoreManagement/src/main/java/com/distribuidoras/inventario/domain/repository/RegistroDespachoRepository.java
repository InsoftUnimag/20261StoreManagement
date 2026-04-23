package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.RegistroDespacho;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for RegistroDespacho.
 */
public interface RegistroDespachoRepository {
    
    /**
     * Guarda un registro de despacho.
     * 
     * @param registro El registro a guardar.
     * @return El registro guardado.
     */
    RegistroDespacho save(RegistroDespacho registro);
    
    /**
     * Busca un registro de despacho por el ID de pedido.
     * 
     * @param pedidoId El ID del pedido asociado.
     * @return Opcionalmente el registro encontrado.
     */
    Optional<RegistroDespacho> findByPedidoId(UUID pedidoId);
}
