package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "recepcion", indexes = {
    @Index(name = "idx_recepcion_manifiesto", columnList = "manifiesto_id"),
    @Index(name = "idx_recepcion_fecha", columnList = "fecha_recepcion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecepcionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recepcion_id") private Long recepcionId;
    @Column(name = "manifiesto_id", nullable = false) private Long manifiestoId;
    @Column(name = "operario_id", nullable = false) private Long operarioId;
    @Column(name = "fecha_recepcion", nullable = false) private LocalDateTime fechaRecepcion;
    @Column(name = "notas") private String notas;
    @Column(name = "numero_recepcion", unique = true)
    private String numeroRecepcion;
}

