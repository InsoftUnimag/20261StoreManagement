package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.model.*;
import com.distribuidoras.inventario.domain.model.enums.*;
import com.distribuidoras.inventario.domain.repository.*;
import com.distribuidoras.inventario.application.usecase.mapper.InventarioMapper;
import com.distribuidoras.inventario.infrastructure.persistence.repository.StockGlobalSkuJpaRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.StockGlobalSkuJpaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * Caso de uso: Registrar Recepción de Mercancía.
 * Spec: 04_registrar_ingreso_productos.md
 * FR-021: Operación ATÓMICA (Lote + MovimientoInventario + stock en una transacción).
 * FR-024: Generar número de recepción secuencial y notificar al supervisor en caso de discrepancias.
 */
@Service
public class RegistrarRecepcionUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarRecepcionUseCase.class);
    private static final DateTimeFormatter NUM_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RecepcionRepository recepcionRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final ManifiestoRepository manifiestoRepository;
    private final DetalleManifiestoRepository detalleManifiestoRepository;
    private final ExcepcionInventarioRepository excepcionRepository;
    private final StockGlobalSkuJpaRepository stockGlobalSkuRepository;
    private final RestTemplate restTemplate;

    @Value("${app.notificaciones.supervisor.url:#{null}}")
    private String supervisorNotificationUrl;


    public RegistrarRecepcionUseCase(RecepcionRepository recepcionRepository,
                                     LoteRepository loteRepository,
                                     MovimientoInventarioRepository movimientoRepository,
                                     ProductoRepository productoRepository,
                                     ManifiestoRepository manifiestoRepository,
                                     DetalleManifiestoRepository detalleManifiestoRepository,
                                     ExcepcionInventarioRepository excepcionRepository,
                                     StockGlobalSkuJpaRepository stockGlobalSkuRepository,
                                     RestTemplate restTemplate) {
        this.recepcionRepository = recepcionRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
        this.manifiestoRepository = manifiestoRepository;
        this.detalleManifiestoRepository = detalleManifiestoRepository;
        this.excepcionRepository = excepcionRepository;
        this.stockGlobalSkuRepository = stockGlobalSkuRepository;
        this.restTemplate = restTemplate;
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

        // 2. Crear Recepción (FR-024) con número de recepción secuencial
        String numeroRecepcion = generarNumeroRecepcion();
        Recepcion recepcion = Recepcion.builder()
                .manifiestoId(command.manifiestoId())
                .operarioId(command.operarioId())
                .fechaRecepcion(LocalDateTime.now())
                .notas(command.notas())
                .numeroRecepcion(numeroRecepcion)
                .build();
        recepcionRepository.save(recepcion);

        // 3. Cargar detalles del manifiesto si aplica
        Map<String, DetalleManifiesto> detallesPorSku = command.manifiestoId() != null
                ? detalleManifiestoRepository.findByManifiestoId(command.manifiestoId()).stream()
                        .collect(Collectors.toMap(DetalleManifiesto::getSkuId, d -> d))
                : Collections.emptyMap();

        List<LoteResult> lotesCreados = new ArrayList<>();
        List<ExcepcionResult> excepcionesGeneradas = new ArrayList<>();

        // 4. Procesar cada línea
        command.lineas().forEach(linea -> {
            // FR-018: Verificar lote duplicado
            Optional<Lote> loteExistente = loteRepository.findBySkuIdAndCodigoLoteAndFechaVencimiento(
                    linea.skuId(), linea.codigoLote(), linea.fechaVencimiento());

            if (loteExistente.isPresent()) {
                throw new com.distribuidoras.inventario.domain.exception.LoteDuplicadoException(
                        linea.skuId(), linea.codigoLote());
            }

            Lote lote;
            // FR-015: Crear nuevo Lote con estado "Disponible"
            // FR-023: Calcular flag de urgencia FEFO
            boolean esUrgente = InventarioMapper.esLoteUrgente(linea.fechaVencimiento(), 30); // Usando 30 dias por ejemplo, o como requiera el mapper
            if (esUrgente) {
                log.warn("ALERTA: Producto crítico por vencimiento proximo. Lote {} del SKU {}", linea.codigoLote(), linea.skuId());
            }

            lote = Lote.builder()
                        .codigoLote(linea.codigoLote())
                        .skuId(linea.skuId())
                        .cantidad(linea.cantidadRecibida())
                        .fechaVencimiento(linea.fechaVencimiento())
                        .fechaExpedicion(linea.fechaFabricacion())
                        .costoUnitarioProducto(linea.costoUnitarioProducto())
                        .disponible(true)
                        .flagUrgenciaFefo(esUrgente)
                        .recepcionId(recepcion.getRecepcionId())
                        .creadoEl(LocalDateTime.now())
                        .build();
            loteRepository.save(lote);

            // Actualizar stock_global_sku
            actualizarStockGlobal(lote.getSkuId(), linea.cantidadRecibida(), linea.costoUnitarioProducto());

            // FR-017: Registrar MovimientoInventario tipo "Entrada"
            MovimientoInventario movimiento = MovimientoInventario.builder()
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
                            .tipoExcepcion(TipoExcepcion.DIFERENCIA)
                            .codigoLote(lote.getCodigoLote())
                            .skuId(linea.skuId().toString())
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

        // FR-025: Notificar al supervisor si hay excepciones
        if (!excepcionesGeneradas.isEmpty()) {
            notificarSupervisorDiscrepancias(recepcion.getNumeroRecepcion(), excepcionesGeneradas);
        }

        log.info("Recepción registrada: id={}, numero={}, lotes={}, excepciones={}",
                recepcion.getRecepcionId(), recepcion.getNumeroRecepcion(),
                lotesCreados.size(), excepcionesGeneradas.size());

        return new RecepcionResult(recepcion.getRecepcionId(), recepcion.getNumeroRecepcion(),
                recepcion.getFechaRecepcion(), lotesCreados, excepcionesGeneradas);
    }

    private void actualizarEstadoManifiesto(Long manifiestoId) {
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

    public record RecepcionCommand(Long manifiestoId, Long operarioId,
                                    List<LineaRecepcionCommand> lineas, String notas) {}

    public record LineaRecepcionCommand(String skuId, String codigoLote,
                                         LocalDate fechaVencimiento, LocalDate fechaFabricacion,
                                         int cantidadRecibida, BigDecimal costoUnitarioProducto) {}

    public record RecepcionResult(Long recepcionId, String numeroRecepcion, LocalDateTime fechaRecepcion,
                                   List<LoteResult> lotesCreados,
                                   List<ExcepcionResult> excepcionesGeneradas) {}

    public record LoteResult(String codigoLoteResult, String skuId, String codigoLote, int cantidad) {}

    public record ExcepcionResult(Long excepcionId, String tipo,
                                   int cantidadAfectada, String descripcion) {}

    private void actualizarStockGlobal(String skuId, int cantidadEntrante, BigDecimal precioUnitario) {
        var stockOpt = stockGlobalSkuRepository.findById(Objects.requireNonNull(skuId));
        if (stockOpt.isPresent()) {
            var stock = stockOpt.get();
            stock.setFisicoTotal(stock.getFisicoTotal() + cantidadEntrante);
            stock.setDisponibles(stock.getDisponibles() + cantidadEntrante);
            if (precioUnitario != null) {
                stock.setPrecio(precioUnitario);
            }
            stockGlobalSkuRepository.save(stock);
            log.info("Stock global actualizado para {}: fisico_total={}, disponibles={}, precio={}",
                    skuId, stock.getFisicoTotal(), stock.getDisponibles(), stock.getPrecio());
        } else {
            var stock = new StockGlobalSkuJpaEntity();
            stock.setSkuId(skuId);
            stock.setFisicoTotal(cantidadEntrante);
            stock.setDisponibles(cantidadEntrante);
            stock.setComprometidos(0);
            if (precioUnitario != null) {
                stock.setPrecio(precioUnitario);
            } else {
                stock.setPrecio(BigDecimal.ZERO);
            }
            stockGlobalSkuRepository.save(stock);
            log.info("Stock global creado para {}: fisico_total={}, disponibles={}, precio={}",
                    skuId, cantidadEntrante, cantidadEntrante, stock.getPrecio());
        }
    }

    private String generarNumeroRecepcion() {
        LocalDate hoy = LocalDate.now();
        String fechaHoy = hoy.format(NUM_FORMAT);
        
        int siguiente = recepcionRepository.findMaxNumeroRecepcionByFecha(hoy)
                .map(max -> max + 1)
                .orElse(1);
        
        return String.format("REC-%s-%04d", fechaHoy, siguiente);
    }

    private void notificarSupervisorDiscrepancias(String numeroRecepcion, List<ExcepcionResult> excepciones) {
        if (supervisorNotificationUrl == null || supervisorNotificationUrl.isBlank()) {
            log.warn("URL de notificación al supervisor no configurada. Discrepancias detectadas en recepción: {}",
                    numeroRecepcion);
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("numeroRecepcion", numeroRecepcion);
            payload.put("fecha", LocalDateTime.now().toString());
            payload.put("excepciones", excepciones.stream()
                    .map(e -> Map.of(
                            "tipo", e.tipo(),
                            "cantidadAfectada", e.cantidadAfectada(),
                            "descripcion", e.descripcion()
                    ))
                    .collect(Collectors.toList()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(Objects.requireNonNull(supervisorNotificationUrl), entity, String.class);
            log.info("Notificación enviada al supervisor para recepción {} con {} excepciones",
                    numeroRecepcion, excepciones.size());
        } catch (Exception e) {
            log.error("Error al notificar al supervisor sobre discrepancias en recepción {}: {}",
                    numeroRecepcion, e.getMessage());
        }
    }
}
