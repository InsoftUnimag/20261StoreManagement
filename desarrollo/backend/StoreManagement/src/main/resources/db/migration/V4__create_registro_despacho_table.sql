-- V4: Crear tabla registro_despacho
CREATE TABLE registro_despacho (
    registro_despacho_id UUID            PRIMARY KEY,
    pedido_id       UUID            NOT NULL,
    operario_id     UUID            NOT NULL,
    transportista   VARCHAR(100)    NOT NULL,
    placa_vehiculo  VARCHAR(50)     NOT NULL,
    fecha_despacho  TIMESTAMP       NOT NULL DEFAULT NOW(),
    observaciones   TEXT
);
