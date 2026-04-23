package com.distribuidoras.inventario.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity for a Dispatch Record.
 */
public class RegistroDespacho {
    private final UUID registroDespachoId;
    private final UUID pedidoId;
    private final UUID operarioId;
    private final LocalDateTime fechaDespacho;
    private final String transportista;
    private final String placaVehiculo;
    private final String observaciones;

    private RegistroDespacho(Builder builder) {
        this.registroDespachoId = builder.registroDespachoId;
        this.pedidoId = builder.pedidoId;
        this.operarioId = builder.operarioId;
        this.fechaDespacho = builder.fechaDespacho;
        this.transportista = builder.transportista;
        this.placaVehiculo = builder.placaVehiculo;
        this.observaciones = builder.observaciones;
        validar();
    }

    private void validar() {
        if (registroDespachoId == null) throw new IllegalArgumentException("registroDespachoId no puede ser nulo");
        if (pedidoId == null) throw new IllegalArgumentException("pedidoId no puede ser nulo");
        if (operarioId == null) throw new IllegalArgumentException("operarioId no puede ser nulo");
        if (fechaDespacho == null) throw new IllegalArgumentException("fechaDespacho no puede ser nula");
        if (fechaDespacho.isAfter(LocalDateTime.now())) throw new IllegalArgumentException("fechaDespacho no puede ser futura");
        if (transportista == null || transportista.isBlank()) throw new IllegalArgumentException("transportista no puede estar vacío");
    }

    public UUID getRegistroDespachoId() { return registroDespachoId; }
    public UUID getPedidoId() { return pedidoId; }
    public UUID getOperarioId() { return operarioId; }
    public LocalDateTime getFechaDespacho() { return fechaDespacho; }
    public String getTransportista() { return transportista; }
    public String getPlacaVehiculo() { return placaVehiculo; }
    public String getObservaciones() { return observaciones; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID registroDespachoId;
        private UUID pedidoId;
        private UUID operarioId;
        private LocalDateTime fechaDespacho;
        private String transportista;
        private String placaVehiculo;
        private String observaciones;

        public Builder registroDespachoId(UUID registroDespachoId) {
            this.registroDespachoId = registroDespachoId;
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
        public Builder fechaDespacho(LocalDateTime fechaDespacho) {
            this.fechaDespacho = fechaDespacho;
            return this;
        }
        public Builder transportista(String transportista) {
            this.transportista = transportista;
            return this;
        }
        public Builder placaVehiculo(String placaVehiculo) {
            this.placaVehiculo = placaVehiculo;
            return this;
        }
        public Builder observaciones(String observaciones) {
            this.observaciones = observaciones;
            return this;
        }

        public RegistroDespacho build() {
            return new RegistroDespacho(this);
        }
    }
}
