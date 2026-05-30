package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "productos_pedido", indexes = {
        @Index(name = "idx_producto_pedido_pedido", columnList = "pedido_id"),
        @Index(name = "idx_producto_pedido_sku", columnList = "sku_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoPedidoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producto_pedido_id")
    private Long productoPedidoId;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "sku_id", nullable = false, length = 20)
    private String skuId;

    @Column(name = "cantidad_solicitada", nullable = false)
    private Integer cantidadSolicitada;

    @Column(name = "cantidad_confirmada", nullable = false)
    private Integer cantidadConfirmada;

    @Column(name = "precio_unitario", precision = 12, scale = 2)
    private BigDecimal precioUnitario;
}
