package com.distribuidoras.inventario.domain.model;

import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity: Pedido (Order).
 * FR-056: Unique order number generation
 * FR-057: Created in ESPERANDO_RUTA state without committed lots
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pedido {

    @EqualsAndHashCode.Include
    private UUID pedidoId;
    
    private String numeroPedido;
    
    private String clienteCc;

    private String clienteNombre;
    
    private LocalDateTime fechaCreacion;
    
    private EstadoPedido estado;
    
    private UUID rutaId;
    
    private LocalDateTime fechaCompromiso;
    
    private UUID asesorId;

    /**
     * Validates that the order can transition to the next state.
     */
    public boolean puedeTransicionarA(EstadoPedido nuevoEstado) {
        return switch (this.estado) {
            case ESPERANDO_RUTA -> nuevoEstado == EstadoPedido.COMPROMETIDO;
            case COMPROMETIDO -> nuevoEstado == EstadoPedido.EN_PICKING;
            case EN_PICKING -> nuevoEstado == EstadoPedido.DESPACHADO;
            case DESPACHADO -> nuevoEstado == EstadoPedido.ENTREGADO;
            case ENTREGADO -> false;
        };
    }
}
