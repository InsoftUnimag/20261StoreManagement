package com.distribuidoras.inventario.domain.repository;

import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for Pedido persistence.
 */
public interface PedidoRepository {
    
    Pedido save(Pedido pedido);
    
    Optional<Pedido> findById(UUID pedidoId);
    
    Optional<Pedido> findByNumeroPedido(String numeroPedido);
    
    /**
     * Generate sequential order number for a given date.
     * Format: PED-YYYYMMDD-NNN
     */
    String generarNumeroPedido(LocalDate fecha);
    
    /**
     * Find orders with dynamic filters.
     */
    Page<Pedido> findByFilters(
            EstadoPedido estado,
            String clienteCc,
            String numeroPedido,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Pageable pageable
    );
    
    void update(Pedido pedido);
    
    /**
     * Find orders by status ordered by creation date ASC (FIFO).
     */
    List<Pedido> findByEstadoOrderByFechaCreacionAsc(EstadoPedido estado);

    /**
     * Find orders by status ordered by commitment date ASC (FIFO) for picking.
     */
    List<Pedido> findByEstadoOrderByFechaCompromisoAsc(EstadoPedido estado);

    /**
     * Find orders with picking status ordered by picking date ASC (FIFO).
     */
    List<Pedido> findByEstadoWithPickingOrderByFechaPickingAsc(EstadoPedido estado);
}
