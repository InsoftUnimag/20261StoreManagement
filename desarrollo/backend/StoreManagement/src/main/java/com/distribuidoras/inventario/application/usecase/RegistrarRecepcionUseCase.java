package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.LoteDuplicadoException;
import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.model.*;
import com.distribuidoras.inventario.domain.model.enums.*;
import com.distribuidoras.inventario.domain.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Caso de uso: Registrar Recepción de Mercancía.
 * Spec: 04_registrar_ingreso_productos.md
 * FR-021: Operación ATÓMICA (Lote + MovimientoInventario + stock en una transacción).
 */
@Service
public class RegistrarRecepcionUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarRecepcionUseCase.class);

    private final RecepcionRepository recepcionRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final ManifiestoRepository manifiestoRepository;
    private final DetalleManifiestoRepository detalleManifiestoRepository;
    private final ExcepcionInventarioRepository excepcionRepository;

    public RegistrarRecepcionUseCase(RecepcionRepository recepcionRepository,
                                     LoteRepository loteRepository,
                                     MovimientoInventarioRepository movimientoRepository,
                                     ProductoRepository productoRepository,
                                     ManifiestoRepository manifiestoRepository,
                                     DetalleManifiestoRepository detalleManifiestoRepository,
                                     ExcepcionInventarioRepository excepcionRepository) {
        this.recepcionRepository = recepcionRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
        this.manifiestoRepository = manifiestoRepository;
        this.detalleManifiestoRepository = detalleManifiestoRepository;
        this.excepcionRepository = excepcionRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public RecepcionResult ejecutar(RecepcionCommand command) {
        // 1. Validar SKUs
        command.lineas().forEach(linea -> {
            if (productoRepository.findById(linea.skuId()).isEmpty()) {
                throw new ProductoNotFoundException(linea.skuId());
            }
            // FR-014: Validar fecha vencimiento futura
            if (!linea.fechaVencimiento().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "La fecha de vencimiento debe ser posterior a la fecha actual para lote " + linea.codigoLote());
            }
        });

        // 2. Crear Recepción (FR-024)
        Recepcion recepcion = Recepcion.builder()
                .recepcionId(UUID.randomUUID())
                .manifiestoId(command.manifiestoId())
                .operarioId(command.operarioId())
                .fechaRecepcion(LocalDateTime.now())
                .notas(command.notas())
                .build();
        recepcionRepository.save(recepcion);

        // 3. Cargar detalles del manifiesto si aplica
        Map<UUID, DetalleManifiesto> detallesPorSku = command.manifiestoId() != null
                ? detalleManifiestoRepository.findByManifiestoId(command.manifiestoId()).stream()
                        .collect(java.util.stream.Collectors.toMap(DetalleManifiesto::getSkuId, d -> d))
                : Collections.emptyMap();

        List<LoteResult> lotesCreados = new ArrayList<>();
        List<ExcepcionResult> excepcionesGeneradas = new ArrayList<>();

        // 4. Procesar cada línea
        command.lineas().forEach(linea -> {
            // FR-018: Verificar lote duplicado
            Optional<Lote> loteExistente = loteRepository.findBySkuIdAndCodigoLoteAndFechaVencimiento(
                    linea.skuId(), linea.codigoLote(), linea.fechaVencimiento());

            Lote lote;
            if (loteExistente.isPresent()) {
                // Incrementar stock de lote existente
                lote = loteExistente.get();
                lote.incrementarStock(linea.cantidadRecibida());
                loteRepository.save(lote);
            } else {
                // FR-015: Crear nuevo Lote con estado "Disponible"
                lote = Lote.builder()
                        .codigoLote(linea.codigoLote())
                        .skuId(linea.skuId())
                        .cantidad(linea.cantidadRecibida())
                        .fechaVencimiento(linea.fechaVencimiento())
                        .fechaExpedicion(linea.fechaFabricacion())
                        .disponible(true)
                        .flagUrgenciaFefo(false)
                        .recepcionId(recepcion.getRecepcionId())
                        .creadoEl(LocalDateTime.now())
                        .build();
                loteRepository.save(lote);
            }

            // FR-017: Registrar MovimientoInventario tipo "Entrada"
            MovimientoInventario movimiento = MovimientoInventario.builder()
                    .movimientoId(UUID.randomUUID())
                    .codigoLote(lote.getCodigoLote())
                    .tipoMovimiento(TipoMovimiento.ENTRADA)
                    .cantidad(linea.cantidadRecibida())
                    .fechaMovimiento(LocalDateTime.now())
                    .operarioId(command.operarioId())
                    .observaciones("Recepción: " + recepcion.getRecepcionId())
                    .build();
            movimientoRepository.save(movimiento);

            lotesCreados.add(new LoteResult(lote.getCodigoLote(), lote.getSkuId(),
                    lote.getCodigoLote(), lote.getCantidad()));

            // FR-020: Detectar discrepancia con manifiesto
            DetalleManifiesto detalle = detallesPorSku.get(linea.skuId());
            if (detalle != null) {
                detalle.setCantidadRecibida(detalle.getCantidadRecibida() + linea.cantidadRecibida());
                detalleManifiestoRepository.save(detalle);

                int diferencia = detalle.getCantidadRecibida() - detalle.getCantidadEsperada();
                if (diferencia != 0) {
                    ExcepcionInventario excepcion = ExcepcionInventario.builder()
                            .excepcionId(UUID.randomUUID())
                            .tipoExcepcion(TipoExcepcion.DIFERENCIA)
                            .codigoLote(lote.getCodigoLote())
                            .skuId(linea.skuId())
                            .cantidadAfectada(Math.abs(diferencia))
                            .fechaRegistro(LocalDateTime.now())
                            .operarioId(command.operarioId())
                            .descripcion("Diferencia automática: Esperado %d, Recibido %d"
                                    .formatted(detalle.getCantidadEsperada(), detalle.getCantidadRecibida()))
                            .build();
                    excepcionRepository.save(excepcion);

                    excepcionesGeneradas.add(new ExcepcionResult(
                            excepcion.getExcepcionId(), "Diferencia",
                            diferencia, excepcion.getDescripcion()));
                }
            }
        });

        // 5. Actualizar estado del manifiesto
        if (command.manifiestoId() != null) {
            actualizarEstadoManifiesto(command.manifiestoId());
        }

        log.info("Recepción registrada: id={}, lotes={}, excepciones={}",
                recepcion.getRecepcionId(), lotesCreados.size(), excepcionesGeneradas.size());

        return new RecepcionResult(recepcion.getRecepcionId(), recepcion.getFechaRecepcion(),
                lotesCreados, excepcionesGeneradas);
    }

    private void actualizarEstadoManifiesto(UUID manifiestoId) {
        List<DetalleManifiesto> detalles = detalleManifiestoRepository.findByManifiestoId(manifiestoId);
        boolean todosCompletos = detalles.stream()
                .allMatch(d -> d.getCantidadRecibida() >= d.getCantidadEsperada());
        boolean algunoRecibido = detalles.stream()
                .anyMatch(d -> d.getCantidadRecibida() > 0);

        manifiestoRepository.findById(manifiestoId).ifPresent(m -> {
            if (todosCompletos) {
                m.setEstado(EstadoManifiesto.RECEPCIONADO_TOTAL);
            } else if (algunoRecibido) {
                m.setEstado(EstadoManifiesto.RECEPCIONADO_PARCIAL);
            }
            manifiestoRepository.save(m);
        });
    }

    // --- Command & Result Records ---

    public record RecepcionCommand(UUID manifiestoId, UUID operarioId,
                                    List<LineaRecepcionCommand> lineas, String notas) {}

    public record LineaRecepcionCommand(UUID skuId, String codigoLote,
                                         LocalDate fechaVencimiento, LocalDate fechaFabricacion,
                                         int cantidadRecibida) {}

    public record RecepcionResult(UUID recepcionId, LocalDateTime fechaRecepcion,
                                   List<LoteResult> lotesCreados,
                                   List<ExcepcionResult> excepcionesGeneradas) {}

    public record LoteResult(String codigoLoteResult, UUID skuId, String codigoLote, int cantidad) {}

    public record ExcepcionResult(UUID excepcionId, String tipo,
                                   int cantidadAfectada, String descripcion) {}
}
