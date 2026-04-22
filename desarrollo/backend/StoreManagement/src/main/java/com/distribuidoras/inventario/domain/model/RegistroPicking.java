package com.distribuidoras.inventario.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity for a Picking Record.
 */
public class RegistroPicking {
    private final UUID registroPickingId;
    private final UUID pedidoId;
    private final UUID operarioId;
    private final LocalDateTime fechaPicking;
    private final String observaciones;

    private RegistroPicking(Builder builder) {
        this.registroPickingId = builder.registroPickingId;
        this.pedidoId = builder.pedidoId;
        this.operarioId = builder.operarioId;
        this.fechaPicking = builder.fechaPicking;
        this.observaciones = builder.observaciones;
        validar();
    }

    private void validar() {
        if (registroPickingId == null) {
            throw new IllegalArgumentException("registroPickingId no puede ser nulo");
        }
        if (pedidoId == null) {
            throw new IllegalArgumentException("pedidoId no puede ser nulo");
        }
        if (operarioId == null) {
            throw new IllegalArgumentException("operarioId no puede ser nulo");
        }
        if (fechaPicking == null) {
            throw new IllegalArgumentException("fechaPicking no puede ser nulo");
        }
        if (fechaPicking.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("fechaPicking no puede ser futura");
        }
    }

    // Getters
    public UUID getRegistroPickingId() { return registroPickingId; }
    public UUID getPedidoId() { return pedidoId; }
    public UUID getOperarioId() { return operarioId; }
    public LocalDateTime getFechaPicking() { return fechaPicking; }
    public String getObservaciones() { return observaciones; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID registroPickingId;
        private UUID pedidoId;
        private UUID operarioId;
        private LocalDateTime fechaPicking;
        private String observaciones;

        public Builder registroPickingId(UUID registroPickingId) {
            this.registroPickingId = registroPickingId;
            return this;
        }

        public Builder pedidoId(UUID pedidoId) {
            this.pedidoId = pedidoId;
            return this;
        }

        public Builder operarioId(UUID operarioId) {
            this.operarioId = operarioId;
            return this;
        }

        public Builder fechaPicking(LocalDateTime fechaPicking) {
            this.fechaPicking = fechaPicking;
            return this;
        }

        public Builder observaciones(String observaciones) {
            this.observaciones = observaciones;
            return this;
        }

        public RegistroPicking build() {
            return new RegistroPicking(this);
        }
    }
}
