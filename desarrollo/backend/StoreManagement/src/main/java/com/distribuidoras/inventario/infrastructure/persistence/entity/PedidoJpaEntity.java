package com.distribuidoras.inventario.infrastructure.persistence.entity;

import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pedidos", indexes = {
    @Index(name = "idx_pedido_numero", columnList = "numero_pedido"),
    @Index(name = "idx_pedido_cliente", columnList = "cliente_cc"),
    @Index(name = "idx_pedido_estado", columnList = "estado"),
    @Index(name = "idx_pedido_fecha_creacion", columnList = "fecha_creacion")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_pedido_numero", columnNames = "numero_pedido")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PedidoJpaEntity {
    
    @Id
    @Column(name = "pedido_id")
    private UUID pedidoId;
    
    @Column(name = "numero_pedido", nullable = false, unique = true, length = 50)
    private String numeroPedido;
    
    @Column(name = "cliente_cc", nullable = false, length = 50)
    private String clienteCc;

    @Column(name = "cliente_nombre", length = 200)
    private String clienteNombre;
    
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
    
    @Column(name = "operario_picking_id")
    private UUID operarioPickingId;
    
    @Column(name = "operario_despacho_id")
    private UUID operarioDespachoId;

    @Column(name = "direccion_entrega", length = 255)
    private String direccionEntrega;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "observaciones", length = 500)
    private String observaciones;
}
