package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ExcepcionNotFoundException;
import com.distribuidoras.inventario.domain.model.ExcepcionInventario;
import com.distribuidoras.inventario.domain.repository.ExcepcionInventarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Consultar Detalle de Excepción de Inventario.
 * T018 del Plan de Implementación.
 */
@Service
public class ConsultarDetalleExcepcionUseCase {

    private final ExcepcionInventarioRepository excepcionRepository;

    public ConsultarDetalleExcepcionUseCase(ExcepcionInventarioRepository excepcionRepository) {
        this.excepcionRepository = excepcionRepository;
    }

    @Transactional(readOnly = true)
    public ExcepcionInventario ejecutar(Long excepcionId) {
        return excepcionRepository.findById(excepcionId)
                .orElseThrow(() -> new ExcepcionNotFoundException(excepcionId));
    }
}
