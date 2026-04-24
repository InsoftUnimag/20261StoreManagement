package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.ListarManifiestosUseCase;
import com.distribuidoras.inventario.application.usecase.ListarManifiestosUseCase.*;
import com.distribuidoras.inventario.application.usecase.ListarManifiestosPendientesUseCase;
import com.distribuidoras.inventario.application.usecase.ListarManifiestosPendientesUseCase.ManifiestoResumen;
import com.distribuidoras.inventario.application.usecase.ConsultarDetallesManifiestoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for manifest listing.
 * Spec 12: Listar Manifiesto
 */
@RestController
@RequestMapping("/api/v1/manifiestos")
public class ManifiestoController {

    private static final Logger log = LoggerFactory.getLogger(ManifiestoController.class);

    private final ListarManifiestosUseCase listarManifiestosUseCase;
    private final ListarManifiestosPendientesUseCase listarManifiestosPendientesUseCase;
    private final ConsultarDetallesManifiestoUseCase consultarDetallesManifiestoUseCase;

    public ManifiestoController(ListarManifiestosUseCase listarManifiestosUseCase,
                                 ListarManifiestosPendientesUseCase listarManifiestosPendientesUseCase,
                                 ConsultarDetallesManifiestoUseCase consultarDetallesManifiestoUseCase) {
        this.listarManifiestosUseCase = listarManifiestosUseCase;
        this.listarManifiestosPendientesUseCase = listarManifiestosPendientesUseCase;
        this.consultarDetallesManifiestoUseCase = consultarDetallesManifiestoUseCase;
    }

    /**
     * GET /api/v1/manifiestos
     * Listar manifiestos con filtros opcionales.
     * GAP-08: Parametro incluirHistorico para ver historial completo (supervisor) o solo pendientes (operario).
     */
    @GetMapping
    public ResponseEntity<List<ManifiestoResumenDTO>> listarManifiestos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "false") boolean incluirHistorico) {
        
        log.info("REST: Listando manifiestos con filtros - incluirHistorico={}", incluirHistorico);
        
        FiltrosManifiestoDTO filtros = new FiltrosManifiestoDTO(fechaDesde, fechaHasta, estado, incluirHistorico);
        List<ManifiestoResumenDTO> manifiestos = listarManifiestosUseCase.ejecutar(filtros);
        
        if (manifiestos.isEmpty()) {
            log.warn("No se encontraron manifiestos con los filtros aplicados");
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(manifiestos);
    }

    /**
     * GET /api/v1/manifiestos/{id}/detalles
     * Obtener detalle completo de un manifiesto.
     */
    @GetMapping("/{id}/detalles")
    public ResponseEntity<ManifiestoDetalleDTO> obtenerDetalleManifiesto(@PathVariable String id) {
        log.info("REST: Obteniendo detalle del manifiesto {}", id);
        
        ManifiestoDetalleDTO detalle = listarManifiestosUseCase.obtenerDetalle(UUID.fromString(id));
        
        return ResponseEntity.ok(detalle);
    }

    /**
     * GET /api/v1/manifiestos/pendientes
     * Listar manifiestos pendientes de recepción (Spec 04).
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<ManifiestoResumen>> listarManifiestosPendientes() {
        log.info("REST: Listando manifiestos pendientes");
        
        List<ManifiestoResumen> pendientes = listarManifiestosPendientesUseCase.ejecutar();
        
        if (pendientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(pendientes);
    }

    /**
     * GET /api/v1/manifiestos/{id}/detalle-completo
     * Consultar detalles completos de un manifiesto específico (Spec 04).
     */
    @GetMapping("/{id}/detalle-completo")
    public ResponseEntity<Object> consultarDetallesManifiesto(@PathVariable String id) {
        log.info("REST: Consultando detalles completos del manifiesto {}", id);
        
        var detalles = consultarDetallesManifiestoUseCase.ejecutar(UUID.fromString(id));
        
        return ResponseEntity.ok(detalles);
    }
}
