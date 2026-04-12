package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "lote")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoteJpaEntity {
    @Id @Column(name = "codigo_lote", length = 100) private String codigoLote;
    @Column(name = "sku_id", nullable = false) private UUID skuId;
    @Column(name = "cantidad", nullable = false) private Integer cantidad;
    @Column(name = "fecha_vencimiento", nullable = false) private LocalDate fechaVencimiento;
    @Column(name = "fecha_expedicion") private LocalDate fechaExpedicion;
    @Column(name = "disponible", nullable = false) private Boolean disponible;
    @Column(name = "flag_urgencia_fefo", nullable = false) private Boolean flagUrgenciaFefo;
    @Column(name = "costo_unitario_producto") private java.math.BigDecimal costoUnitarioProducto;
    @Column(name = "recepcion_id") private UUID recepcionId;
    @Column(name = "creado_el", nullable = false) private LocalDateTime creadoEl;
}
