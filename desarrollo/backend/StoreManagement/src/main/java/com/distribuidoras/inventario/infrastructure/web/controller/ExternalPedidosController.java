package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.*;
import com.distribuidoras.inventario.domain.model.*;
import com.distribuidoras.inventario.domain.repository.*;
import com.distribuidoras.inventario.infrastructure.web.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for external modules (Transporte/Financiero) to query pedido details.
 * Per document_Api.yml: GET /api/v1/external/pedidos/{pedidoId}?modulo=transporte|financiero
 * 
 * Spec 13: Solicitar Ruta - Módulo Transporte consulta datos de pedido
 * Spec 15: Ofrecer Datos Pedido - Módulo Financiero consulta datos de pedido
 */
@RestController
@RequestMapping("/api/v1/external/pedidos")
public class ExternalPedidosController {

    private static final Logger log = LoggerFactory.getLogger(ExternalPedidosController.class);

    private final PedidoRepository pedidoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;
    private final ProductoRepository productoRepository;
    private final ClienteServicePort clienteServicePort;
    private final LoteComprometidoRepository loteComprometidoRepository;
    private final LoteRepository loteRepository;

    public ExternalPedidosController(PedidoRepository pedidoRepository,
                                      ProductoPedidoRepository productoPedidoRepository,
                                      ProductoRepository productoRepository,
                                      ClienteServicePort clienteServicePort,
                                      LoteComprometidoRepository loteComprometidoRepository,
                                      LoteRepository loteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
        this.productoRepository = productoRepository;
        this.clienteServicePort = clienteServicePort;
        this.loteComprometidoRepository = loteComprometidoRepository;
        this.loteRepository = loteRepository;
    }

    /**
     * GET /api/v1/external/pedidos/{pedidoId}?modulo=transporte|financiero
     * 
     * @param pedidoId UUID del pedido
     * @param modulo Módulo solicitante (transporte|financiero)
     * @return PedidoExternalDTO con datos según el módulo
     */
    @GetMapping("/{pedidoId}")
    public ResponseEntity<PedidoExternalDTO> obtenerPedidoParaModulo(
            @PathVariable String pedidoId,
            @RequestParam String modulo) {
        
        log.info("REST External: Consultando pedido {} para módulo {}", pedidoId, modulo);
        
        Pedido pedido = pedidoRepository.findById(UUID.fromString(pedidoId))
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + pedidoId));
        
        List<ProductoPedido> lineas = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());
        
        // Obtener datos del cliente
        ClienteExternalDTO clienteInfo = obtenerClienteInfo(pedido.getClienteCc());
        
        // Construir líneas con información de producto
        List<LineaExternalDTO> lineasDTO = lineas.stream()
                .map(this::toLineaExternalDTO)
                .toList();
        
        // Calcular peso logístico total
        BigDecimal pesoTotal = lineasDTO.stream()
                .map(LineaExternalDTO::pesoLogisticoTotal)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Construir respuesta
        PedidoExternalDTO response = PedidoExternalDTO.builder()
                .pedidoId(pedido.getPedidoId().toString())
                .numeroPedido(pedido.getNumeroPedido())
                .estado(pedido.getEstado().name())
                .fechaCreacion(pedido.getFechaCreacion())
                .fechaCompromiso(pedido.getFechaCompromiso())
                .rutaId(pedido.getRutaId() != null ? pedido.getRutaId().toString() : null)
                .cliente(clienteInfo)
                .lineas(lineasDTO)
                .pesoLogisticoTotal(pesoTotal)
                .precioTotal(BigDecimal.ZERO)  // Placeholder - se calcularía con precios de producto
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene información del cliente desde módulo externo.
     */
    private ClienteExternalDTO obtenerClienteInfo(String clienteCc) {
        return clienteServicePort.findByCedula(clienteCc)
                .map(cliente -> ClienteExternalDTO.builder()
                        .cedula(cliente.getCedula())
                        .nombre(cliente.getNombre())
                        .direccion(cliente.getDireccion())
                        .telefono(cliente.getTelefono())
                        .build())
                .orElse(ClienteExternalDTO.builder()
                        .cedula(clienteCc)
                        .nombre("Cliente no disponible")
                        .direccion("")
                        .telefono("")
                        .build());
    }

    /**
     * Transforma ProductoPedido a LineaExternalDTO.
     */
    private LineaExternalDTO toLineaExternalDTO(ProductoPedido linea) {
        Producto producto = productoRepository.findById(linea.getSkuId()).orElse(null);
        
        BigDecimal pesoUnitario = producto != null ? producto.getPesoLogisticoKg() : BigDecimal.ZERO;
        BigDecimal pesoTotal = pesoUnitario != null ? 
                pesoUnitario.multiply(BigDecimal.valueOf(linea.getCantidadConfirmada() != null ? linea.getCantidadConfirmada() : 0)) : 
                BigDecimal.ZERO;
        
        return LineaExternalDTO.builder()
                .skuId(linea.getSkuId().toString())
                .marca(producto != null ? producto.getMarca() : "N/A")
                .presentacion(producto != null ? producto.getPresentacion() : "N/A")
                .cantidadSolicitada(linea.getCantidadSolicitada())
                .cantidadConfirmada(linea.getCantidadConfirmada())
                .pesoLogisticoUnitario(pesoUnitario)
                .pesoLogisticoTotal(pesoTotal)
                .build();
    }
}
