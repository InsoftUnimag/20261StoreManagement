package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.LoteComprometido;
import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.model.ProductoPedido;
import com.distribuidoras.inventario.domain.repository.ClienteServicePort;
import com.distribuidoras.inventario.domain.repository.LoteComprometidoRepository;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.OperarioServicePort;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.domain.exception.PedidoNotFoundException;
import com.distribuidoras.inventario.infrastructure.web.dto.AsesorInfoDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.ClienteInfoDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.LineaPedidoResponseDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.LoteComprometidoDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.OperarioInfoDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.PedidoResponseDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.ProductoInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

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
        private final LoteRepository loteRepository;
        private final ProductoRepository productoRepository;
        private final ClienteServicePort clienteServicePort;
        private final OperarioServicePort operarioServicePort;

        public ConsultarDetallePedidoUseCase(PedidoRepository pedidoRepository,
                        ProductoPedidoRepository productoPedidoRepository,
                        LoteComprometidoRepository loteComprometidoRepository,
                        LoteRepository loteRepository,
                        ProductoRepository productoRepository,
                        ClienteServicePort clienteServicePort,
                        OperarioServicePort operarioServicePort) {
                this.pedidoRepository = pedidoRepository;
                this.productoPedidoRepository = productoPedidoRepository;
                this.loteComprometidoRepository = loteComprometidoRepository;
                this.loteRepository = loteRepository;
                this.productoRepository = productoRepository;
                this.clienteServicePort = clienteServicePort;
                this.operarioServicePort = operarioServicePort;
        }

        /**
         * Ejecuta la consulta por ID o número de pedido.
         */
        @Transactional(readOnly = true)
        public PedidoResponseDTO ejecutar(String identifier) {
                log.info("Consultando detalle de pedido: {}", identifier);

                // 1. Buscar pedido por ID o numero_pedido
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

                BigDecimal pesoLogisticoTotal = lineasDTO.stream()
                                .filter(l -> l.producto() != null && l.producto().pesoLogisticoKg() != null
                                                && l.cantidadConfirmada() != null)
                                .map(l -> l.producto().pesoLogisticoKg()
                                                .multiply(BigDecimal.valueOf(l.cantidadConfirmada())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                String tipoCumplimiento = totalSolicitado > 0 && totalSolicitado.equals(totalConfirmado) ? "COMPLETO"
                                : "PARCIAL";

                return PedidoResponseDTO.builder()
                                .pedidoId(pedido.getPedidoId())
                                .numeroPedido(pedido.getNumeroPedido())
                                .estado(pedido.getEstado().name())
                                .fechaCreacion(pedido.getFechaCreacion())
                                .fechaCompromiso(pedido.getFechaCompromiso())
                                .rutaId(pedido.getRutaId())
                                .cliente(clienteInfo)
                                .clienteNombre(pedido.getClienteNombre())
                                .asesor(new AsesorInfoDTO(pedido.getAsesorId(), "Asesor"))
                                .operarioPicking(mapearOperario(pedido.getOperarioPickingId()))
                                .operarioDespacho(mapearOperario(pedido.getOperarioDespachoId()))
                                .direccionEntrega(pedido.getDireccionEntrega())
                                .lineas(lineasDTO)
                                .totalSolicitado(totalSolicitado)
                                .totalConfirmado(totalConfirmado)
                .pesoLogisticoTotal(pesoLogisticoTotal)
                .tipoCumplimiento(tipoCumplimiento)
                .costoTotal(pedido.getCostoTotal())
                .build();
        }

        /**
         * Busca pedido por ID o número.
         */
        private Pedido buscarPedido(String identifier) {
                Optional<Pedido> pedido;

                try {
                        // Intentar como ID numérico primero
                        Long id = Long.parseLong(identifier);
                        pedido = pedidoRepository.findById(id);
                } catch (IllegalArgumentException e) {
                        // Si no es ID numérico válido, buscar por numero_pedido
                        pedido = pedidoRepository.findByNumeroPedido(identifier);
                }

                return pedido.orElseThrow(() -> {
                        log.warn("Pedido no encontrado: {}", identifier);
                        return new PedidoNotFoundException("Pedido '" + identifier + "' no encontrado");
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
         * Mapea un ID de operario a su DTO de información.
         */
        private OperarioInfoDTO mapearOperario(Long operarioId) {
                if (operarioId == null)
                        return null;
                return operarioServicePort.findById(operarioId)
                                .map(o -> OperarioInfoDTO.builder()
                                                .id(o.getOperarioId())
                                                .nombre(o.getNombre())
                                                .cedula(o.getCedula())
                                                .rol(o.getRol() != null ? o.getRol().name() : null)
                                                .build())
                                .orElse(OperarioInfoDTO.builder()
                                                .id(operarioId)
                                                .nombre("Operario no disponible")
                                                .build());
        }

        /**
         * Transforma ProductoPedido a DTO con lotes si están comprometidos.
         */
        private LineaPedidoResponseDTO toLineaDTO(ProductoPedido linea) {
                // Obtener info del producto
                Producto producto = productoRepository.findById(linea.getSkuId())
                                .orElse(null);

                ProductoInfoDTO productoInfo = producto != null ? ProductoInfoDTO.builder()
                                .skuId(producto.getSkuId())
                                .marca(producto.getMarca())
                                .presentacion(producto.getPresentacion())
                                .contenidoMl(producto.getContenidoMl())
                                .pesoLogisticoKg(producto.getPesoLogisticoKg())
                                .build() : null;

                // Obtener lotes comprometidos si existen
                List<LoteComprometidoDTO> lotesDTO = loteComprometidoRepository
                                .findByProductoPedidoId(linea.getProductoPedidoId()).stream()
                                .map(this::toLoteComprometidoDTO)
                                .toList();

                return LineaPedidoResponseDTO.builder()
                                .productoPedidoId(linea.getProductoPedidoId())
                                .skuId(linea.getSkuId())
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
                java.time.LocalDate vencimiento = loteRepository.findById(lc.getCodigoLote())
                                .map(com.distribuidoras.inventario.domain.model.Lote::getFechaVencimiento)
                                .orElse(null);

                return LoteComprometidoDTO.builder()
                                .compromisoId(lc.getCompromisoId())
                                .codigoLote(lc.getCodigoLote())
                                .cantidadComprometida(lc.getCantidadComprometida())
                                .fechaVencimiento(vencimiento)
                                .build();
        }
}
