package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "registro_picking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroPickingJpaEntity {
    @Id
    @Column(name = "registro_picking_id", updatable = false, nullable = false)
    private UUID registroPickingId;

    @Column(name = "pedido_id", nullable = false)
    private UUID pedidoId;

    @Column(name = "operario_id", nullable = false)
    private UUID operarioId;

    @Column(name = "fecha_picking", nullable = false)
    private LocalDateTime fechaPicking;

    @Column(name = "observaciones")
    private String observaciones;
}
