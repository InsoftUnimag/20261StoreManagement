package com.distribuidoras.inventario.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity: LoteComprometido (Committed Lot).
 * Represents lots reserved for a specific order using FEFO.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LoteComprometido {

    private UUID compromisoId;
    
    private UUID productoPedidoId;
    
    private String codigoLote;
    
    private Integer cantidadComprometida;
    
    private LocalDateTime fechaCompromiso;

    /**
     * Validates that committed quantity is positive.
     */
    public void validarCantidad() {
        if (this.cantidadComprometida == null || this.cantidadComprometida <= 0) {
            throw new IllegalArgumentException(
                    "Cantidad comprometida debe ser mayor a 0: %d".formatted(this.cantidadComprometida));
        }
    }
}
