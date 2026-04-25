package com.distribuidoras.inventario.infrastructure.scheduler;

import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.model.enums.TipoExcepcion;
import com.distribuidoras.inventario.domain.model.enums.TipoMovimiento;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.MovimientoInventarioRepository;
import com.distribuidoras.inventario.domain.repository.ExcepcionInventarioRepository;
import com.distribuidoras.inventario.domain.model.ExcepcionInventario;
import com.distribuidoras.inventario.domain.model.MovimientoInventario;
import com.distribuidoras.inventario.infrastructure.persistence.repository.StockGlobalSkuJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class LotesVencidosScheduler {

    private static final Logger log = LoggerFactory.getLogger(LotesVencidosScheduler.class);

    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ExcepcionInventarioRepository excepcionRepository;
    private final StockGlobalSkuJpaRepository stockGlobalSkuRepository;

    public LotesVencidosScheduler(LoteRepository loteRepository,
            MovimientoInventarioRepository movimientoRepository,
            ExcepcionInventarioRepository excepcionRepository,
            StockGlobalSkuJpaRepository stockGlobalSkuRepository) {
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.excepcionRepository = excepcionRepository;
        this.stockGlobalSkuRepository = stockGlobalSkuRepository;
    }

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional(rollbackFor = Exception.class)
    public void procesarLotesVencidos() {
        log.info("Iniciando proceso automático de lotes vencidos");

        LocalDate today = LocalDate.now();
        List<Lote> lotesVencidos = loteRepository.findByFechaVencimientoBeforeAndCantidadGreaterThan(today, 0);

        if (lotesVencidos.isEmpty()) {
            log.info("No hay lotes vencidos para procesar");
            return;
        }

        log.info("Se encontraron {} lotes vencidos", lotesVencidos.size());

        for (Lote lote : lotesVencidos) {
            try {
                int cantidadBaja = lote.getCantidad();
                String skuId = lote.getSkuId();

                ExcepcionInventario excepcion = ExcepcionInventario.builder()
                        .excepcionId(UUID.randomUUID())
                        .tipoExcepcion(TipoExcepcion.VENCIMIENTO)
                        .codigoLote(lote.getCodigoLote())
                        .skuId(skuId)
                        .cantidadAfectada(cantidadBaja)
                        .fechaRegistro(LocalDateTime.now())
                        .operarioId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                        .descripcion("Proceso automático: Lote vencido el " + lote.getFechaVencimiento())
                        .build();
                excepcionRepository.save(excepcion);

                MovimientoInventario movimiento = MovimientoInventario.builder()
                        .movimientoId(UUID.randomUUID())
                        .codigoLote(lote.getCodigoLote())
                        .tipoMovimiento(TipoMovimiento.BAJA_VENCIMIENTO)
                        .cantidad(-cantidadBaja)
                        .fechaMovimiento(LocalDateTime.now())
                        .excepcionId(excepcion.getExcepcionId())
                        .operarioId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                        .observaciones("Proceso automático de lotes vencidos")
                        .build();
                movimientoRepository.save(movimiento);

                lote.reducirStock(cantidadBaja);
                loteRepository.save(lote);

                stockGlobalSkuRepository.findById(Objects.requireNonNull(skuId)).ifPresent(stock -> {
                    stock.setFisicoTotal(stock.getFisicoTotal() - cantidadBaja);
                    stock.setDisponibles(stock.getDisponibles() - cantidadBaja);
                    stockGlobalSkuRepository.save(stock);
                });

                log.info("Lote procesado: código={}, SKU={}, cantidad_baja={}",
                        lote.getCodigoLote(), skuId, cantidadBaja);

            } catch (Exception e) {
                log.error("Error procesando lote {}: {}", lote.getCodigoLote(), e.getMessage());
            }
        }

        log.info("Proceso automático de lotes vencidos completado: {} lotes procesados", lotesVencidos.size());
    }
}