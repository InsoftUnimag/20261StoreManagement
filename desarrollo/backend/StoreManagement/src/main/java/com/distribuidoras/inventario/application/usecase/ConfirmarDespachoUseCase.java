package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.PedidoEstadoInvalidoException;
import com.distribuidoras.inventario.domain.model.MovimientoInventario;
import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.ProductoPedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.model.enums.TipoMovimiento;
import com.distribuidoras.inventario.domain.repository.MovimientoInventarioRepository;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use Case: Confirmar Despacho de pedido.
 * Spec 14: Confirmar Despacho
 * 
 * FR-081: Confirmar despacho solo de pedidos en "En Picking"
 * FR-082: Cambiar de "Picking" a "Despachado"
 * FR-083: Registrar MovimientoInventario tipo "Salida"
 * FR-084: Cambiar estado a "Despachado"
 * FR-085: Soportar despacho parcial
 * FR-086: Operación atómica
 * FR-087: Control de concurrencia
 */
@Service
public class ConfirmarDespachoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarDespachoUseCase.class);

    private final PedidoRepository pedidoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public ConfirmarDespachoUseCase(PedidoRepository pedidoRepository,
                                     ProductoPedidoRepository productoPedidoRepository,
                                     MovimientoInventarioRepository movimientoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    public record ConfirmarDespachoCommand(
            UUID pedidoId,
            UUID operarioId,
            Map<UUID, Integer> cantidadesDespachadas  // productoPedidoId -> cantidad_real
    ) {}

    public record ConfirmarDespachoResult(
            boolean exitoso,
            String numeroPedido,
            EstadoPedido nuevoEstado,
            boolean despachoParcial,
            List<String> alertas
    ) {}

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

        // 4. Procesar despacho por cada línea
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

            // Registrar movimiento de SALIDA (FR-083)
            registrarMovimientoSalida(pedido, linea, cantidadDespachada, command.operarioId());
        }

        // 5. Actualizar pedido a "Despachado" (FR-084)
        pedido.setEstado(EstadoPedido.DESPACHADO);
        pedidoRepository.update(pedido);

        log.info("Despacho confirmado para pedido {}. Parcial: {}. Alertas: {}", 
                pedido.getNumeroPedido(), despachoParcial, alertas.size());

        return new ConfirmarDespachoResult(
                true, pedido.getNumeroPedido(), EstadoPedido.DESPACHADO, despachoParcial, alertas);
    }

    /**
     * Registra movimiento de inventario tipo SALIDA.
     * FR-083: El stock ya se redujo al comprometer, solo registramos la salida.
     */
    private void registrarMovimientoSalida(Pedido pedido, ProductoPedido linea, 
                                            int cantidad, UUID operarioId) {
        MovimientoInventario movimiento = MovimientoInventario.builder()
                .movimientoId(UUID.randomUUID())
                .codigoLote(null)
                .tipoMovimiento(TipoMovimiento.SALIDA)
                .cantidad(-cantidad)
                .fechaMovimiento(LocalDateTime.now())
                .pedidoId(pedido.getPedidoId())
                .operarioId(operarioId)
                .observaciones("Despacho confirmado para pedido %s, SKU %s: %d unidades"
                        .formatted(pedido.getNumeroPedido(), linea.getSkuId(), cantidad))
                .build();

        movimientoRepository.save(movimiento);
    }
}
