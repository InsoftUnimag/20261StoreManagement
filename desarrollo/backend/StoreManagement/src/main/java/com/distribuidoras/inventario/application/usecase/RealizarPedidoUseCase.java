package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.StockInsuficienteException;
import com.distribuidoras.inventario.domain.model.Cliente;
import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.model.ProductoPedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.infrastructure.messaging.PedidoCreadoProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final LoteRepository loteRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;
    private final PedidoCreadoProducer pedidoCreadoProducer;

    public RealizarPedidoUseCase(ConsultarClienteUseCase consultarClienteUseCase,
                                  ProductoRepository productoRepository,
                                  LoteRepository loteRepository,
                                  PedidoRepository pedidoRepository,
                                  ProductoPedidoRepository productoPedidoRepository,
                                  PedidoCreadoProducer pedidoCreadoProducer) {
        this.consultarClienteUseCase = consultarClienteUseCase;
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
        this.pedidoCreadoProducer = pedidoCreadoProducer;
    }

    /**
     * Command object para crearar pedido.
     */
    public record PedidoCommand(
            String clienteCc,
            UUID asesorId,
            List<LineaCommand> lineas
    ) {}

    public record LineaCommand(
            UUID skuId,
            Integer cantidadSolicitada
    ) {}

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
        Cliente cliente = consultarClienteUseCase.ejecutar(command.clienteCc());

        // 2. Validar SKUs existen (FR-053)
        Map<UUID, Producto> productos = validarYObtenerProductos(command.lineas());

        // 3. Validar stock disponible SIN comprometer (FR-054, FR-055)
        validarStockDisponible(command.lineas(), productos);

        // 4. Generar número único de pedido (FR-056)
        String numeroPedido = pedidoRepository.generarNumeroPedido(java.time.LocalDate.now());

        // 5. Crear Pedido en estado ESPERANDO_RUTA (FR-057)
        Pedido pedido = Pedido.builder()
                .pedidoId(UUID.randomUUID())
                .numeroPedido(numeroPedido)
                .clienteCc(command.clienteCc())
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
                        .cantidadConfirmada(linea.cantidadSolicitada()) // Inicialmente igual a solicitada
                        .build())
                .toList();

        productoPedidoRepository.saveAll(lineas);

        log.info("Pedido creado exitosamente: {} - {} en estado ESPERANDO_RUTA", 
                pedidoGuardado.getPedidoId(), numeroPedido);

        // Spec 15: Publicar evento para Módulo 3 Financiero
        pedidoCreadoProducer.publicarPedidoCreado(pedidoGuardado.getPedidoId(), numeroPedido);
        
        // Spec 13: Solicitar ruta a Módulo 2 Logística
        pedidoCreadoProducer.solicitarRuta(pedidoGuardado.getPedidoId(), numeroPedido);

        return pedidoGuardado;
    }

    /**
     * Valida que todos los SKUs existan y retorna mapa de productos.
     */
    private Map<UUID, Producto> validarYObtenerProductos(List<LineaCommand> lineas) {
        Map<UUID, Producto> productos = lineas.stream()
                .map(LineaCommand::skuId)
                .distinct()
                .filter(skuId -> productoRepository.findById(skuId).isPresent())
                .collect(Collectors.toMap(
                        skuId -> skuId,
                        skuId -> productoRepository.findById(skuId).get()
                ));

        // Verificar que todos los SKUs fueron encontrados
        List<UUID> skuIdsFaltantes = lineas.stream()
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
    private void validarStockDisponible(List<LineaCommand> lineas, Map<UUID, Producto> productos) {
        Map<UUID, StockInsuficienteException.StockDetalle> insuficientes = new java.util.HashMap<>();
        
        for (LineaCommand linea : lineas) {
            List<Lote> lotesConStock = loteRepository.findBySkuIdWithStock(linea.skuId());
            int stockDisponible = lotesConStock.stream()
                    .mapToInt(Lote::getCantidad)
                    .sum();

            if (stockDisponible < linea.cantidadSolicitada()) {
                Producto producto = productos.get(linea.skuId());
                insuficientes.put(linea.skuId(),
                        new StockInsuficienteException.StockDetalle(
                                linea.skuId(),
                                producto.getMarca(),
                                producto.getPresentacion(),
                                linea.cantidadSolicitada(),
                                stockDisponible
                        ));
            }
        }

        if (!insuficientes.isEmpty()) {
            log.warn("Stock insuficiente para crear pedido: {} SKUs con problema", insuficientes.size());
            throw new StockInsuficienteException(insuficientes);
        }
    }
}
