package com.distribuidoras.inventario.domain.model;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Domain entity: ProductoPedido (Order line item).
 * Represents a product line in an order.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductoPedido {

    private Long productoPedidoId;

    @NotNull(message = "El ID del pedido no puede ser nulo")
    private Long pedidoId;

    @NotBlank(message = "El SKU no puede estar vacío")
    @Size(max = 20, message = "El SKU no puede exceder 20 caracteres")
    private String skuId;

    @NotNull(message = "La cantidad solicitada no puede ser nula")
    @Min(value = 1, message = "La cantidad solicitada debe ser mayor a 0")
    private Integer cantidadSolicitada;

    @NotNull(message = "La cantidad confirmada no puede ser nula")
    @Min(value = 0, message = "La cantidad confirmada no puede ser negativa")
    private Integer cantidadConfirmada;

    @Min(value = 0, message = "La cantidad despachada no puede ser negativa")
    private Integer cantidadDespachada;

    /**
     * Validates that confirmed quantity doesn't exceed requested quantity.
     */
    public void validarCantidadConfirmada() {
        if (this.cantidadConfirmada != null && this.cantidadSolicitada != null
                && this.cantidadConfirmada > this.cantidadSolicitada) {
            throw new IllegalArgumentException(
                    "Cantidad confirmada (%d) no puede ser mayor a la solicitada (%d)"
                            .formatted(this.cantidadConfirmada, this.cantidadSolicitada));
        }
        if (this.cantidadConfirmada != null && this.cantidadConfirmada < 0) {
            throw new IllegalArgumentException(
                    "Cantidad confirmada no puede ser negativa: %d".formatted(this.cantidadConfirmada));
        }
        if (this.cantidadDespachada != null && this.cantidadDespachada < 0) {
            throw new IllegalArgumentException(
                    "Cantidad despachada no puede ser negativa: %d".formatted(this.cantidadDespachada));
        }
        if (this.cantidadDespachada != null && this.cantidadConfirmada != null
                && this.cantidadDespachada > this.cantidadConfirmada) {
            throw new IllegalArgumentException(
                    "Cantidad despachada (%d) no puede ser mayor a la confirmada (%d)"
                            .formatted(this.cantidadDespachada, this.cantidadConfirmada));
        }
    }
}
