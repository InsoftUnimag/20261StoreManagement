package com.distribuidoras.inventario.domain.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

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

    private Long compromisoId;
    
    @NotNull(message = "El ID del producto pedido no puede ser nulo")
    private Long productoPedidoId;
    
    @NotBlank(message = "El código de lote no puede estar vacío")
    @Size(max = 100, message = "El código de lote no puede exceder 100 caracteres")
    private String codigoLote;
    
    @NotNull(message = "La cantidad comprometida no puede ser nula")
    @Min(value = 1, message = "La cantidad comprometida debe ser mayor a 0")
    private Integer cantidadComprometida;
    
    @NotNull(message = "La fecha de compromiso no puede ser nula")
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
