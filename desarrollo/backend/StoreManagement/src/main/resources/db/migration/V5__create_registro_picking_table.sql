-- V5: Crear tabla registro_picking
CREATE TABLE registro_picking (
    registro_picking_id UUID            PRIMARY KEY,
    pedido_id       UUID            NOT NULL,
    operario_id     UUID            NOT NULL,
    fecha_picking   TIMESTAMP       NOT NULL DEFAULT NOW(),
    observaciones   TEXT
);