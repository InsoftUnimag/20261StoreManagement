-- V5: Crear tabla registro_picking
CREATE TABLE registro_picking (
    registro_picking_id BIGSERIAL        PRIMARY KEY,
    pedido_id       BIGINT          NOT NULL,
    operario_id     BIGINT          NOT NULL,
    fecha_picking   TIMESTAMP       NOT NULL DEFAULT NOW(),
    observaciones   TEXT
);