package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.BitacoraProducto;

import java.util.List;

/**
 * Puerto del dominio para persistencia de BitacoraProducto.
 * Formato skuIdRef: SKU-001, SKU-012, SKU-111, etc.
 */
public interface BitacoraProductoRepository {
 
    BitacoraProducto save(BitacoraProducto bitacora);
    
    List<BitacoraProducto> findBySkuIdRef(String skuIdRef);
}
