package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "movimiento_inventario", indexes = {
    @Index(name = "idx_movimiento_lote", columnList = "codigo_lote"),
    @Index(name = "idx_movimiento_fecha", columnList = "fecha_movimiento"),
    @Index(name = "idx_movimiento_pedido", columnList = "pedido_id"),
    @Index(name = "idx_movimiento_tipo", columnList = "tipo_movimiento")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventarioJpaEntity {
    @Id @Column(name = "movimiento_id") private UUID movimientoId;
    @Column(name = "codigo_lote", nullable = false, length = 100) private String codigoLote;
    @Column(name = "tipo_movimiento", nullable = false, length = 30) private String tipoMovimiento;
    @Column(name = "cantidad", nullable = false) private Integer cantidad;
    @Column(name = "fecha_movimiento", nullable = false) private LocalDateTime fechaMovimiento;
    @Column(name = "pedido_id") private UUID pedidoId;
    @Column(name = "excepcion_id") private UUID excepcionId;
    @Column(name = "operario_id") private UUID operarioId;
    @Column(name = "observaciones") private String observaciones;
}
