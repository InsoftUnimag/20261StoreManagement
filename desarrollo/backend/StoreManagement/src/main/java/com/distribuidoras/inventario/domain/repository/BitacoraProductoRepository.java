package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.BitacoraProducto;

import java.util.List;
import java.util.UUID;

/**
 * Puerto del dominio para persistencia de BitacoraProducto.
 */
public interface BitacoraProductoRepository {

    BitacoraProducto save(BitacoraProducto bitacora);

    List<BitacoraProducto> findBySkuIdRef(UUID skuIdRef);
}
