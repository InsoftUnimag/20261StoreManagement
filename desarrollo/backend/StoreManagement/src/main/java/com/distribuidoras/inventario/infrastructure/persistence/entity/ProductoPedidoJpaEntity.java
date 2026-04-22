package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "productos_pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoPedidoJpaEntity {
    
    @Id
    @Column(name = "producto_pedido_id")
    private UUID productoPedidoId;
    
    @Column(name = "pedido_id", nullable = false)
    private UUID pedidoId;
    
    @Column(name = "sku_id", nullable = false, length = 20)
    private String skuId;
    
    @Column(name = "cantidad_solicitada", nullable = false)
    private Integer cantidadSolicitada;
    
    @Column(name = "cantidad_confirmada", nullable = false)
    private Integer cantidadConfirmada;
}
