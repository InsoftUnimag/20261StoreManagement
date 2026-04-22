package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.enums.TipoMovimiento;
import com.distribuidoras.inventario.domain.repository.ExcepcionInventarioRepository;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.MovimientoInventarioRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.AlertasDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.MovimientosHoyDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.ResumenInventarioDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Use Case: Dashboard con resumen general de inventario.
 * Spec 05: Consultar Inventario
 * 
 * Provides a high-level summary of the inventory system status.
 */
@Service
public class ConsultarResumenInventarioUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarResumenInventarioUseCase.class);
    private static final int UMBRAL_VENCIMIENTO_DIAS = 30;

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ExcepcionInventarioRepository excepcionRepository;

    public ConsultarResumenInventarioUseCase(ProductoRepository productoRepository,
                                              LoteRepository loteRepository,
                                              MovimientoInventarioRepository movimientoRepository,
                                              ExcepcionInventarioRepository excepcionRepository) {
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.excepcionRepository = excepcionRepository;
    }

    /**
     * Ejecuta el caso de uso de resumen de inventario.
     * 
     * @return ResumenInventarioDTO con el dashboard completo
     */
    @Transactional(readOnly = true)
    public ResumenInventarioDTO ejecutar() {
        log.info("Consultando resumen de inventario");

        // 1. COUNT(DISTINCT Producto) con lotes activos
        Integer totalSkusActivos = productoRepository.countActiveSkus();

        // 2. COUNT(Lote) WHERE cantidad > 0
        Integer totalLotesConStock = loteRepository.countLotesWithStock();

        // 3. SUM(Lote.cantidad)
        Integer stockTotalUnidades = loteRepository.sumTotalStock();

        // 4. Alertas
        AlertasDTO alertas = calcularAlertas();

        // 5. Movimientos de hoy
        MovimientosHoyDTO movimientosHoy = calcularMovimientosHoy();

        // Build result using builder pattern
        ResumenInventarioDTO resultado = ResumenInventarioDTO.builder()
                .totalSkusActivos(totalSkusActivos)
                .totalLotesConStock(totalLotesConStock)
                .stockTotalUnidades(stockTotalUnidades)
                .alertas(alertas)
                .movimientosHoy(movimientosHoy)
                .build();

        log.info("Resumen consultado: {} SKUs activos, {} unidades en stock", 
                totalSkusActivos, stockTotalUnidades);

        return resultado;
    }

    /**
     * Calcula las alertas del sistema usando composición funcional.
     */
    private AlertasDTO calcularAlertas() {
        // Lotes próximos a vencer (30 días)
        Integer proximosVencer = loteRepository.countLotesExpiringWithinDays(UMBRAL_VENCIMIENTO_DIAS);

        // SKUs con stock bajo (umbral configurable, ej: 50 units - this is a simplified version)
        // In a real scenario, you'd want a specific query for this
        Integer stockBajo = 0; // Placeholder - would need additional repository method

        // Excepciones abiertas
        Integer excepcionesAbiertas = excepcionRepository.countOpenExceptions();

        return AlertasDTO.builder()
                .proximosVencer30Dias(proximosVencer)
                .stockBajo(stockBajo)
                .excepcionesAbiertas(excepcionesAbiertas)
                .build();
    }

    /**
     * Calcula los movimientos del día actual.
     */
    private MovimientosHoyDTO calcularMovimientosHoy() {
        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();

        // COUNT MovimientoInventario WHERE fecha >= hoy 00:00
        Integer entradas = movimientoRepository.countByTipoMovimientoAndFechaAfter(
                TipoMovimiento.ENTRADA, inicioHoy);

        Integer salidas = movimientoRepository.countByTipoMovimientoAndFechaAfter(
                TipoMovimiento.SALIDA, inicioHoy) + 
                movimientoRepository.countByTipoMovimientoAndFechaAfter(
                        TipoMovimiento.PICKING, inicioHoy);

        Integer compromisos = movimientoRepository.countByTipoMovimientoAndFechaAfter(
                TipoMovimiento.COMPROMISO, inicioHoy);

        return MovimientosHoyDTO.builder()
                .entradas(entradas)
                .salidas(salidas)
                .compromisos(compromisos)
                .build();
    }
}
