package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "recepcion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecepcionJpaEntity {
    @Id @Column(name = "recepcion_id") private UUID recepcionId;
    @Column(name = "manifiesto_id") private UUID manifiestoId;
    @Column(name = "operario_id") private UUID operarioId;
    @Column(name = "fecha_recepcion", nullable = false) private LocalDateTime fechaRecepcion;
    @Column(name = "notas") private String notas;
}
