-- V8: Agregar columna creado_el a la tabla manifiesto para trazabilidad real
ALTER TABLE manifiesto ADD COLUMN creado_el TIMESTAMP NOT NULL DEFAULT NOW();
