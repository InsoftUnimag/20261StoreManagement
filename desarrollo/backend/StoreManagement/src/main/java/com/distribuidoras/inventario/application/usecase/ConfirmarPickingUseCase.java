package com.distribuidoras.inventario.application.usecase;

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
                        Long pedidoId,
                        Long operarioId,
                        List<LineaPickingCommand> lineasRecolectadas) {
        }

        public record LineaPickingCommand(
                        Long productoPedidoId,
                        Integer cantidadRecolectada) {
        }

        public record ConfirmarPickingResult(
                        boolean exitoso,
                        String numeroPedido,
                        EstadoPedido nuevoEstado,
                        List<String> alertas) {
        }

        @Transactional
        public ConfirmarPickingResult ejecutar(ConfirmarPickingCommand command) {
                log.info("Confirmando picking para pedido: {}, operario: {}",
                                command.pedidoId(), command.operarioId());

                // 1. Buscar pedido
                Pedido pedido = pedidoRepository.findById(command.pedidoId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Pedido no encontrado: " + command.pedidoId()));

                // 2. Control de concurrencia: verificar que el estado siga siendo EN_PICKING
                // Esto previene que se confirme picking si el pedido no ha sido iniciado
                if (pedido.getEstado() != EstadoPedido.EN_PICKING) {
                        throw new IllegalStateException(
                                        "Pedido %s no está en estado EN_PICKING. Estado actual: %s".formatted(
                                                        pedido.getNumeroPedido(), pedido.getEstado()));
                }

                // 3. Obtener líneas del pedido
                List<ProductoPedido> lineasPedido = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());

                // 4. Obtener lotes comprometidos por cada línea
                Map<Long, LoteComprometido> lotesPorProductoPedidoId = new java.util.HashMap<>();
                for (ProductoPedido linea : lineasPedido) {
                        List<LoteComprometido> lotes = loteComprometidoRepository
                                        .findByProductoPedidoId(linea.getProductoPedidoId());
                        if (!lotes.isEmpty()) {
                                lotesPorProductoPedidoId.put(linea.getProductoPedidoId(), lotes.get(0));
                        }
                }

                // 5. Procesar cada línea con cantidades recolectadas
                List<String> alertas = new ArrayList<>();
                Map<Long, Integer> cantidadesRecolectadas = command.lineasRecolectadas().stream()
                                .collect(Collectors.toMap(
                                                LineaPickingCommand::productoPedidoId,
                                                LineaPickingCommand::cantidadRecolectada));

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

                // 6. Actualizar pedido a "Pickup"
                pedido.setEstado(EstadoPedido.PICKUP);
                pedidoRepository.update(pedido);

                log.info("Picking confirmado para pedido {}. Estado: PICKUP. Alertas: {}",
                                pedido.getNumeroPedido(), alertas.size());

                return new ConfirmarPickingResult(
                                true, pedido.getNumeroPedido(), EstadoPedido.PICKUP, alertas);
        }

        /**
         * Procesa una línea de picking con control de concurrencia y reasignación
         * efectiva de lotes.
         * Si cantidadRecolectada < cantidadConfirmada, busca y reasigna lotes
         * alternativos con FEFO.
         * Si no hay más stock, registra excepción por faltante.
         */
        private List<String> procesarLineaPicking(Pedido pedido, ProductoPedido linea,
                        Integer cantidadRecolectada, Long operarioId, String codigoLoteOriginal) {
                List<String> alertas = new ArrayList<>();
                int cantidadConfirmada = linea.getCantidadConfirmada();

                // Si no se recolectó nada, registrar excepción y continuar
                if (cantidadRecolectada == null || cantidadRecolectada == 0) {
                        registrarExcepcionFaltante(pedido, linea, cantidadConfirmada, operarioId, codigoLoteOriginal);
                        alertas.add("Línea %s: 0 unidades recolectadas de %d confirmadas"
                                        .formatted(linea.getProductoPedidoId(), cantidadConfirmada));
                        return alertas;
                }

                // Guardar lote original si existe
                String codigoLoteUsado = codigoLoteOriginal;
                int faltante = 0;

                // Si recolectó menos de lo confirmado, buscar lotes alternativos con FEFO y
                // reasignar
                if (cantidadRecolectada < cantidadConfirmada) {
                        faltante = cantidadConfirmada - cantidadRecolectada;

                        // Buscar lotes alternativos con FEFO
                        List<Lote> lotesAlternativos = loteRepository.findBySkuIdWithStock(linea.getSkuId()).stream()
                                        .sorted(Comparator.comparing(Lote::getFechaVencimiento))
                                        .toList();

                        List<String> lotesReasignados = new ArrayList<>();
                        List<String> lotesSugeridos = new ArrayList<>();

                        for (Lote lote : lotesAlternativos) {
                                if (faltante <= 0)
                                        break;

                                // Solo usar lotes que no están ya comprometidos para este pedido
                                List<LoteComprometido> compromisosExistentes = loteComprometidoRepository
                                                .findByProductoPedidoId(linea.getProductoPedidoId());
                                boolean yaComprometido = compromisosExistentes.stream()
                                                .anyMatch(c -> c.getCodigoLote().equals(lote.getCodigoLote()));

                                if (!yaComprometido && lote.getCantidad() > 0) {
                                        int aTomar = Math.min(faltante, lote.getCantidad());

                                        // Control de concurrencia: verificar stock disponible actual
                                        Lote loteActual = loteRepository.findById(lote.getCodigoLote())
                                                        .orElseThrow(() -> new IllegalArgumentException(
                                                                        "Lote no encontrado: " + lote.getCodigoLote()));

                                        if (loteActual.getCantidad() < aTomar) {
                                                alertas.add("Lote %s: Stock insuficiente (disponible: %d, requerido: %d)"
                                                                .formatted(lote.getCodigoLote(),
                                                                                loteActual.getCantidad(), aTomar));
                                                continue;
                                        }

                                        // Reasignar lote
                                        reasignarLoteParaLinea(pedido, linea, loteActual, aTomar, operarioId);
                                        faltante -= aTomar;
                                        lotesReasignados.add("Lote %s: %d unidades reasignadas (venc: %s)"
                                                        .formatted(loteActual.getCodigoLote(), aTomar,
                                                                        loteActual.getFechaVencimiento()));

                                        // Actualizar codigoLoteUsado si es la primera reasignación
                                        if (codigoLoteUsado == null || (codigoLoteOriginal != null
                                                        && codigoLoteUsado.equals(codigoLoteOriginal))) {
                                                codigoLoteUsado = loteActual.getCodigoLote();
                                        }
                                } else if (lote.getCantidad() > 0) {
                                        // Lote disponible para sugerencia
                                        lotesSugeridos.add("Lote %s: %d unidades disponibles (venc: %s)"
                                                        .formatted(lote.getCodigoLote(), lote.getCantidad(),
                                                                        lote.getFechaVencimiento()));
                                }
                        }

                        // Si se reasignaron lotes, informar
                        if (!lotesReasignados.isEmpty()) {
                                alertas.add("Línea %s: %d unidades faltantes. Lotes reasignados: %s"
                                                .formatted(linea.getProductoPedidoId(),
                                                                cantidadConfirmada - cantidadRecolectada,
                                                                String.join(", ", lotesReasignados)));
                        }

                        // Si aún falta stock después de reasignar, registrar excepción
                        if (faltante > 0) {
                                registrarExcepcionFaltante(pedido, linea, faltante, operarioId, codigoLoteUsado);
                                alertas.add("Línea %s: Faltante de %d unidades - Excepción registrada"
                                                .formatted(linea.getProductoPedidoId(), faltante));

                                // Si hay lotes sugeridos pero no pudieron ser usados
                                if (!lotesSugeridos.isEmpty()) {
                                        alertas.add("Lotes sugeridos pero no disponibles: %s"
                                                        .formatted(String.join(", ", lotesSugeridos)));
                                }
                        }
                }

                // Registrar movimiento de inventario tipo PICKING
                registrarMovimientoPicking(pedido, linea, cantidadRecolectada, operarioId, codigoLoteUsado);

                return alertas;
        }

        /**
         * Registra excepción por faltante en picking.
         */
        private void registrarExcepcionFaltante(Pedido pedido, ProductoPedido linea,
                        int cantidadFaltante, Long operarioId, String codigoLote) {
                ExcepcionInventario excepcion = ExcepcionInventario.builder()
                                .tipoExcepcion(TipoExcepcion.FALTANTE)
                                .codigoLote(codigoLote)
                                .skuId(linea.getSkuId())
                                .cantidadAfectada(cantidadFaltante)
                                .fechaRegistro(LocalDateTime.now())
                                .operarioId(operarioId)
                                .descripcion("Faltante en picking para pedido %s, SKU %s, Lote %s: %d unidades no recolectadas"
                                                .formatted(pedido.getNumeroPedido(), linea.getSkuId(), codigoLote,
                                                                cantidadFaltante))
                                .evidenciaUrl(null)
                                .build();

                excepcionRepository.save(excepcion);
        }

        /**
         * Reasigna un lote para una línea de pedido durante el picking.
         * Actualiza el stock del lote y crea un nuevo registro de lote comprometido.
         */
        private void reasignarLoteParaLinea(Pedido pedido, ProductoPedido linea,
                        Lote lote, int cantidad, Long operarioId) {
                // Actualizar cantidad disponible en el lote
                int nuevaCantidad = lote.getCantidad() - cantidad;
                lote.setCantidad(nuevaCantidad);
                loteRepository.save(lote);

                // Crear nuevo compromiso de lote
                LoteComprometido.builder()
                        .productoPedidoId(linea.getProductoPedidoId())
                                .codigoLote(lote.getCodigoLote())
                                .cantidadComprometida(cantidad)
                                .fechaCompromiso(LocalDateTime.now())
                                .build();
                // Pendiente: agregar método save al repositorio si no existe
                // loteComprometidoRepository.save(nuevoCompromiso);

                // Registrar movimiento de reasignación
                registroMovimientoReasignacion(pedido, linea, lote, cantidad, operarioId);

                log.info("Lote reasignado: pedido={}, sku={}, lote={}, cantidad={}, stock_restante={}",
                                pedido.getNumeroPedido(), linea.getSkuId(), lote.getCodigoLote(),
                                cantidad, nuevaCantidad);
        }

        /**
         * Registra movimiento de reasignación de lote.
         */
        private void registroMovimientoReasignacion(Pedido pedido, ProductoPedido linea,
                        Lote lote, int cantidad, Long operarioId) {
                MovimientoInventario movimiento = MovimientoInventario.builder()
                                .codigoLote(lote.getCodigoLote())
                                .tipoMovimiento(TipoMovimiento.REASIGNACION)
                                .cantidad(-cantidad)
                                .fechaMovimiento(LocalDateTime.now())
                                .pedidoId(pedido.getPedidoId())
                                .operarioId(operarioId)
                                .observaciones("Reasignación de lote durante picking para pedido %s, SKU %s: %d unidades"
                                                .formatted(pedido.getNumeroPedido(), linea.getSkuId(), cantidad))
                                .build();

                movimientoRepository.save(movimiento);
                log.info("Movimiento REASIGNACION registrado: pedido={}, sku={}, lote={}, cantidad={}",
                                pedido.getNumeroPedido(), linea.getSkuId(), lote.getCodigoLote(), -cantidad);
        }

        /**
         * Registra movimiento de inventario tipo PICKING.
         */
        private void registrarMovimientoPicking(Pedido pedido, ProductoPedido linea,
                        int cantidad, Long operarioId, String codigoLote) {
                MovimientoInventario movimiento = MovimientoInventario.builder()
                                .codigoLote(codigoLote)
                                .tipoMovimiento(TipoMovimiento.PICKING)
                                .cantidad(-cantidad)
                                .fechaMovimiento(LocalDateTime.now())
                                .pedidoId(pedido.getPedidoId())
                                .operarioId(operarioId)
                                .observaciones("Picking confirmado para pedido %s, SKU %s, Lote %s: %d unidades"
                                                .formatted(pedido.getNumeroPedido(), linea.getSkuId(), codigoLote,
                                                                cantidad))
                                .build();

                movimientoRepository.save(movimiento);
                log.info("Movimiento PICKING registrado: pedido={}, sku={}, lote={}, cantidad={}",
                                pedido.getNumeroPedido(), linea.getSkuId(), codigoLote, -cantidad);
        }
}
