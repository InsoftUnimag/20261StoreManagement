-- V4: Crear tabla registro_despacho
CREATE TABLE registro_despacho (
    registro_despacho_id BIGSERIAL        PRIMARY KEY,
    pedido_id       BIGINT          NOT NULL,
    operario_id     BIGINT          NOT NULL,
    transportista   VARCHAR(100)    NOT NULL,
    placa_vehiculo  VARCHAR(50)     NOT NULL,
    fecha_despacho  TIMESTAMP       NOT NULL DEFAULT NOW(),
    observaciones   TEXT
);
