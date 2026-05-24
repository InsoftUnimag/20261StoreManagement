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
@Table(name = "registro_despacho", indexes = {
    @Index(name = "idx_despacho_pedido", columnList = "pedido_id"),
    @Index(name = "idx_despacho_fecha", columnList = "fecha_despacho"),
    @Index(name = "idx_despacho_transportista", columnList = "transportista")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroDespachoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registro_despacho_id", updatable = false, nullable = false)
    private Long registroDespachoId;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "operario_id", nullable = false)
    private Long operarioId;

    @Column(name = "fecha_despacho", nullable = false)
    private LocalDateTime fechaDespacho;

    @Column(name = "transportista", nullable = false)
    private String transportista;

    @Column(name = "placa_vehiculo", nullable = false)
    private String placaVehiculo;

    @Column(name = "observaciones")
    private String observaciones;
}
