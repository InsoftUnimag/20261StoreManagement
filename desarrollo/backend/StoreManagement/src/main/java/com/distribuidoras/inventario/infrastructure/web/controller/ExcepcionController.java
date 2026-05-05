package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.*;
import com.distribuidoras.inventario.domain.model.ExcepcionInventario;
import com.distribuidoras.inventario.domain.model.enums.TipoExcepcion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/excepciones")
public class ExcepcionController {

    private static final Logger log = LoggerFactory.getLogger(ExcepcionController.class);
    private final RegistrarExcepcionUseCase registrarExcepcionUseCase;
    private final ConsultarExcepcionesUseCase consultarExcepcionesUseCase;
    private final ConsultarDetalleExcepcionUseCase consultarDetalleUseCase;

    public ExcepcionController(RegistrarExcepcionUseCase registrarExcepcionUseCase,
                                ConsultarExcepcionesUseCase consultarExcepcionesUseCase,
                                ConsultarDetalleExcepcionUseCase consultarDetalleUseCase) {
        this.registrarExcepcionUseCase = registrarExcepcionUseCase;
        this.consultarExcepcionesUseCase = consultarExcepcionesUseCase;
        this.consultarDetalleUseCase = consultarDetalleUseCase;
    }

    /** POST /api/v1/excepciones - Registrar excepción de inventario */
    @PostMapping
    public ResponseEntity<RegistrarExcepcionUseCase.ExcepcionResultado> registrarExcepcion(
            @Valid @RequestBody ExcepcionRequestDto request) {
        log.info("POST /api/v1/excepciones - tipo={}, sku={}", request.tipoExcepcion, request.skuId);

        RegistrarExcepcionUseCase.ExcepcionCommand command = new RegistrarExcepcionUseCase.ExcepcionCommand(
                TipoExcepcion.valueOf(request.tipoExcepcion),
                request.skuId, request.codigoLote, request.cantidadAfectada,
                request.descripcion, request.evidenciaUrl, request.operarioId.toString());

        return ResponseEntity.status(HttpStatus.CREATED).body(registrarExcepcionUseCase.ejecutar(command));
    }

    /** GET /api/v1/excepciones - Consultar excepciones con filtros */
    @GetMapping
    public ResponseEntity<List<ExcepcionInventario>> consultarExcepciones(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String skuId) {
        log.info("GET /api/v1/excepciones - tipo={}, sku={}", tipo, skuId);

        TipoExcepcion tipoEnum = tipo != null ? TipoExcepcion.valueOf(tipo) : null;

        return ResponseEntity.ok(consultarExcepcionesUseCase.ejecutar(tipoEnum, skuId));
    }

    /** GET /api/v1/excepciones/{id} - Ver detalle de excepción */
    @GetMapping("/{id}")
    public ResponseEntity<ExcepcionInventario> consultarDetalle(@PathVariable UUID id) {
        log.info("GET /api/v1/excepciones/{}", id);
        return ResponseEntity.ok(consultarDetalleUseCase.ejecutar(id));
    }

    // --- Request DTOs ---

    public static class ExcepcionRequestDto {
        @NotBlank(message = "El tipo de excepción es obligatorio")
        public String tipoExcepcion;
        @NotBlank(message = "El SKU es obligatorio")
        public String skuId;
        public String codigoLote;
        @Positive(message = "La cantidad afectada debe ser mayor a cero")
        public int cantidadAfectada;
        @NotBlank(message = "La descripción es obligatoria")
        public String descripcion;
        public String evidenciaUrl;
        public UUID operarioId;
    }
}
