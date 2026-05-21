package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.StockInsuficienteException;
import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.model.ProductoPedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.infrastructure.messaging.PedidoCreadoProducer;
import com.distribuidoras.inventario.infrastructure.messaging.SolicitudRutaProducer;
import com.distribuidoras.inventario.infrastructure.persistence.repository.StockGlobalSkuJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case: Realizar Pedido (crear sin comprometer inventario).
 * Spec 08: Realizar Pedido - Parte 1
 * 
 * FR-052: Validar CC/NIT activo
 * FR-053: Ejecutar "Consultar disponibilidad"
 * FR-054: Verificar stock en momento exacto
 * FR-055: Rechazar si stock insuficiente
 * FR-056: Generar número único
 * FR-057: Dejar pedido en estado "Esperando Ruta"
 */
@Service
public class RealizarPedidoUseCase {

        private static final Logger log = LoggerFactory.getLogger(RealizarPedidoUseCase.class);

        private final ConsultarClienteUseCase consultarClienteUseCase;
        private final ProductoRepository productoRepository;
        private final PedidoRepository pedidoRepository;
        private final ProductoPedidoRepository productoPedidoRepository;
        private final PedidoCreadoProducer pedidoCreadoProducer;
        private final SolicitudRutaProducer solicitudRutaProducer;
        private final StockGlobalSkuJpaRepository stockGlobalSkuRepository;

        public RealizarPedidoUseCase(ConsultarClienteUseCase consultarClienteUseCase,
                        ProductoRepository productoRepository,
                        PedidoRepository pedidoRepository,
                        ProductoPedidoRepository productoPedidoRepository,
                        PedidoCreadoProducer pedidoCreadoProducer,
                        SolicitudRutaProducer solicitudRutaProducer,
                        StockGlobalSkuJpaRepository stockGlobalSkuRepository) {
                this.consultarClienteUseCase = Objects.requireNonNull(consultarClienteUseCase);
                this.productoRepository = Objects.requireNonNull(productoRepository);
                this.pedidoRepository = Objects.requireNonNull(pedidoRepository);
                this.productoPedidoRepository = Objects.requireNonNull(productoPedidoRepository);
                this.pedidoCreadoProducer = Objects.requireNonNull(pedidoCreadoProducer);
                this.solicitudRutaProducer = Objects.requireNonNull(solicitudRutaProducer);
                this.stockGlobalSkuRepository = Objects.requireNonNull(stockGlobalSkuRepository);
        }

        /**
         * Command object para crearar pedido.
         */
        public record PedidoCommand(
                        String clienteCc,
                        UUID asesorId,
                        List<LineaCommand> lineas) {
        }

        public record LineaCommand(
                        String skuId,
                        Integer cantidadSolicitada) {
        }

        /**
         * Ejecuta la creación de pedido.
         * 
         * @param command Datos del pedido
         * @return Pedido creado con líneas
         */
        @Transactional
        public Pedido ejecutar(PedidoCommand command) {
                log.info("Creando pedido para cliente: {}, asesor: {}, {} líneas",
                                command.clienteCc(), command.asesorId(), command.lineas().size());

                // 1. Validar cliente existe y está activo (FR-052)
                var cliente = consultarClienteUseCase.ejecutar(command.clienteCc());

                // 2. Validar SKUs existen (FR-053)
                Map<String, Producto> productos = validarYObtenerProductos(command.lineas());

                // 3. Validar stock disponible SIN comprometer (FR-054, FR-055)
                validarStockDisponible(command.lineas(), productos);

                // 4. Generar número único de pedido (FR-056)
                String numeroPedido = pedidoRepository.generarNumeroPedido(LocalDate.now());

                // 5. Crear Pedido en estado ESPERANDO_RUTA (FR-057)
                Pedido pedido = Pedido.builder()
                                .pedidoId(UUID.randomUUID())
                                .numeroPedido(numeroPedido)
                                .clienteCc(command.clienteCc())
                                .clienteNombre(cliente.getNombre())
                                .fechaCreacion(LocalDateTime.now())
                                .estado(EstadoPedido.ESPERANDO_RUTA)
                                .asesorId(command.asesorId())
                                .build();

                Pedido pedidoGuardado = pedidoRepository.save(pedido);

                // 6. Crear líneas de pedido
                List<ProductoPedido> lineas = command.lineas().stream()
                                .map(linea -> ProductoPedido.builder()
                                                .productoPedidoId(UUID.randomUUID())
                                                .pedidoId(pedidoGuardado.getPedidoId())
                                                .skuId(linea.skuId())
                                                .cantidadSolicitada(linea.cantidadSolicitada())
                                                .cantidadConfirmada(linea.cantidadSolicitada()) // Inicialmente igual a
                                                                                                // solicitada
                                                .build())
                                .toList();

                productoPedidoRepository.saveAll(lineas);

                // Descontar stock disponible al crear pedido
                descontarStockDisponible(command.lineas());

                log.info("Pedido creado exitosamente: {} - {} en estado ESPERANDO_RUTA",
                                pedidoGuardado.getPedidoId(), numeroPedido);

                // Spec 15 & 13: Publicar eventos SOLO después del commit exitoso para evitar
                // Race Conditions
                if (TransactionSynchronizationManager.isActualTransactionActive()) {
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                                @Override
                                public void afterCommit() {
                                        // Spec 15: Publicar evento para Módulo 3 Financiero
                                        pedidoCreadoProducer.publicarPedidoCreado(pedidoGuardado.getPedidoId(),
                                                        numeroPedido);

                                        // Spec 13: Solicitar ruta a Módulo 2 Logística
                                        solicitudRutaProducer
                                                        .enviarSolicitudRuta(pedidoGuardado.getPedidoId().toString());
                                }
                        });
                } else {
                        // Fallback si por alguna razón no hay transacción activa (no debería ocurrir
                        // aquí)
                        pedidoCreadoProducer.publicarPedidoCreado(pedidoGuardado.getPedidoId(), numeroPedido);
                        solicitudRutaProducer.enviarSolicitudRuta(pedidoGuardado.getPedidoId().toString());
                }

                return pedidoGuardado;
        }

        /**
         * Valida que todos los SKUs existan y retorna mapa de productos.
         * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
         */
        private Map<String, Producto> validarYObtenerProductos(List<LineaCommand> lineas) {
                Map<String, Producto> productos = lineas.stream()
                                .map(LineaCommand::skuId)
                                .distinct()
                                .filter(skuId -> productoRepository.findById(skuId).isPresent())
                                .collect(Collectors.toMap(
                                                skuId -> skuId,
                                                skuId -> productoRepository.findById(skuId).get()));

                // Verificar que todos los SKUs fueron encontrados
                List<String> skuIdsFaltantes = lineas.stream()
                                .map(LineaCommand::skuId)
                                .distinct()
                                .filter(skuId -> !productos.containsKey(skuId))
                                .toList();

                if (!skuIdsFaltantes.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Los siguientes SKUs no existen: " + skuIdsFaltantes);
                }

                return productos;
        }

        /**
         * Valida que hay stock suficiente para todas las líneas.
         * FR-055: Rechazar pedido si stock insuficiente.
         */
        private void validarStockDisponible(List<LineaCommand> lineas, Map<String, Producto> productos) {
                Map<String, StockInsuficienteException.StockDetalle> insuficientes = new java.util.HashMap<>();

                for (LineaCommand linea : lineas) {
                        // Consulta StockGlobalSku para validar disponibilidad
                        var stockOpt = stockGlobalSkuRepository
                                        .findById(java.util.Objects.requireNonNull(linea.skuId()));
                        int stockDisponible = stockOpt.map(s -> s.getDisponibles()).orElse(0);

                        if (stockDisponible < linea.cantidadSolicitada()) {
                                log.warn("STOCK INSUFICIENTE para SKU {}: Solicitado={}, Disponible en DB={}",
                                                linea.skuId(), linea.cantidadSolicitada(), stockDisponible);

                                Producto producto = productos.get(linea.skuId());
                                insuficientes.put(linea.skuId(),
                                                new StockInsuficienteException.StockDetalle(
                                                                linea.skuId(),
                                                                producto.getMarca(),
                                                                producto.getPresentacion(),
                                                                linea.cantidadSolicitada(),
                                                                stockDisponible));
                        }
                }

                if (!insuficientes.isEmpty()) {
                        log.warn("Stock insuficiente para crear pedido: {} SKUs con problema", insuficientes.size());
                        throw new StockInsuficienteException(insuficientes);
                }
        }

        /**
         * Descuenta el stock disponible al crear un pedido.
         * Cuando se confirma el compromiso, se mueven los comprometidos.
         * Cuando se despacha, se reduce el físico.
         */
        private void descontarStockDisponible(List<LineaCommand> lineas) {
                for (LineaCommand linea : lineas) {
                        var stockOpt = stockGlobalSkuRepository.findById(Objects.requireNonNull(linea.skuId()));
                        if (stockOpt.isPresent()) {
                                var stock = stockOpt.get();
                                stock.setDisponibles(stock.getDisponibles() - linea.cantidadSolicitada());
                                stockGlobalSkuRepository.save(stock);
                                log.info("Stock disponible descontado para {}: cantidad={}, disponibles={}",
                                                linea.skuId(), linea.cantidadSolicitada(), stock.getDisponibles());
                        }
                }
        }
}
