package com.distribuidoras.inventario.domain.model;

import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import jakarta.validation.constraints.*;
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
    
    @NotBlank(message = "El número de pedido no puede estar vacío")
    @Size(max = 50, message = "El número de pedido no puede exceder 50 caracteres")
    private String numeroPedido;
    
    @NotBlank(message = "La cédula del cliente no puede estar vacía")
    @Size(max = 50, message = "La cédula no puede exceder 50 caracteres")
    private String clienteCc;

    @Size(max = 200, message = "El nombre del cliente no puede exceder 200 caracteres")
    private String clienteNombre;
    
    @NotNull(message = "La fecha de creación no puede ser nula")
    private LocalDateTime fechaCreacion;
    
    @NotNull(message = "El estado del pedido no puede ser nulo")
    private EstadoPedido estado;
    
    private UUID rutaId;
    
    private LocalDateTime fechaCompromiso;
    
    private UUID asesorId;
    
    private UUID operarioPickingId;
    
    private UUID operarioDespachoId;

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
