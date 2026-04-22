package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(name = "detalle_manifiesto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DetalleManifiestoJpaEntity {
    @Id @Column(name = "detalle_id") private UUID detalleId;
    @Column(name = "manifiesto_id", nullable = false) private UUID manifiestoId;
    @Column(name = "sku_id", nullable = false, length = 20) private String skuId;
    @Column(name = "cantidad_esperada", nullable = false) private Integer cantidadEsperada;
    @Column(name = "cantidad_recibida", nullable = false) private Integer cantidadRecibida;
}
