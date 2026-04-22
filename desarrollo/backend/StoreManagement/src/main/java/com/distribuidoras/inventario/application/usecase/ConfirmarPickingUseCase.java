package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.PedidoEstadoInvalidoException;
import com.distribuidoras.inventario.domain.model.ExcepcionInventario;
import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.model.LoteComprometido;
import com.distribuidoras.inventario.domain.model.MovimientoInventario;
import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.ProductoPedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.model.enums.TipoExcepcion;
import com.distribuidoras.inventario.domain.model.enums.TipoMovimiento;
import com.distribuidoras.inventario.domain.repository.ExcepcionInventarioRepository;
import com.distribuidoras.inventario.domain.repository.LoteComprometidoRepository;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.MovimientoInventarioRepository;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case: Confirmar Picking de pedido comprometido.
 * Spec 11: Confirmar Picking
 * 
 * FR-067: Confirmar picking de pedido "Comprometido"
 * FR-068: Cambiar producto de "Comprometido" a "Picking"
 * FR-069: Registrar MovimientoInventario tipo "Picking"
 * FR-070: Impedir confirmar picking de pedido ya procesado
 * FR-071: Actualizar estado a "En Picking"
 * FR-072: Confirmar picking con unidades recolectadas
 * FR-073: Registrar MovimientoInventario tipo "Faltante_Picking"
 */
@Service
public class ConfirmarPickingUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarPickingUseCase.class);

    private final PedidoRepository pedidoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;
    private final LoteComprometidoRepository loteComprometidoRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ExcepcionInventarioRepository excepcionRepository;

    public ConfirmarPickingUseCase(PedidoRepository pedidoRepository,
                                    ProductoPedidoRepository productoPedidoRepository,
                                    LoteComprometidoRepository loteComprometidoRepository,
                                    LoteRepository loteRepository,
                                    MovimientoInventarioRepository movimientoRepository,
                                    ExcepcionInventarioRepository excepcionRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
        this.loteComprometidoRepository = loteComprometidoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.excepcionRepository = excepcionRepository;
    }

    public record ConfirmarPickingCommand(
            UUID pedidoId,
            UUID operarioId,
            List<LineaPickingCommand> lineasRecolectadas
    ) {}

    public record LineaPickingCommand(
            UUID productoPedidoId,
            Integer cantidadRecolectada
    ) {}

    public record ConfirmarPickingResult(
            boolean exitoso,
            String numeroPedido,
            EstadoPedido nuevoEstado,
            List<String> alertas
    ) {}

    @Transactional
    public ConfirmarPickingResult ejecutar(ConfirmarPickingCommand command) {
        log.info("Confirmando picking para pedido: {}, operario: {}", 
                command.pedidoId(), command.operarioId());

        // 1. Buscar pedido
        Pedido pedido = pedidoRepository.findById(command.pedidoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido no encontrado: " + command.pedidoId()));

        // 2. Validar estado (FR-070): Solo COMPROMETIDO puede pasar a PICKING
        if (pedido.getEstado() != EstadoPedido.COMPROMETIDO) {
            throw new PedidoEstadoInvalidoException(
                    pedido.getNumeroPedido(), pedido.getEstado().name());
        }

        // 3. Obtener líneas del pedido
        List<ProductoPedido> lineasPedido = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());

        // 4. Obtener lotes comprometidos por cada línea
        Map<UUID, LoteComprometido> lotesPorProductoPedidoId = new java.util.HashMap<>();
        for (ProductoPedido linea : lineasPedido) {
            List<LoteComprometido> lotes = loteComprometidoRepository.findByProductoPedidoId(linea.getProductoPedidoId());
            if (!lotes.isEmpty()) {
                lotesPorProductoPedidoId.put(linea.getProductoPedidoId(), lotes.get(0));
            }
        }

        // 5. Procesar cada línea con cantidades recolectadas
        List<String> alertas = new ArrayList<>();
        Map<UUID, Integer> cantidadesRecolectadas = command.lineasRecolectadas().stream()
                .collect(Collectors.toMap(
                        LineaPickingCommand::productoPedidoId,
                        LineaPickingCommand::cantidadRecolectada
                ));

        for (ProductoPedido linea : lineasPedido) {
            Integer cantidadRecolectada = cantidadesRecolectadas.getOrDefault(
                    linea.getProductoPedidoId(), 0);

            // Obtener lote comprometido para esta línea
            LoteComprometido loteComprometido = lotesPorProductoPedidoId.get(linea.getProductoPedidoId());
            String codigoLote = loteComprometido != null ? loteComprometido.getCodigoLote() : null;

            List<String> alertasLinea = procesarLineaPicking(
                    pedido, linea, cantidadRecolectada, command.operarioId(), codigoLote);
            alertas.addAll(alertasLinea);
        }

        // 6. Actualizar pedido a "En Picking" (FR-071)
        pedido.setEstado(EstadoPedido.EN_PICKING);
        pedidoRepository.update(pedido);

        log.info("Picking confirmado para pedido {}. Estado: EN_PICKING. Alertas: {}", 
                pedido.getNumeroPedido(), alertas.size());

        return new ConfirmarPickingResult(
                true, pedido.getNumeroPedido(), EstadoPedido.EN_PICKING, alertas);
    }

    /**
     * Procesa una línea de picking.
     * Si cantidadRecolectada < cantidadConfirmada, busca lotes alternativos con FEFO.
     * Si no hay más stock, registra excepción por faltante.
     */
    private List<String> procesarLineaPicking(Pedido pedido, ProductoPedido linea,
                                               Integer cantidadRecolectada, UUID operarioId, String codigoLote) {
        List<String> alertas = new ArrayList<>();
        int cantidadConfirmada = linea.getCantidadConfirmada();

        // Si no se recolectó nada, registrar excepción y continuar
        if (cantidadRecolectada == null || cantidadRecolectada == 0) {
            registrarExcepcionFaltante(pedido, linea, cantidadConfirmada, operarioId, codigoLote);
            alertas.add("Línea %s: 0 unidades recolectadas de %d confirmadas"
                    .formatted(linea.getProductoPedidoId(), cantidadConfirmada));
            return alertas;
        }

        // Si recolectó menos de lo confirmado, buscar lotes alternativos con FEFO
        if (cantidadRecolectada < cantidadConfirmada) {
            int faltante = cantidadConfirmada - cantidadRecolectada;
            
            // Buscar lotes alternativos con FEFO
            List<Lote> lotesAlternativos = loteRepository.findBySkuIdWithStock(linea.getSkuId()).stream()
                    .sorted(Comparator.comparing(Lote::getFechaVencimiento))
                    .toList();

            List<String> lotesSugeridos = new ArrayList<>();

            for (Lote lote : lotesAlternativos) {
                if (faltante <= 0) break;
                
                // Solo usar lotes que no están ya comprometidos para este pedido
                List<LoteComprometido> compromisosExistentes = 
                        loteComprometidoRepository.findByProductoPedidoId(linea.getProductoPedidoId());
                boolean yaComprometido = compromisosExistentes.stream()
                        .anyMatch(c -> c.getCodigoLote().equals(lote.getCodigoLote()));
                
                if (!yaComprometido && lote.getCantidad() > 0) {
                    int aTomar = Math.min(faltante, lote.getCantidad());
                    faltante -= aTomar;
                    lotesSugeridos.add("Lote %s: %d unidades (venc: %s)"
                            .formatted(lote.getCodigoLote(), aTomar, lote.getFechaVencimiento()));
                }
            }

            // Si encontró lotes alternativos, informar al operario
            if (!lotesSugeridos.isEmpty()) {
                alertas.add("Línea %s: Faltan %d unidades. Lotes alternativos disponibles: %s"
                        .formatted(linea.getProductoPedidoId(), 
                                cantidadConfirmada - cantidadRecolectada,
                                String.join(", ", lotesSugeridos)));
            }

            // Si aún falta stock después de buscar alternativos, registrar excepción
            if (faltante > 0) {
                registrarExcepcionFaltante(pedido, linea, faltante, operarioId, codigoLote);
                alertas.add("Línea %s: Faltante de %d unidades - Excepción registrada"
                        .formatted(linea.getProductoPedidoId(), faltante));
            }
        }

        // Registrar movimiento de inventario tipo PICKING
        registrarMovimientoPicking(pedido, linea, cantidadRecolectada, operarioId, codigoLote);

        return alertas;
    }

    /**
     * Registra excepción por faltante en picking.
     */
    private void registrarExcepcionFaltante(Pedido pedido, ProductoPedido linea, 
                                             int cantidadFaltante, UUID operarioId, String codigoLote) {
        ExcepcionInventario excepcion = ExcepcionInventario.builder()
                .excepcionId(UUID.randomUUID())
                .tipoExcepcion(TipoExcepcion.FALTANTE)
                .codigoLote(codigoLote)
                .skuId(linea.getSkuId())
                .cantidadAfectada(cantidadFaltante)
                .fechaRegistro(LocalDateTime.now())
                .operarioId(operarioId.toString())
                .descripcion("Faltante en picking para pedido %s, SKU %s, Lote %s: %d unidades no recolectadas"
                        .formatted(pedido.getNumeroPedido(), linea.getSkuId(), codigoLote, cantidadFaltante))
                .evidenciaUrl(null)
                .build();

        excepcionRepository.save(excepcion);
    }

    /**
     * Registra movimiento de inventario tipo PICKING.
     */
    private void registrarMovimientoPicking(Pedido pedido, ProductoPedido linea, 
                                             int cantidad, UUID operarioId, String codigoLote) {
        MovimientoInventario movimiento = MovimientoInventario.builder()
                .movimientoId(UUID.randomUUID())
                .codigoLote(codigoLote)
                .tipoMovimiento(TipoMovimiento.PICKING)
                .cantidad(-cantidad)
                .fechaMovimiento(LocalDateTime.now())
                .pedidoId(pedido.getPedidoId())
                .operarioId(operarioId)
                .observaciones("Picking confirmado para pedido %s, SKU %s, Lote %s: %d unidades"
                        .formatted(pedido.getNumeroPedido(), linea.getSkuId(), codigoLote, cantidad))
                .build();

        movimientoRepository.save(movimiento);
        log.info("Movimiento PICKING registrado: pedido={}, sku={}, lote={}, cantidad={}",
                pedido.getNumeroPedido(), linea.getSkuId(), codigoLote, -cantidad);
    }
}
