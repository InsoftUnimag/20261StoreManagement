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
@Table(name = "registro_despacho")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroDespachoJpaEntity {
    @Id
    @Column(name = "registro_despacho_id", updatable = false, nullable = false)
    private UUID registroDespachoId;

    @Column(name = "pedido_id", nullable = false)
    private UUID pedidoId;

    @Column(name = "operario_id", nullable = false)
    private UUID operarioId;

    @Column(name = "fecha_despacho", nullable = false)
    private LocalDateTime fechaDespacho;

    @Column(name = "transportista", nullable = false)
    private String transportista;

    @Column(name = "placa_vehiculo", nullable = false)
    private String placaVehiculo;

    @Column(name = "observaciones")
    private String observaciones;
}
