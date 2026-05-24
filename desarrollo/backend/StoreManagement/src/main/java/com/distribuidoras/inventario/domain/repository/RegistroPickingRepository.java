package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.RegistroPicking;

import java.util.Optional;

/**
 * Repository interface for RegistroPicking.
 */
public interface RegistroPickingRepository {

    /**
     * Guarda un registro de picking.
     * 
     * @param registro El registro a guardar.
     * @return El registro guardado.
     */
    RegistroPicking save(RegistroPicking registro);

    /**
     * Busca un registro de picking por el ID de pedido.
     * 
     * @param pedidoId El ID del pedido asociado.
     * @return Opcionalmente el registro encontrado.
     */
    Optional<RegistroPicking> findByPedidoId(Long pedidoId);
}
