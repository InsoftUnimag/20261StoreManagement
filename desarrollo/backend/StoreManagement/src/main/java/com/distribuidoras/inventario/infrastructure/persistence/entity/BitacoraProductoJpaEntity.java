package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla 'bitacora_producto'.
 * Formato skuIdRef: SKU-001, SKU-012, SKU-111, etc.
 */
@Entity
@Table(name = "bitacora_producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BitacoraProductoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sku_id_ref", nullable = false, length = 20)
    private String skuIdRef;

    @Column(name = "campo", nullable = false, length = 50)
    private String campo;

    @Column(name = "valor_anterior", length = 255)
    private String valorAnterior;

    @Column(name = "valor_nuevo", length = 255)
    private String valorNuevo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "usuario", length = 100)
    private String usuario;
}
