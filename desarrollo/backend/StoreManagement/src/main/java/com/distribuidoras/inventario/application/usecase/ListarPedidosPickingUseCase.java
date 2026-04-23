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
public class ListarPedidosPickingUseCase {

    private final PedidoRepository pedidoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;

    public ListarPedidosPickingUseCase(PedidoRepository pedidoRepository,
            ProductoPedidoRepository productoPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoPedidoRepository = productoPedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<PedidoPickingDTO> ejecutar() {
        List<Pedido> pedidos = pedidoRepository.findByEstadoOrderByFechaCreacionAsc(EstadoPedido.COMPROMETIDO);

        return pedidos.stream().map(pedido -> {
            List<ProductoPedido> lineas = productoPedidoRepository.findByPedidoId(pedido.getPedidoId());
            int totalUnidades = lineas.stream()
                    .mapToInt(ProductoPedido::getCantidadConfirmada)
                    .sum();

            return new PedidoPickingDTO(
                    pedido.getPedidoId().toString(),
                    pedido.getNumeroPedido(),
                    pedido.getClienteCc(),
                    pedido.getClienteNombre(),
                    totalUnidades,
                    lineas.size());
        }).toList();
    }

    public record PedidoPickingDTO(
            String pedidoId,
            String numeroPedido,
            String clienteCc,
            String clienteNombre,
            int totalUnidades,
            int cantidadLineas) {
    }
}