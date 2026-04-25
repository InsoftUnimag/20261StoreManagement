package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.Operario;
import java.util.List;
import java.util.Optional;

/**
 * Port for Operario consultation from external user module.
 */
public interface OperarioServicePort {
    
    /**
     * Find operario (operario picking, supervisor, etc) by CC/NIT.
     * @param cedula CC/NIT del operario
     * @return Optional con datos del operario
     */
    Optional<Operario> findByCedula(String cedula);
    
    /**
     * Find all operarios by rol.
     * @param rol Rol del operario (OPERARIO_PICKING, OPERARIO_DESPACHO, etc)
     * @return Lista de operarios
     */
    List<Operario> findByRol(String rol);
}