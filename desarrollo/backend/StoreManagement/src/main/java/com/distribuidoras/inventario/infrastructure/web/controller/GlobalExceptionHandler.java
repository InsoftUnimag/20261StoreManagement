package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.domain.exception.ClienteInactivoException;
import com.distribuidoras.inventario.domain.exception.ClienteNotFoundException;
import com.distribuidoras.inventario.domain.exception.ExternalServiceException;
import com.distribuidoras.inventario.domain.exception.PedidoEstadoInvalidoException;
import com.distribuidoras.inventario.domain.exception.PedidoNotFoundException;
import com.distribuidoras.inventario.domain.exception.ProductoConLotesActivosException;
import com.distribuidoras.inventario.domain.exception.ProductoDuplicadoException;
import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones.
 * Mapea excepciones de dominio a códigos HTTP correctos.
 * T019 del plan de implementación.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 409 Conflict - Producto duplicado (marca + presentación ya existe)
     */
    @ExceptionHandler(ProductoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleProductoDuplicado(ProductoDuplicadoException ex) {
        log.warn("Producto duplicado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * 404 Not Found - Producto no existe
     */
    @ExceptionHandler(ProductoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductoNotFound(ProductoNotFoundException ex) {
        log.warn("Producto no encontrado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * 409 Conflict - No se puede eliminar producto con lotes activos
     */
    @ExceptionHandler(ProductoConLotesActivosException.class)
    public ResponseEntity<Map<String, Object>> handleProductoConLotesActivos(ProductoConLotesActivosException ex) {
        log.warn("Producto con lotes activos: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * 400 Bad Request - Validación de campos (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Datos inválidos");
        body.put("campos", fieldErrors);

        log.warn("Validación fallida: {}", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    // --- Excepciones del módulo Recepción & Excepciones ---

    /**
     * 409 Conflict - Lote duplicado
     */
    @ExceptionHandler(com.distribuidoras.inventario.domain.exception.LoteDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleLoteDuplicado(
            com.distribuidoras.inventario.domain.exception.LoteDuplicadoException ex) {
        log.warn("Lote duplicado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * 409 Conflict - Stock insuficiente
     */
    @ExceptionHandler(com.distribuidoras.inventario.domain.exception.StockInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleStockInsuficiente(
            com.distribuidoras.inventario.domain.exception.StockInsuficienteException ex) {
        log.warn("Stock insuficiente: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * 404 Not Found - Excepción no encontrada
     */
    @ExceptionHandler(com.distribuidoras.inventario.domain.exception.ExcepcionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleExcepcionNotFound(
            com.distribuidoras.inventario.domain.exception.ExcepcionNotFoundException ex) {
        log.warn("Excepción no encontrada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * 404 Not Found - Manifiesto no encontrado
     */
    @ExceptionHandler(com.distribuidoras.inventario.domain.exception.ManifiestoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleManifiestoNotFound(
            com.distribuidoras.inventario.domain.exception.ManifiestoNotFoundException ex) {
        log.warn("Manifiesto no encontrado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * 400 Bad Request - Argumentos inválidos (fecha vencimiento, transiciones de estado, etc.)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // --- Excepciones del módulo Pedidos ---

    /**
     * 404 Not Found - Cliente no encontrado
     */
    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleClienteNotFound(ClienteNotFoundException ex) {
        log.warn("Cliente no encontrado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * 400 Bad Request - Cliente inactivo
     */
    @ExceptionHandler(ClienteInactivoException.class)
    public ResponseEntity<Map<String, Object>> handleClienteInactivo(ClienteInactivoException ex) {
        log.warn("Cliente inactivo: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * 503 Service Unavailable - Servicio externo no disponible
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Map<String, Object>> handleExternalService(ExternalServiceException ex) {
        log.error("Servicio externo no disponible: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    /**
     * 404 Not Found - Pedido no encontrado
     */
    @ExceptionHandler(PedidoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePedidoNotFound(PedidoNotFoundException ex) {
        log.warn("Pedido no encontrado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * 400 Bad Request - Estado inválido para transición de pedido
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        log.error("Estado inválido: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * 409 Conflict - Pedido en estado inválido para picking/despacho
     */
    @ExceptionHandler(PedidoEstadoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handlePedidoEstadoInvalido(PedidoEstadoInvalidoException ex) {
        log.warn("Pedido en estado inválido: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }
}
