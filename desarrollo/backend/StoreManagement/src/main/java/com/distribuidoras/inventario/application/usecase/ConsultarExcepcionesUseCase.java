package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.ExcepcionInventario;
import com.distribuidoras.inventario.domain.model.enums.TipoExcepcion;
import com.distribuidoras.inventario.domain.repository.ExcepcionInventarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Caso de uso: Consultar Excepciones de Inventario.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
@Service
public class ConsultarExcepcionesUseCase {

    private final ExcepcionInventarioRepository excepcionRepository;

    public ConsultarExcepcionesUseCase(ExcepcionInventarioRepository excepcionRepository) {
        this.excepcionRepository = excepcionRepository;
    }

    @Transactional(readOnly = true)
    public List<ExcepcionInventario> ejecutar(TipoExcepcion tipo, String skuId) {
        if (tipo == null && skuId == null) {
            return excepcionRepository.findAll();
        }
        return excepcionRepository.findByFilters(tipo, skuId);
    }

    @Transactional(readOnly = true)
    public List<ExcepcionInventario> ejecutar(LocalDateTime desde) {
        return excepcionRepository.findByFechaRegistroAfter(desde);
    }

    public List<ExcepcionInventario> ejecutar(int minutos) {
        LocalDateTime desde = LocalDateTime.now().minusMinutes(minutos);
        return ejecutar(desde);
    }

    @Transactional(readOnly = true)
    public Page<ExcepcionInventario> ejecutarConPaginacion(
            TipoExcepcion tipo, String skuId,
            LocalDateTime desde, LocalDateTime hasta,
            Pageable pageable) {
        return excepcionRepository.findByFiltersWithPagination(tipo, skuId, desde, hasta, pageable);
    }
}
