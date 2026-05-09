package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.domain.model.*;
import com.distribuidoras.inventario.domain.repository.*;
import com.distribuidoras.inventario.infrastructure.web.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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

    public ExternalPedidosController(PedidoRepository pedidoRepository,
                                      ProductoPedidoRepository productoPedidoRepository,
                                      ProductoRepository productoRepository,
                                      ClienteServicePort clienteServicePort) {
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
        this.productoRepository = productoRepository;
        this.clienteServicePort = clienteServicePort;
    }

    /**
     * GET /api/v1/external/pedidos/{pedidoId}?modulo=transporte|financiero
     * 
     * @param pedidoId UUID del pedido
     * @param modulo Módulo solicitante (transporte|financiero)
     * @return PedidoExternalDTO con datos según el módulo
     * 
     * Spec 09 FR-094: Módulo 2 recibe: cliente, dirección entrega, SKUs, cantidades despachadas, peso logístico total
     * Spec 09 FR-095: Módulo 3 recibe: cliente, NIT, SKUs, cantidad solicitada, cantidad despachada, indicador Completo/Parcial
     */
    @GetMapping("/{pedidoId}")
    public ResponseEntity<PedidoExternalDTO> obtenerPedidoParaModulo(
            @PathVariable String pedidoId,
            @RequestParam String modulo) {
        
        log.info("REST External: Consultando pedido {} para módulo {}", pedidoId, modulo);
        
        Pedido pedido = pedidoRepository.findById(UUID.fromString(pedidoId))
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + pedidoId));
        
        List<ProductoPedido> lineas = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());
        
        ClienteExternalDTO clienteInfo = obtenerClienteInfo(pedido.getClienteCc());
        
        boolean esTransporte = "transporte".equalsIgnoreCase(modulo);
        
        List<LineaExternalDTO> lineasDTO = lineas.stream()
                .map(linea -> toLineaExternalDTO(linea, esTransporte))
                .toList();
        
        BigDecimal pesoTotal = lineasDTO.stream()
                .map(LineaExternalDTO::pesoLogisticoTotal)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        String indicadorParcial = lineas.stream()
                .anyMatch(l -> l.getCantidadConfirmada() != null && l.getCantidadSolicitada() != null 
                        && !l.getCantidadConfirmada().equals(l.getCantidadSolicitada()))
                ? "Parcial" : "Completo";
        
        PedidoExternalDTO response = PedidoExternalDTO.builder()
                .pedidoId(pedido.getPedidoId().toString())
                .numeroPedido(pedido.getNumeroPedido())
                .estado(pedido.getEstado().name())
                .fechaCreacion(pedido.getFechaCreacion())
                .fechaCompromiso(pedido.getFechaCompromiso())
                .rutaId(pedido.getRutaId() != null ? pedido.getRutaId().toString() : null)
                .cliente(clienteInfo)
                .lineas(lineasDTO)
                .pesoLogisticoTotal(esTransporte ? pesoTotal : null)
                .indicadorParcial(esTransporte ? null : indicadorParcial)
                .precioTotal(BigDecimal.ZERO)
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
     * Transforma ProductoPedido a LineaExternalDTO diferenciando por módulo.
     * Transporte: retorna cantidadConfirmada (despachada) y peso logístico
     * Financiero: retorna cantidadSolicitada y cantidadConfirmada para indicador parcial
     */
    private LineaExternalDTO toLineaExternalDTO(ProductoPedido linea, boolean esTransporte) {
        Producto producto = productoRepository.findById(linea.getSkuId()).orElse(null);
        
        BigDecimal pesoUnitario = producto != null ? producto.getPesoLogisticoKg() : BigDecimal.ZERO;
        int cantidad = esTransporte 
                ? (linea.getCantidadConfirmada() != null ? linea.getCantidadConfirmada() : 0)
                : (linea.getCantidadConfirmada() != null ? linea.getCantidadConfirmada() : 0);
        BigDecimal pesoTotal = pesoUnitario != null ? 
                pesoUnitario.multiply(BigDecimal.valueOf(cantidad)) : 
                BigDecimal.ZERO;
        
        return LineaExternalDTO.builder()
                .skuId(linea.getSkuId())
                .marca(producto != null ? producto.getMarca() : "N/A")
                .presentacion(producto != null ? producto.getPresentacion() : "N/A")
                .cantidadSolicitada(linea.getCantidadSolicitada())
                .cantidadConfirmada(linea.getCantidadConfirmada())
                .pesoLogisticoUnitario(esTransporte ? pesoUnitario : null)
                .pesoLogisticoTotal(esTransporte ? pesoTotal : null)
                .build();
    }
}
