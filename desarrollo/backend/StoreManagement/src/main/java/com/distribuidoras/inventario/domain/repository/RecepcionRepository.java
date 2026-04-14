package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.Recepcion;

import java.util.Optional;
import java.util.UUID;

public interface RecepcionRepository {
    Recepcion save(Recepcion recepcion);
    Optional<Recepcion> findById(UUID recepcionId);
}
