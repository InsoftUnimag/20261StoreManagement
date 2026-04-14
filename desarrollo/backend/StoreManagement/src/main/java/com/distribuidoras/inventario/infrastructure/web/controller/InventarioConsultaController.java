package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.*;
import com.distribuidoras.inventario.infrastructure.web.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for inventory consultation endpoints.
 * Spec 05: Consultar Inventario
 * Spec 06: Consultar Disponibilidad
 * 
 * All endpoints are read-only with cache headers for stock queries.
 */
@RestController
@RequestMapping("/api/v1/inventario")
@Validated
public class InventarioConsultaController {

    private static final Logger log = LoggerFactory.getLogger(InventarioConsultaController.class);
    private static final int CACHE_MAX_AGE_SECONDS = 30;

    private final ConsultarStockPorSkuUseCase consultarStockPorSkuUseCase;
    private final ConsultarStockMultipleSkusUseCase consultarStockMultipleSkusUseCase;
    private final ConsultarDisponibilidadPedidoUseCase consultarDisponibilidadPedidoUseCase;
    private final ConsultarMovimientosInventarioUseCase consultarMovimientosInventarioUseCase;
    private final ConsultarResumenInventarioUseCase consultarResumenInventarioUseCase;

    public InventarioConsultaController(ConsultarStockPorSkuUseCase consultarStockPorSkuUseCase,
                                         ConsultarStockMultipleSkusUseCase consultarStockMultipleSkusUseCase,
                                         ConsultarDisponibilidadPedidoUseCase consultarDisponibilidadPedidoUseCase,
                                         ConsultarMovimientosInventarioUseCase consultarMovimientosInventarioUseCase,
                                         ConsultarResumenInventarioUseCase consultarResumenInventarioUseCase) {
        this.consultarStockPorSkuUseCase = consultarStockPorSkuUseCase;
        this.consultarStockMultipleSkusUseCase = consultarStockMultipleSkusUseCase;
        this.consultarDisponibilidadPedidoUseCase = consultarDisponibilidadPedidoUseCase;
        this.consultarMovimientosInventarioUseCase = consultarMovimientosInventarioUseCase;
        this.consultarResumenInventarioUseCase = consultarResumenInventarioUseCase;
    }

    /**
     * GET /api/v1/inventario/stock/{sku_id}
     * Consultar stock disponible de un SKU con detalle de lotes FEFO.
     */
    @GetMapping("/stock/{skuId}")
    public ResponseEntity<StockDisponibleDTO> consultarStockPorSku(
            @PathVariable @NotBlank String skuId) {
        
        log.info("REST: Consultando stock para SKU {}", skuId);
        
        StockDisponibleDTO resultado = consultarStockPorSkuUseCase.ejecutar(UUID.fromString(skuId));
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "max-age=" + CACHE_MAX_AGE_SECONDS)
                .body(resultado);
    }

    /**
     * GET /api/v1/inventario/stock
     * Consultar stock de múltiples SKUs (batch query).
     */
    @GetMapping("/stock")
    public ResponseEntity<StockMultipleDTO> consultarStockMultiple(
            @RequestParam @NotBlank String skuIds,
            @RequestParam(defaultValue = "false") boolean includeZeroStock) {
        
        log.info("REST: Consultando stock múltiple para SKUs: {}", skuIds);
        
        List<UUID> skuIdList = Arrays.stream(skuIds.split(","))
                .map(String::trim)
                .map(UUID::fromString)
                .collect(Collectors.toList());
        
        StockMultipleDTO resultado = consultarStockMultipleSkusUseCase.ejecutar(skuIdList, includeZeroStock);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "max-age=" + CACHE_MAX_AGE_SECONDS)
                .body(resultado);
    }

    /**
     * POST /api/v1/inventario/disponibilidad
     * Verificar disponibilidad para un pedido.
     */
    @PostMapping("/disponibilidad")
    public ResponseEntity<DisponibilidadDTO> verificarDisponibilidadPedido(
            @Valid @RequestBody DisponibilidadRequestDTO request) {
        
        log.info("REST: Verificando disponibilidad para pedido {}", request.pedidoId());
        
        // Convert request to use case format
        List<ConsultarDisponibilidadPedidoUseCase.LineaPedidoDTO> lineas = request.lineas().stream()
                .map(linea -> new ConsultarDisponibilidadPedidoUseCase.LineaPedidoDTO(
                        linea.skuId(),
                        linea.cantidad()))
                .toList();
        
        DisponibilidadDTO resultado = consultarDisponibilidadPedidoUseCase.ejecutar(
                request.pedidoId(), lineas);
        
        // Return 200 if available, 409 if not (conflict)
        HttpStatus status = resultado.disponible() ? HttpStatus.OK : HttpStatus.CONFLICT;
        
        return ResponseEntity.status(status).body(resultado);
    }

    /**
     * GET /api/v1/inventario/movimientos
     * Consultar kardex (movimientos de inventario) con filtros.
     */
    @GetMapping("/movimientos")
    public ResponseEntity<MovimientosInventarioResponseDTO> consultarMovimientos(
            @RequestParam(required = false) String skuId,
            @RequestParam(required = false) String codigoLote,
            @RequestParam(required = false) String tipoMovimiento,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size) {
        
        log.info("REST: Consultando movimientos con filtros");
        
        ConsultarMovimientosInventarioUseCase.FiltrosKardexDTO filtros = 
                new ConsultarMovimientosInventarioUseCase.FiltrosKardexDTO(
                        skuId != null ? UUID.fromString(skuId) : null,
                        codigoLote,
                        tipoMovimiento != null ? 
                                com.distribuidoras.inventario.domain.model.enums.TipoMovimiento.valueOf(tipoMovimiento) 
                                : null,
                        fechaDesde,
                        fechaHasta,
                        page,
                        size
                );
        
        MovimientosInventarioResponseDTO resultado = consultarMovimientosInventarioUseCase.ejecutar(filtros);
        
        // No cache for kardex - must be real-time
        return ResponseEntity.ok(resultado);
    }

    /**
     * GET /api/v1/inventario/resumen
     * Dashboard con resumen general de inventario.
     */
    @GetMapping("/resumen")
    public ResponseEntity<ResumenInventarioDTO> consultarResumen() {
        
        log.info("REST: Consultando resumen de inventario");
        
        ResumenInventarioDTO resultado = consultarResumenInventarioUseCase.ejecutar();
        
        // Cache for 30 seconds
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "max-age=" + CACHE_MAX_AGE_SECONDS)
                .body(resultado);
    }
}
