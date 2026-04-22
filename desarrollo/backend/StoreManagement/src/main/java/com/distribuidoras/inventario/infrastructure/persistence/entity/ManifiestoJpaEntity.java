package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name = "manifiesto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ManifiestoJpaEntity {
    @Id @Column(name = "manifiesto_id") private UUID manifiestoId;
    @Column(name = "numero_manifiesto", unique = true, nullable = false) private String numeroManifiesto;
    @Column(name = "fecha_emision", nullable = false) private LocalDate fechaEmision;
    @Column(nullable = false) private String proveedor;
    @Column(nullable = false, length = 30) private String estado;
}
