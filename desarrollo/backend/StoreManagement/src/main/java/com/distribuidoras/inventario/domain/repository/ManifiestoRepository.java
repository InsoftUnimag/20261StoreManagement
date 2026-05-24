package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.Manifiesto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ManifiestoRepository {
    Optional<Manifiesto> findById(Long id);
    List<Manifiesto> findPendientes();
    List<Manifiesto> findAll();
    List<Manifiesto> findByFechaEmisionBetween(LocalDate desde, LocalDate hasta);
    List<Manifiesto> findByFechaEmisionBetweenWithLimit(LocalDate desde, LocalDate hasta, int limit);
    Optional<Integer> findMaxNumeroManifiestoByFecha(LocalDate fecha);
    Manifiesto save(Manifiesto m);
}
