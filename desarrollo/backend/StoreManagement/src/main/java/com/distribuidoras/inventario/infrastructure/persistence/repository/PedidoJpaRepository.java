package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.PedidoJpaEntity;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PedidoJpaRepository extends JpaRepository<PedidoJpaEntity, UUID> {
    
    Optional<PedidoJpaEntity> findByNumeroPedido(String numeroPedido);
    
    /**
     * Find max order number sequence for a given date prefix.
     */
    @Query("SELECT MAX(CAST(SUBSTRING(p.numeroPedido, LENGTH(p.numeroPedido) - 2) AS int)) FROM PedidoJpaEntity p WHERE p.numeroPedido LIKE :prefix%")
    Integer findMaxNumeroPedidoByPrefix(@Param("prefix") String prefix);
    
    /**
     * Find orders by status ordered by creation ASC (FIFO).
     */
    List<PedidoJpaEntity> findByEstadoOrderByFechaCreacionAsc(EstadoPedido estado);
    
    /**
     * Find orders with dynamic filters.
     */
    @Query("SELECT p FROM PedidoJpaEntity p WHERE " +
           "(:estado IS NULL OR p.estado = :estado) AND " +
           "(:clienteCc IS NULL OR p.clienteCc LIKE %:clienteCc%) AND " +
           "(:numeroPedido IS NULL OR p.numeroPedido LIKE %:numeroPedido%) AND " +
           "(:fechaDesde IS NULL OR p.fechaCreacion >= :fechaDesde) AND " +
           "(:fechaHasta IS NULL OR p.fechaCreacion <= :fechaHasta)")
    Page<PedidoJpaEntity> findByFilters(
            @Param("estado") EstadoPedido estado,
            @Param("clienteCc") String clienteCc,
            @Param("numeroPedido") String numeroPedido,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            Pageable pageable
    );
}
