package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.DetalleDisponibilidadDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.DisponibilidadDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Use Case: Verificar disponibilidad de stock para los productos de un pedido.
 * Spec 06: Consultar Disponibilidad
 * 
 * FR-067: Permitir buscar e informar si hay producto en stock
 * FR-068: Hacer el proceso automáticamente antes de confirmar un pedido
 * SC-025: Realizar la consulta 100% de las veces antes de confirmar un pedido nuevo
 */
@Service
public class ConsultarDisponibilidadPedidoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarDisponibilidadPedidoUseCase.class);

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;

    public ConsultarDisponibilidadPedidoUseCase(ProductoRepository productoRepository,
                                                LoteRepository loteRepository) {
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
    }

    /**
     * Record que representa la línea de un pedido a validar.
     */
    public record LineaPedidoDTO(
            UUID skuId,
            Integer cantidadSolicitada
    ) {}

    /**
     * Ejecuta la validación de disponibilidad para un pedido.
     * 
     * @param pedidoId ID del pedido
     * @param lineas Lista de líneas del pedido (sku_id, cantidad)
     * @return DisponibilidadDTO con el resultado de la validación
     */
    @Transactional(readOnly = true)
    public DisponibilidadDTO ejecutar(String pedidoId, List<LineaPedidoDTO> lineas) {
        log.info("Verificando disponibilidad para pedido: {}, {} líneas", pedidoId, lineas.size());

        // 1. Obtener todos los SKU únicos
        List<UUID> skuIds = lineas.stream()
                .map(LineaPedidoDTO::skuId)
                .distinct()
                .toList();

        // 2. Query eficiente: obtener stock de todos los SKUs en una sola consulta
        Map<UUID, List<Lote>> lotesPorSku = loteRepository.findBySkuIdsWithStock(skuIds);
        Map<UUID, Producto> productos = productoRepository.findByIds(skuIds);

        // 3. Validar disponibilidad por cada línea del pedido - Functional approach
        List<DetalleDisponibilidadDTO> detalles = lineas.stream()
                .map(linea -> validarLinea(linea, lotesPorSku, productos))
                .toList();

        // 4. Determinar si el pedido está completo
        boolean todosCumplen = detalles.stream()
                .allMatch(DetalleDisponibilidadDTO::cumple);

        // 5. Generar mensaje de alerta si hay insuficiencia
        String mensajeAlerta = todosCumplen 
                ? null 
                : generarMensajeAlerta(detalles);

        DisponibilidadDTO resultado = DisponibilidadDTO.builder()
                .pedidoId(pedidoId)
                .disponible(todosCumplen)
                .detalles(detalles)
                .mensajeAlerta(mensajeAlerta)
                .build();

        log.info("Pedido {} - Disponible: {}, Detalles: {}", 
                pedidoId, todosCumplen, detalles.size());

        return resultado;
    }

    /**
     * Valida una línea de pedido usando composición funcional.
     */
    private DetalleDisponibilidadDTO validarLinea(LineaPedidoDTO linea,
                                                    Map<UUID, List<Lote>> lotesPorSku,
                                                    Map<UUID, Producto> productos) {
        UUID skuId = linea.skuId();
        Integer cantidadSolicitada = linea.cantidadSolicitada();

        // Obtener producto (si existe)
        Producto producto = productos.get(skuId);
        if (producto == null) {
            return DetalleDisponibilidadDTO.builder()
                    .skuId(skuId.toString())
                    .marca("N/A")
                    .presentacion("N/A")
                    .cantidadSolicitada(cantidadSolicitada)
                    .cantidadDisponible(0)
                    .cumple(false)
                    .mensaje("SKU no encontrado en el catálogo")
                    .build();
        }

        // Calcular stock disponible - Functional reduction
        Integer cantidadDisponible = lotesPorSku.getOrDefault(skuId, List.of()).stream()
                .mapToInt(Lote::getCantidad)
                .sum();

        boolean cumple = cantidadDisponible >= cantidadSolicitada;

        String mensaje = cumple
                ? "Stock suficiente"
                : "Falta stock para el producto en el momento. Disponible: %d, Solicitado: %d"
                        .formatted(cantidadDisponible, cantidadSolicitada);

        return DetalleDisponibilidadDTO.builder()
                .skuId(skuId.toString())
                .marca(producto.getMarca())
                .presentacion(producto.getPresentacion())
                .cantidadSolicitada(cantidadSolicitada)
                .cantidadDisponible(cantidadDisponible)
                .cumple(cumple)
                .mensaje(mensaje)
                .build();
    }

    /**
     * Genera mensaje de alerta consolidado.
     */
    private String generarMensajeAlerta(List<DetalleDisponibilidadDTO> detalles) {
        long noCumplen = detalles.stream()
                .filter(d -> !d.cumple())
                .count();

        return "Hay %d producto(s) sin stock suficiente de %d solicitado(s)".formatted(
                noCumplen, detalles.size());
    }
}
