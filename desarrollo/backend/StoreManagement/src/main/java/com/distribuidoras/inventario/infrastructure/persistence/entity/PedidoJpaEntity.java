package com.distribuidoras.inventario.infrastructure.persistence.entity;

import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoJpaEntity {
    
    @Id
    @Column(name = "pedido_id")
    private UUID pedidoId;
    
    @Column(name = "numero_pedido", nullable = false, unique = true, length = 50)
    private String numeroPedido;
    
    @Column(name = "cliente_cc", nullable = false, length = 50)
    private String clienteCc;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPedido estado;
    
    @Column(name = "ruta_id")
    private UUID rutaId;
    
    @Column(name = "fecha_compromiso")
    private LocalDateTime fechaCompromiso;
    
    @Column(name = "asesor_id")
    private UUID asesorId;
}
