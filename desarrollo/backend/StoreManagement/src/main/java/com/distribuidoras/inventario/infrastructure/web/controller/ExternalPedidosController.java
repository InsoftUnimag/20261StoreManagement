package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.model.ProductoPedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.ProductoEnPedidoDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.ProductosPedidoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
public class ExternalPedidosController {

        private static final Logger log = LoggerFactory.getLogger(ExternalPedidosController.class);

        private final PedidoRepository pedidoRepository;
        private final ProductoPedidoRepository productoPedidoRepository;
        private final ProductoRepository productoRepository;

        public ExternalPedidosController(PedidoRepository pedidoRepository,
                        ProductoPedidoRepository productoPedidoRepository,
                        ProductoRepository productoRepository) {
                this.pedidoRepository = pedidoRepository;
                this.productoPedidoRepository = productoPedidoRepository;
                this.productoRepository = productoRepository;
        }

        @GetMapping("/{pedidoId}/productos")
        public ResponseEntity<ProductosPedidoResponse> obtenerProductosDelPedido(
                        @PathVariable String pedidoId) {

                log.info("REST: Consultando productos del pedido {}", pedidoId);

                Pedido pedido = pedidoRepository.findById(Long.parseLong(pedidoId))
                                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + pedidoId));

                List<ProductoPedido> lineas = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());

                List<ProductoEnPedidoDTO> productos = lineas.stream()
                                .map(this::toProductoEnPedidoDTO)
                                .toList();

                return ResponseEntity.ok(new ProductosPedidoResponse(productos));
        }

        private ProductoEnPedidoDTO toProductoEnPedidoDTO(ProductoPedido linea) {
                Producto producto = productoRepository.findById(linea.getSkuId()).orElse(null);
                var stock = stockGlobalRepository.findById(linea.getSkuId()).orElse(null);

                String nombre = producto != null ? producto.getMarca() + " " + producto.getPresentacion() : "N/A";
                BigDecimal precioUnitario = linea.getPrecioUnitario() != null
                                ? linea.getPrecioUnitario()
                                : BigDecimal.ZERO;
                int cantidad = linea.getCantidadConfirmada() != null ? linea.getCantidadConfirmada() : 0;
                BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));

                return new ProductoEnPedidoDTO(
                                linea.getSkuId(),
                                nombre,
                                cantidad,
                                precioUnitario,
                                subtotal);
        }
}
