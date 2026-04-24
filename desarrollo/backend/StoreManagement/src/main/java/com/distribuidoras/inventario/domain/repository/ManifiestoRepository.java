package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.Manifiesto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManifiestoRepository {
    Optional<Manifiesto> findById(UUID id);
    List<Manifiesto> findPendientes();
    List<Manifiesto> findAll();
    List<Manifiesto> findByFechaEmisionBetween(LocalDate desde, LocalDate hasta);
    List<Manifiesto> findByFechaEmisionBetweenWithLimit(LocalDate desde, LocalDate hasta, int limit);
    Manifiesto save(Manifiesto m);
}
