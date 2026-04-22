package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.*;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.model.enums.TipoMovimiento;
import com.distribuidoras.inventario.domain.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Use Case: Comprometer Inventario cuando Módulo 2 asigna ruta.
 * Spec 08: Realizar Pedido - Parte 2
 * 
 * FR-058: Recibir ruta de Módulo 2
 * FR-059: Re-verificar disponibilidad
 * FR-060: Comprometer productos con ruta asignada
 * FR-061: Seleccionar lotes por FEFO
 * FR-062: Registrar movimiento "Compromiso"
 * FR-063: Cambiar estado a "Comprometido"
 */
@Service
public class ComprometerInventarioUseCase {

    private static final Logger log = LoggerFactory.getLogger(ComprometerInventarioUseCase.class);

    private final PedidoRepository pedidoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;
    private final LoteRepository loteRepository;
    private final LoteComprometidoRepository loteComprometidoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public ComprometerInventarioUseCase(PedidoRepository pedidoRepository,
                                         ProductoPedidoRepository productoPedidoRepository,
                                         LoteRepository loteRepository,
                                         LoteComprometidoRepository loteComprometidoRepository,
                                         MovimientoInventarioRepository movimientoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
        this.loteRepository = loteRepository;
        this.loteComprometidoRepository = loteComprometidoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    /**
     * Command para compromiso de inventario.
     */
    public record ComprometerCommand(
            UUID pedidoId,
            UUID rutaId
    ) {}

    public record ComprometerResult(
            boolean exitoso,
            String numeroPedido,
            List<String> alertas
    ) {}

    /**
     * Ejecuta el compromiso de inventario con algoritmo FEFO.
     * Idempotencia: Si ya está comprometido, retorna resultado sin reprocesar.
     */
    @Transactional
    public ComprometerResult ejecutar(ComprometerCommand command) {
        log.info("Comprometiendo inventario para pedido: {}, ruta: {}", 
                command.pedidoId(), command.rutaId());

        // 1. Buscar pedido
        Pedido pedido = pedidoRepository.findById(command.pedidoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido no encontrado: " + command.pedidoId()));

        // 2. Idempotencia: Verificar si ya está comprometido
        if (pedido.getEstado() == EstadoPedido.COMPROMETIDO) {
            log.warn("Pedido {} ya está COMPROMETIDO. Ignorando (idempotencia).", 
                    pedido.getNumeroPedido());
            return new ComprometerResult(true, pedido.getNumeroPedido(), 
                    List.of("Pedido ya estaba comprometido (mensaje duplicado)"));
        }

        // 3. Validar estado
        if (pedido.getEstado() != EstadoPedido.ESPERANDO_RUTA) {
            throw new IllegalStateException(
                    "Pedido %s no está en ESPERANDO_RUTA, está en %s"
                            .formatted(pedido.getNumeroPedido(), pedido.getEstado()));
        }

        // 4. Obtener líneas del pedido
        List<ProductoPedido> lineas = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());

        // 5. Aplicar algoritmo FEFO por cada línea
        List<String> alertas = new ArrayList<>();
        for (ProductoPedido linea : lineas) {
            List<String> alertasLinea = comprometerLineaFEFO(pedido, linea);
            alertas.addAll(alertasLinea);
        }

        // 6. Actualizar pedido a COMPROMETIDO
        pedido.setEstado(EstadoPedido.COMPROMETIDO);
        pedido.setRutaId(command.rutaId());
        pedido.setFechaCompromiso(LocalDateTime.now());
        pedidoRepository.update(pedido);

        log.info("Pedido {} comprometido exitosamente. Alertas: {}", 
                pedido.getNumeroPedido(), alertas.size());

        return new ComprometerResult(true, pedido.getNumeroPedido(), alertas);
    }

    /**
     * Compromete una línea de pedido usando algoritmo FEFO.
     * Retorna alertas si hay compromiso parcial.
     */
    private List<String> comprometerLineaFEFO(Pedido pedido, ProductoPedido linea) {
        List<String> alertas = new ArrayList<>();

        // Obtener lotes disponibles ordenados FEFO
        List<Lote> lotesFEFO = loteRepository.findBySkuIdWithStock(linea.getSkuId()).stream()
                .sorted(Comparator.comparing(Lote::getFechaVencimiento))
                .toList();

        int cantidadPendiente = linea.getCantidadSolicitada();
        List<LoteComprometido> compromisos = new ArrayList<>();

        // Aplicar FEFO: seleccionar lotes hasta cubrir cantidad
        for (Lote lote : lotesFEFO) {
            if (cantidadPendiente <= 0) break;

            int cantidadAComprometer = Math.min(cantidadPendiente, lote.getCantidad());

            // Crear compromiso
            LoteComprometido compromiso = LoteComprometido.builder()
                    .compromisoId(UUID.randomUUID())
                    .productoPedidoId(linea.getProductoPedidoId())
                    .codigoLote(lote.getCodigoLote())
                    .cantidadComprometida(cantidadAComprometer)
                    .fechaCompromiso(LocalDateTime.now())
                    .build();

            compromisos.add(compromiso);

            // Reducir stock del lote
            lote.reducirStock(cantidadAComprometer);
            loteRepository.save(lote);

            // Registrar movimiento de inventario tipo COMPROMISO
            registrarMovimientoCompromiso(pedido, lote, cantidadAComprometer);

            cantidadPendiente -= cantidadAComprometer;
        }

        // Guardar todos los compromisos
        if (!compromisos.isEmpty()) {
            loteComprometidoRepository.saveAll(compromisos);
        }

        // Actualizar cantidad confirmada (puede ser parcial)
        int cantidadConfirmada = linea.getCantidadSolicitada() - cantidadPendiente;
        linea.setCantidadConfirmada(cantidadConfirmada);
        productoPedidoRepository.update(linea);

        // Generar alerta si compromiso parcial
        if (cantidadPendiente > 0) {
            String alerta = "Stock insuficiente para pedido %s, SKU %s: solicitado %d, confirmado %d"
                    .formatted(pedido.getNumeroPedido(), linea.getSkuId(),
                            linea.getCantidadSolicitada(), cantidadConfirmada);
            alertas.add(alerta);
            log.warn(alerta);
        }

        return alertas;
    }

    /**
     * Registra movimiento de inventario tipo COMPROMISO.
     */
    private void registrarMovimientoCompromiso(Pedido pedido, Lote lote, int cantidad) {
        MovimientoInventario movimiento = MovimientoInventario.builder()
                .movimientoId(UUID.randomUUID())
                .codigoLote(lote.getCodigoLote())
                .tipoMovimiento(TipoMovimiento.COMPROMISO)
                .cantidad(-cantidad)  // Negativo porque es salida/reserva
                .fechaMovimiento(LocalDateTime.now())
                .pedidoId(pedido.getPedidoId())
                .observaciones("Compromiso FEFO para pedido " + pedido.getNumeroPedido())
                .build();

        movimientoRepository.save(movimiento);
    }
}
