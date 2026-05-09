package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.ProductoPedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoPedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarPedidosDespachoUseCase {

    private final PedidoRepository pedidoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;

    public ListarPedidosDespachoUseCase(PedidoRepository pedidoRepository,
            ProductoPedidoRepository productoPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<PedidoDespachoDTO> ejecutar() {
        List<Pedido> pedidos = pedidoRepository.findByEstadoOrderByFechaCreacionAsc(EstadoPedido.PICKUP);

        return pedidos.stream().map(pedido -> {
            List<ProductoPedido> lineas = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());
            int totalUnidades = lineas.stream()
                    .mapToInt(ProductoPedido::getCantidadConfirmada)
                    .sum();

            return new PedidoDespachoDTO(
                    pedido.getPedidoId().toString(),
                    pedido.getNumeroPedido(),
                    pedido.getClienteCc(),
                    pedido.getClienteNombre(),
                    totalUnidades,
                    lineas.size());
        }).toList();
    }

    public record PedidoDespachoDTO(
            String pedidoId,
            String numeroPedido,
            String clienteCc,
            String clienteNombre,
            int totalUnidades,
            int cantidadLineas) {
    }
}