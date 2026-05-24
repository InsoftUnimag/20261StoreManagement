package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_picking", indexes = {
    @Index(name = "idx_picking_pedido", columnList = "pedido_id"),
    @Index(name = "idx_picking_fecha", columnList = "fecha_picking")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroPickingJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registro_picking_id", updatable = false, nullable = false)
    private Long registroPickingId;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "operario_id", nullable = false)
    private Long operarioId;

    @Column(name = "fecha_picking", nullable = false)
    private LocalDateTime fechaPicking;

    @Column(name = "observaciones")
    private String observaciones;
}
