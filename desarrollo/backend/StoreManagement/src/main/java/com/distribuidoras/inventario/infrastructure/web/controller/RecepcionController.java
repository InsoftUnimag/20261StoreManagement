package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.RegistrarRecepcionUseCase;
import com.distribuidoras.inventario.application.usecase.RegistrarRecepcionUseCase.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recepciones")
public class RecepcionController {

    private static final Logger log = LoggerFactory.getLogger(RecepcionController.class);
    private final RegistrarRecepcionUseCase registrarRecepcionUseCase;

    public RecepcionController(RegistrarRecepcionUseCase registrarRecepcionUseCase) {
        this.registrarRecepcionUseCase = registrarRecepcionUseCase;
    }

    /**
     * POST /api/v1/recepciones - Registrar recepción de mercancía.
     * Spec: 04_registrar_ingreso_productos.md
     */
    @PostMapping
    public ResponseEntity<RecepcionResult> registrarRecepcion(
            @Valid @RequestBody RecepcionRequestDto request) {
        log.info("POST /api/v1/recepciones - operario={}, líneas={}", request.operarioId, request.lineasRecepcion.size());

        List<LineaRecepcionCommand> lineas = request.lineasRecepcion.stream()
                .map(l -> new LineaRecepcionCommand(l.skuId, l.codigoLote, l.fechaVencimiento,
                        l.fechaFabricacion, l.cantidadRecibida))
                .toList();

        RecepcionCommand command = new RecepcionCommand(
                request.manifiestoId, request.operarioId, lineas, request.notas);

        RecepcionResult result = registrarRecepcionUseCase.ejecutar(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // --- Request DTO ---
    public static class RecepcionRequestDto {
        public UUID manifiestoId;
        @NotNull(message = "El operario es obligatorio")
        public UUID operarioId;
        @NotNull @Size(min = 1, message = "Debe incluir al menos una línea de recepción")
        public List<LineaRecepcionDto> lineasRecepcion;
        public String notas;
    }

    public static class LineaRecepcionDto {
        @NotNull(message = "El SKU es obligatorio")
        public UUID skuId;
        @NotBlank(message = "El código de lote es obligatorio")
        public String codigoLote;
        @NotNull(message = "La fecha de vencimiento es obligatoria")
        public LocalDate fechaVencimiento;
        public LocalDate fechaFabricacion;
        @Positive(message = "La cantidad debe ser mayor a cero")
        public int cantidadRecibida;
    }
}
