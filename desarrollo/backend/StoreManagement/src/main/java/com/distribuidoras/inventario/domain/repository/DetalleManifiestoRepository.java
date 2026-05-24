package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.DetalleManifiesto;

import java.util.List;


public interface DetalleManifiestoRepository {
    List<DetalleManifiesto> findByManifiestoId(Long manifiestoId);
    DetalleManifiesto save(DetalleManifiesto detalle);
    java.util.Map<Long, List<DetalleManifiesto>> findByManifiestoIds(List<Long> manifiestoIds);
}
