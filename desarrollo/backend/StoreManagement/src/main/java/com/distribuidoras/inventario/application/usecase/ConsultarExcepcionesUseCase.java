package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.ExcepcionInventario;
import com.distribuidoras.inventario.domain.model.enums.TipoExcepcion;
import com.distribuidoras.inventario.domain.repository.ExcepcionInventarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: Consultar Excepciones de Inventario.
 */
@Service
public class ConsultarExcepcionesUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarExcepcionesUseCase.class);

    private final ExcepcionInventarioRepository excepcionRepository;

    public ConsultarExcepcionesUseCase(ExcepcionInventarioRepository excepcionRepository) {
        this.excepcionRepository = excepcionRepository;
    }

    @Transactional(readOnly = true)
    public List<ExcepcionInventario> ejecutar(TipoExcepcion tipo, UUID skuId) {
        if (tipo == null && skuId == null) {
            return excepcionRepository.findAll();
        }
        return excepcionRepository.findByFilters(tipo, skuId);
    }
}
