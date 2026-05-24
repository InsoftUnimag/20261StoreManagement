package com.distribuidoras.inventario.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lotes_comprometidos", indexes = {
    @Index(name = "idx_lote_comprometido_producto_pedido", columnList = "producto_pedido_id"),
    @Index(name = "idx_lote_comprometido_lote", columnList = "codigo_lote")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoteComprometidoJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compromiso_id")
    private Long compromisoId;
    
    @Column(name = "producto_pedido_id", nullable = false)
    private Long productoPedidoId;
    
    @Column(name = "codigo_lote", nullable = false, length = 100)
    private String codigoLote;
    
    @Column(name = "cantidad_comprometida", nullable = false)
    private Integer cantidadComprometida;
    
    @Column(name = "fecha_compromiso", nullable = false)
    private LocalDateTime fechaCompromiso;
}
