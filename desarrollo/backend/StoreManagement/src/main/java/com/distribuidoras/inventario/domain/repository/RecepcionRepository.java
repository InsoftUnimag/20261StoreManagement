package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.Recepcion;

import java.util.Optional;


public interface RecepcionRepository {
    Recepcion save(Recepcion recepcion);
    Optional<Recepcion> findById(Long recepcionId);
    Optional<Integer> findMaxNumeroRecepcionByFecha(java.time.LocalDate fecha);
    java.util.List<Recepcion> findAll();
}
