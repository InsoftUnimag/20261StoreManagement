package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.PedidoEstadoInvalidoException;
import com.distribuidoras.inventario.domain.model.*;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.model.enums.TipoMovimiento;
import com.distribuidoras.inventario.domain.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ConfirmarDespachoUseCase {

        private static final Logger log = LoggerFactory.getLogger(ConfirmarDespachoUseCase.class);

        private final PedidoRepository pedidoRepository;
        private final ProductoPedidoRepository productoPedidoRepository;
        private final MovimientoInventarioRepository movimientoRepository;
        private final RegistroDespachoRepository registroDespachoRepository;
        private final LoteComprometidoRepository loteComprometidoRepository;
        private final LoteRepository loteRepository;
        private final com.distribuidoras.inventario.infrastructure.persistence.repository.StockGlobalSkuJpaRepository stockGlobalSkuRepository;

        public ConfirmarDespachoUseCase(PedidoRepository pedidoRepository,
                        ProductoPedidoRepository productoPedidoRepository,
                        MovimientoInventarioRepository movimientoRepository,
                        RegistroDespachoRepository registroDespachoRepository,
                        LoteComprometidoRepository loteComprometidoRepository,
                        LoteRepository loteRepository,
                        com.distribuidoras.inventario.infrastructure.persistence.repository.StockGlobalSkuJpaRepository stockGlobalSkuRepository) {
                this.pedidoRepository = pedidoRepository;
                this.productoPedidoRepository = productoPedidoRepository;
                this.movimientoRepository = movimientoRepository;
                this.registroDespachoRepository = registroDespachoRepository;
                this.loteComprometidoRepository = loteComprometidoRepository;
                this.loteRepository = loteRepository;
                this.stockGlobalSkuRepository = stockGlobalSkuRepository;
        }

        public record ConfirmarDespachoCommand(
                        UUID pedidoId,
                        UUID operarioId,
                        String transportista,
                        String placaVehiculo,
                        String observaciones,
                        Map<UUID, Integer> cantidadesDespachadas) {
        }

        public record ConfirmarDespachoResult(
                        boolean exitoso,
                        String numeroPedido,
                        EstadoPedido nuevoEstado,
                        boolean despachoParcial,
                        List<String> alertas) {
        }

        @Transactional
        public ConfirmarDespachoResult ejecutar(ConfirmarDespachoCommand command) {
                log.info("Confirmando despacho para pedido: {}, operario: {}",
                                command.pedidoId(), command.operarioId());

                // 1. Buscar pedido
                Pedido pedido = pedidoRepository.findById(command.pedidoId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Pedido no encontrado: " + command.pedidoId()));

                // 2. Validar estado (FR-081): Solo EN_PICKING puede pasar a DESPACHADO
                if (pedido.getEstado() != EstadoPedido.EN_PICKING) {
                        throw new PedidoEstadoInvalidoException(
                                        pedido.getNumeroPedido(), pedido.getEstado().name());
                }

                // 3. Obtener líneas del pedido
                List<ProductoPedido> lineasPedido = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());

                // 4. Obtener lotes comprometidos por cada línea del pedido
                Map<UUID, LoteComprometido> lotesPorProductoPedidoId = new HashMap<>();
                for (ProductoPedido linea : lineasPedido) {
                        List<LoteComprometido> lotes = loteComprometidoRepository
                                        .findByProductoPedidoId(linea.getProductoPedidoId());
                        if (!lotes.isEmpty()) {
                                lotesPorProductoPedidoId.put(linea.getProductoPedidoId(), lotes.get(0));
                        }
                }

                // 5. Procesar despacho por cada línea
                List<String> alertas = new ArrayList<>();
                boolean despachoParcial = false;

                for (ProductoPedido linea : lineasPedido) {
                        Integer cantidadDespachada = command.cantidadesDespachadas().getOrDefault(
                                        linea.getProductoPedidoId(), linea.getCantidadConfirmada());

                        // Validar despacho parcial
                        if (cantidadDespachada < linea.getCantidadConfirmada()) {
                                despachoParcial = true;
                                alertas.add("Línea %s: Despacho parcial - Confirmado: %d, Despachado: %d"
                                                .formatted(linea.getProductoPedidoId(),
                                                                linea.getCantidadConfirmada(), cantidadDespachada));
                        }

                        // Obtener lote comprometido para esta línea
                        LoteComprometido loteComprometido = lotesPorProductoPedidoId.get(linea.getProductoPedidoId());
                        String codigoLote = loteComprometido != null ? loteComprometido.getCodigoLote() : null;

                        // Reducir stock del lote físico (FR-071)
                        if (codigoLote != null) {
                                Optional<Lote> loteOpt = loteRepository.findById(codigoLote);
                                if (loteOpt.isPresent()) {
                                        Lote lote = loteOpt.get();
                                        int nuevoStock = lote.getCantidad() - cantidadDespachada;
                                        lote.setCantidad(Math.max(0, nuevoStock));
                                        loteRepository.save(lote);
                                        log.info("Stock reducido en lote {}: {} -> {}",
                                                        codigoLote, lote.getCantidad() + cantidadDespachada,
                                                        lote.getCantidad());
                                }
                        }

                        // Actualizar stock_global_sku (fisico_total y comprometidos)
                        actualizarStockDespacho(linea.getSkuId(), cantidadDespachada);

                        // Registrar movimiento de SALIDA con codigoLote correcto (FR-083)
                        registrarMovimientoSalida(pedido, linea, cantidadDespachada, command.operarioId(), codigoLote);

// Actualizar cantidad despachada en producto_pedido
            linea.setCantidadDespachada(cantidadDespachada);
            productoPedidoRepository.update(linea);
                }

                // Crear registro de despacho
                RegistroDespacho registro = RegistroDespacho.builder()
                                .registroDespachoId(UUID.randomUUID())
                                .pedidoId(pedido.getPedidoId())
                                .operarioId(command.operarioId())
                                .fechaDespacho(LocalDateTime.now())
                                .transportista(command.transportista())
                                .placaVehiculo(command.placaVehiculo())
                                .observaciones(command.observaciones())
                                .build();

                registroDespachoRepository.save(registro);

                // 6. Actualizar pedido a "Despachado" (FR-084)
                pedido.setEstado(EstadoPedido.DESPACHADO);
                pedidoRepository.update(pedido);

                log.info("Despacho confirmado para pedido {}. Parcial: {}. Alertas: {}",
                                pedido.getNumeroPedido(), despachoParcial, alertas.size());

                return new ConfirmarDespachoResult(
                                true, pedido.getNumeroPedido(), EstadoPedido.DESPACHADO, despachoParcial, alertas);
        }

        private void registrarMovimientoSalida(Pedido pedido, ProductoPedido linea,
                        int cantidad, UUID operarioId, String codigoLote) {
                MovimientoInventario movimiento = MovimientoInventario.builder()
                                .movimientoId(UUID.randomUUID())
                                .codigoLote(codigoLote)
                                .tipoMovimiento(TipoMovimiento.SALIDA)
                                .cantidad(-cantidad)
                                .fechaMovimiento(LocalDateTime.now())
                                .pedidoId(pedido.getPedidoId())
                                .operarioId(operarioId)
                                .observaciones("Despacho confirmado para pedido %s, SKU %s, Lote %s: %d unidades"
                                                .formatted(pedido.getNumeroPedido(), linea.getSkuId(), codigoLote,
                                                                cantidad))
                                .build();

                movimientoRepository.save(movimiento);
                log.info("Movimiento SALIDA registrado: pedido={}, sku={}, lote={}, cantidad={}",
                                pedido.getNumeroPedido(), linea.getSkuId(), codigoLote, -cantidad);
        }

        private void actualizarStockDespacho(String skuId, int cantidadDespachada) {
                var stockOpt = stockGlobalSkuRepository.findById(Objects.requireNonNull(skuId));
                if (stockOpt.isPresent()) {
                        var stock = stockOpt.get();
                        int nuevosComprometidos = Math.max(0, stock.getComprometidos() - cantidadDespachada);
                        int nuevosDisponibles = stock.getFisicoTotal() - nuevosComprometidos;
                        stock.setComprometidos(nuevosComprometidos);
                        stock.setDisponibles(nuevosDisponibles);
                        stockGlobalSkuRepository.save(stock);
                        log.info("Stock global actualizado para {} tras despacho: fisico_total={}, comprometidos={}, disponibles={}",
                                        skuId, stock.getFisicoTotal(), stock.getComprometidos(),
                                        stock.getDisponibles());
                }
        }
}