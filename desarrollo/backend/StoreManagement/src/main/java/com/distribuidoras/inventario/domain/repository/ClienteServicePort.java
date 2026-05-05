package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.Cliente;

import java.util.Optional;

/**
 * Domain port for external User Module service.
 * FR-042: Client data consultation by CC/NIT
 */
public interface ClienteServicePort {
    
    /**
     * Find client by CC/NIT in external user module.
     * SC-021: Query time <= 2 seconds
     * 
     * @param cedula CC/NIT of the client
     * @return Client data if found
     */
    Optional<Cliente> findByCedula(String cedula);
}
