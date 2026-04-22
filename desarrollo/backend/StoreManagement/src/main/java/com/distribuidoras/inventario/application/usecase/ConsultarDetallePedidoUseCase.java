package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.*;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.repository.*;
import com.distribuidoras.inventario.infrastructure.web.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case: Consultar Detalle de Pedido.
 * Spec 09: Consultar Detalle de Pedido
 * 
 * FR-093: Exponer detalle por número de pedido
 * FR-094: Retornar info para Módulo 2
 * FR-095: Retornar info para Módulo 3
 * FR-096: Error claro si no existe
 * FR-097: Incluir estado actual
 * FR-098: NO exponer detalles internos de lotes
 */
@Service
public class ConsultarDetallePedidoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarDetallePedidoUseCase.class);

    private final PedidoRepository pedidoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;
    private final LoteComprometidoRepository loteComprometidoRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final ClienteServicePort clienteServicePort;

    public ConsultarDetallePedidoUseCase(PedidoRepository pedidoRepository,
                                          ProductoPedidoRepository productoPedidoRepository,
                                          LoteComprometidoRepository loteComprometidoRepository,
                                          ProductoRepository productoRepository,
                                          LoteRepository loteRepository,
                                          ClienteServicePort clienteServicePort) {
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
        this.loteComprometidoRepository = loteComprometidoRepository;
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
        this.clienteServicePort = clienteServicePort;
    }

    /**
     * Ejecuta la consulta por ID o número de pedido.
     */
    @Transactional(readOnly = true)
    public PedidoResponseDTO ejecutar(String identifier) {
        log.info("Consultando detalle de pedido: {}", identifier);

        // 1. Buscar pedido por UUID o numero_pedido
        Pedido pedido = buscarPedido(identifier);

        // 2. Obtener líneas del pedido
        List<ProductoPedido> lineas = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());

        // 3. Obtener cliente (puede fallar si módulo externo no responde)
        ClienteInfoDTO clienteInfo = obtenerClienteInfo(pedido.getClienteCc());

        // 4. Construir response con líneas y lotes comprometidos
        List<LineaPedidoResponseDTO> lineasDTO = lineas.stream()
                .map(this::toLineaDTO)
                .toList();

        Integer totalSolicitado = lineas.stream().mapToInt(ProductoPedido::getCantidadSolicitada).sum();
        Integer totalConfirmado = lineas.stream().mapToInt(ProductoPedido::getCantidadConfirmada).sum();

        return PedidoResponseDTO.builder()
                .pedidoId(pedido.getPedidoId())
                .numeroPedido(pedido.getNumeroPedido())
                .estado(pedido.getEstado().name())
                .fechaCreacion(pedido.getFechaCreacion())
                .fechaCompromiso(pedido.getFechaCompromiso())
                .rutaId(pedido.getRutaId())
                .cliente(clienteInfo)
                .lineas(lineasDTO)
                .totalSolicitado(totalSolicitado)
                .totalConfirmado(totalConfirmado)
                .build();
    }

    /**
     * Busca pedido por UUID o número.
     */
    private Pedido buscarPedido(String identifier) {
        Optional<Pedido> pedido;
        
        try {
            // Intentar como UUID primero
            UUID id = UUID.fromString(identifier);
            pedido = pedidoRepository.findById(id);
        } catch (IllegalArgumentException e) {
            // Si no es UUID válido, buscar por numero_pedido
            pedido = pedidoRepository.findByNumeroPedido(identifier);
        }

        return pedido.orElseThrow(() -> {
            log.warn("Pedido no encontrado: {}", identifier);
            return new IllegalArgumentException("Pedido '" + identifier + "' no encontrado");
        });
    }

    /**
     * Obtiene info del cliente desde módulo externo.
     */
    private ClienteInfoDTO obtenerClienteInfo(String clienteCc) {
        return clienteServicePort.findByCedula(clienteCc)
                .map(cliente -> ClienteInfoDTO.builder()
                        .cedula(cliente.getCedula())
                        .nombre(cliente.getNombre())
                        .telefono(cliente.getTelefono())
                        .direccion(cliente.getDireccion())
                        .build())
                .orElse(ClienteInfoDTO.builder()
                        .cedula(clienteCc)
                        .nombre("Cliente no disponible")
                        .build());
    }

    /**
     * Transforma ProductoPedido a DTO con lotes si están comprometidos.
     */
    private LineaPedidoResponseDTO toLineaDTO(ProductoPedido linea) {
        // Obtener info del producto
        Producto producto = productoRepository.findById(linea.getSkuId())
                .orElse(null);

        ProductoInfoDTO productoInfo = producto != null ?
                ProductoInfoDTO.builder()
                        .skuId(producto.getSkuId())
                        .marca(producto.getMarca())
                        .presentacion(producto.getPresentacion())
                        .contenidoMl(producto.getContenidoMl())
                        .pesoLogisticoKg(producto.getPesoLogisticoKg())
                        .build() : null;

        // Obtener lotes comprometidos si existen
        List<LoteComprometidoDTO> lotesDTO = List.of();
        if (linea.getCantidadConfirmada() > 0) {
            lotesDTO = loteComprometidoRepository.findByProductoPedidoId(linea.getProductoPedidoId()).stream()
                    .map(this::toLoteComprometidoDTO)
                    .toList();
        }

        return LineaPedidoResponseDTO.builder()
                .productoPedidoId(linea.getProductoPedidoId())
                .producto(productoInfo)
                .cantidadSolicitada(linea.getCantidadSolicitada())
                .cantidadConfirmada(linea.getCantidadConfirmada())
                .lotesComprometidos(lotesDTO)
                .build();
    }

    /**
     * Transforma LoteComprometido a DTO.
     * FR-098: NO exponer info interna de lotes (solo código y fecha vencimiento)
     */
    private LoteComprometidoDTO toLoteComprometidoDTO(LoteComprometido lc) {
        return LoteComprometidoDTO.builder()
                .compromisoId(lc.getCompromisoId())
                .codigoLote(lc.getCodigoLote())
                .cantidadComprometida(lc.getCantidadComprometida())
                .fechaVencimiento(null)  // FR-098: NO exponer fecha vencimiento a externos
                .build();
    }
}
