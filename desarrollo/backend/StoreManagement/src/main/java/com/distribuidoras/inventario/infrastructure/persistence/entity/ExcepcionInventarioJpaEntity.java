package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "excepcion_inventario", indexes = {
    @Index(name = "idx_excepcion_sku", columnList = "sku_id"),
    @Index(name = "idx_excepcion_fecha", columnList = "fecha_registro"),
    @Index(name = "idx_excepcion_lote", columnList = "codigo_lote")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcepcionInventarioJpaEntity {
    @Id @Column(name = "excepcion_id") private UUID excepcionId;
    @Column(name = "tipo_excepcion", nullable = false, length = 30) private String tipoExcepcion;
    @Column(name = "codigo_lote", length = 100) private String codigoLote;
    @Column(name = "sku_id", nullable = false) private String skuId;
    @Column(name = "cantidad_afectada", nullable = false) private Integer cantidadAfectada;
    @Column(name = "fecha_registro", nullable = false) private LocalDateTime fechaRegistro;
    @Column(name = "operario_id") private UUID operarioId;
    @Column(nullable = false) private String descripcion;
    @Column(name = "evidencia_url", length = 500) private String evidenciaUrl;
}
