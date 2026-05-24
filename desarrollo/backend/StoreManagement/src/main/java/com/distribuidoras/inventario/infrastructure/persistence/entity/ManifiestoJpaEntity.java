package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "manifiesto", indexes = {
    @Index(name = "idx_manifiesto_numero", columnList = "numero_manifiesto"),
    @Index(name = "idx_manifiesto_estado", columnList = "estado"),
    @Index(name = "idx_manifiesto_fecha", columnList = "fecha_emision")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_manifiesto_numero", columnNames = "numero_manifiesto")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManifiestoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manifiesto_id") private Long manifiestoId;
    @Column(name = "numero_manifiesto", unique = true, nullable = false) private String numeroManifiesto;
    @Column(name = "fecha_emision", nullable = false) private LocalDate fechaEmision;
    @Column(nullable = false) private String proveedor;
    @Column(nullable = false, length = 30) private String estado;
    @Builder.Default
    @Column(name = "creado_el", nullable = false) private LocalDateTime creadoEl = LocalDateTime.now();
}
