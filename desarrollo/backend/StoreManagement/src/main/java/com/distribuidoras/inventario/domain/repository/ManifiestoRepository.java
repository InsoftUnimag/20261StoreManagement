package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.Manifiesto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManifiestoRepository {
    Optional<Manifiesto> findById(UUID manifiestoId);
    List<Manifiesto> findPendientes();
    Manifiesto save(Manifiesto manifiesto);
}
