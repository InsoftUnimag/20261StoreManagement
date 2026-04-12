package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.DetalleManifiesto;

import java.util.List;
import java.util.UUID;

public interface DetalleManifiestoRepository {
    List<DetalleManifiesto> findByManifiestoId(UUID manifiestoId);
    DetalleManifiesto save(DetalleManifiesto detalle);
}
