package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.*;
import com.distribuidoras.inventario.domain.model.enums.TipoExcepcion;
import com.distribuidoras.inventario.infrastructure.web.dto.ExcepcionRequestDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.ExcepcionResponseDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/excepciones")
public class ExcepcionController {

    private static final Logger log = LoggerFactory.getLogger(ExcepcionController.class);
    private final RegistrarExcepcionUseCase registrarExcepcionUseCase;
    private final ConsultarExcepcionesUseCase consultarExcepcionesUseCase;
    private final ConsultarDetalleExcepcionUseCase consultarDetalleUseCase;
    private volatile LocalDateTime ultimaExcepcionNotificada = LocalDateTime.MIN;

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
            @Valid @RequestBody ExcepcionRequestDTO request) {
        log.info("POST /api/v1/excepciones - tipo={}, sku={}", request.getTipoExcepcion(), request.getSkuId());

        RegistrarExcepcionUseCase.ExcepcionCommand command = new RegistrarExcepcionUseCase.ExcepcionCommand(
                TipoExcepcion.valueOf(request.getTipoExcepcion()),
                request.getSkuId(), request.getCodigoLote(), request.getCantidadAfectada(),
                request.getDescripcion(), request.getEvidenciaUrl(),
                request.getOperarioId() != null ? request.getOperarioId().toString() : null);

        RegistrarExcepcionUseCase.ExcepcionResultado resultado = registrarExcepcionUseCase.ejecutar(command);

        ultimaExcepcionNotificada = LocalDateTime.now();

        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    /** GET /api/v1/excepciones - Listar con filtros y paginación */
    @GetMapping
    public ResponseEntity<Page<ExcepcionResponseDTO>> consultarExcepciones(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String skuId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) LocalDateTime desde,
            @RequestParam(required = false) LocalDateTime hasta) {
        log.info("GET /api/v1/excepciones - tipo={}, sku={}, page={}", tipo, skuId, page);

        TipoExcepcion tipoEnum = (tipo != null && !tipo.isBlank())
                ? TipoExcepcion.valueOf(tipo.toUpperCase())
                : null;

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaRegistro"));

        Page<ExcepcionResponseDTO> excepciones = consultarExcepcionesUseCase.ejecutarConPaginacion(
                tipoEnum, skuId, desde, hasta, pageRequest);

        return ResponseEntity.ok(excepciones);
    }

    /** GET /api/v1/excepciones/ultimas - Excepciones de los últimos N minutos (para polling) */
    @GetMapping("/ultimas")
    public ResponseEntity<List<ExcepcionResponseDTO>> consultarUltimas(
            @RequestParam(defaultValue = "60") int minutos) {
        log.info("GET /api/v1/excepciones/ultimas - ultimos={} minutos", minutos);

        List<ExcepcionResponseDTO> excepciones = consultarExcepcionesUseCase.ejecutar(minutos);
        return ResponseEntity.ok(excepciones);
    }

    /** GET /api/v1/excepciones/nuevas - Excepciones nuevas desde última consulta (long polling) */
    @GetMapping("/nuevas")
    public ResponseEntity<List<ExcepcionResponseDTO>> consultarNuevas(
            @RequestParam(required = false) String desde) {
        log.info("GET /api/v1/excepciones/nuevas - ultimaNotificada={}", ultimaExcepcionNotificada);

        LocalDateTime desdeTiempo = desde != null
                ? LocalDateTime.parse(desde)
                : ultimaExcepcionNotificada;

        if (desdeTiempo == null || desdeTiempo.equals(LocalDateTime.MIN)) {
            desdeTiempo = LocalDateTime.now().minusMinutes(5);
        }

        List<ExcepcionResponseDTO> excepciones = consultarExcepcionesUseCase.ejecutar(desdeTiempo);

        if (!excepciones.isEmpty()) {
            ultimaExcepcionNotificada = excepciones.stream()
                    .map(ExcepcionResponseDTO::getFechaRegistro)
                    .max(LocalDateTime::compareTo)
                    .orElse(ultimaExcepcionNotificada);
        }

        return ResponseEntity.ok(excepciones);
    }

    /** GET /api/v1/excepciones/{id} - Ver detalle de excepción */
    @GetMapping("/{id}")
    public ResponseEntity<ExcepcionResponseDTO> consultarDetalle(@PathVariable Long id) {
        log.info("GET /api/v1/excepciones/{}", id);
        return ResponseEntity.ok(consultarDetalleUseCase.ejecutar(id));
    }
}
