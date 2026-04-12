package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.exception.StockInsuficienteException;
import com.distribuidoras.inventario.domain.model.*;
import com.distribuidoras.inventario.domain.model.enums.*;
import com.distribuidoras.inventario.domain.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso: Registrar Excepción de Inventario.
 * Spec: 16_reportar_excepciones_inventario.md
 * FR-025 a FR-033
 */
@Service
public class RegistrarExcepcionUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarExcepcionUseCase.class);

    private final ExcepcionInventarioRepository excepcionRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    public RegistrarExcepcionUseCase(ExcepcionInventarioRepository excepcionRepository,
                                     LoteRepository loteRepository,
                                     MovimientoInventarioRepository movimientoRepository,
                                     ProductoRepository productoRepository) {
        this.excepcionRepository = excepcionRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public ExcepcionResultado ejecutar(ExcepcionCommand command) {
        // Validar SKU existe
        if (productoRepository.findById(command.skuId()).isEmpty()) {
            throw new ProductoNotFoundException(command.skuId());
        }

        // Validar lote si presente - FR-026
        Lote lote = null;
        if (command.codigoLote() != null && !command.codigoLote().isBlank()) {
            lote = loteRepository.findById(command.codigoLote())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Lote con código '%s' no encontrado".formatted(command.codigoLote())));

            if (command.cantidadAfectada() > lote.getCantidad()) {
                throw new IllegalArgumentException(
                        "Cantidad afectada (%d) mayor al stock disponible (%d) en lote %s"
                                .formatted(command.cantidadAfectada(), lote.getCantidad(), command.codigoLote()));
            }
        }

        // Crear excepción (FR-025)
        ExcepcionInventario excepcion = ExcepcionInventario.builder()
                .excepcionId(UUID.randomUUID())
                .tipoExcepcion(command.tipoExcepcion())
                .codigoLote(command.codigoLote())
                .skuId(command.skuId())
                .cantidadAfectada(command.cantidadAfectada())
                .fechaRegistro(LocalDateTime.now())
                .operarioId(command.operarioId())
                .descripcion(command.descripcion())
                .evidenciaUrl(command.evidenciaUrl())
                .build();
        excepcionRepository.save(excepcion);

        // FR-027, FR-028: Si requiere baja de stock, crear movimiento y reducir stock
        MovimientoInventario movimiento = null;
        if (lote != null && requiereBajaStock(command.tipoExcepcion())) {
            TipoMovimiento tipoMov = mapTipoMovimiento(command.tipoExcepcion());

            // FR-029/FR-030: Si vencimiento, forzar cantidad completa
            int cantidadBaja = command.cantidadAfectada();
            if (command.tipoExcepcion() == TipoExcepcion.VENCIMIENTO) {
                cantidadBaja = lote.getCantidad(); // Forzar a cero (Agotado)
            }

            lote.reducirStock(cantidadBaja);
            loteRepository.save(lote);

            movimiento = MovimientoInventario.builder()
                    .movimientoId(UUID.randomUUID())
                    .codigoLote(lote.getCodigoLote())
                    .tipoMovimiento(tipoMov)
                    .cantidad(-cantidadBaja)
                    .fechaMovimiento(LocalDateTime.now())
                    .excepcionId(excepcion.getExcepcionId())
                    .operarioId(command.operarioId())
                    .observaciones(command.descripcion())
                    .build();
            movimientoRepository.save(movimiento);

            log.warn("Excepción registrada: tipo={}, SKU={}, lote={}, cantidad_baja={}",
                    command.tipoExcepcion(), command.skuId(), command.codigoLote(), cantidadBaja);
        } else {
            log.info("Excepción registrada (sin baja de stock): tipo={}, SKU={}",
                    command.tipoExcepcion(), command.skuId());
        }

        return new ExcepcionResultado(
                excepcion.getExcepcionId(), excepcion.getTipoExcepcion().name(),
                excepcion.getFechaRegistro(),
                movimiento != null ? new MovimientoInfo(movimiento.getMovimientoId(),
                        movimiento.getTipoMovimiento().name(), movimiento.getCantidad()) : null);
    }

    private boolean requiereBajaStock(TipoExcepcion tipo) {
        return tipo == TipoExcepcion.AVERIA || tipo == TipoExcepcion.VENCIMIENTO || tipo == TipoExcepcion.FALTANTE;
    }

    private TipoMovimiento mapTipoMovimiento(TipoExcepcion tipo) {
        return switch (tipo) {
            case AVERIA -> TipoMovimiento.BAJA_AVERIA;
            case VENCIMIENTO -> TipoMovimiento.BAJA_VENCIMIENTO;
            case FALTANTE -> TipoMovimiento.FALTANTE;
            default -> throw new IllegalArgumentException("Tipo de excepción no soportado para baja: " + tipo);
        };
    }

    // --- Command & Result Records ---

    public record ExcepcionCommand(TipoExcepcion tipoExcepcion, UUID skuId, String codigoLote,
                                    int cantidadAfectada, String descripcion,
                                    String evidenciaUrl, UUID operarioId) {}

    public record ExcepcionResultado(UUID excepcionId, String tipoExcepcion,
                                      LocalDateTime fechaRegistro, MovimientoInfo movimientoGenerado) {}

    public record MovimientoInfo(UUID movimientoId, String tipoMovimiento, int cantidad) {}
}
