-- V7: Agregar campos fechaEntrega y observaciones a Pedido
-- Requiere V1-V6 aplicados.

-- Agregar columna fechaEntrega a pedidos
ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS fecha_entrega TIMESTAMP;
ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS observaciones VARCHAR(500);

-- Crear índices para los nuevos campos
CREATE INDEX IF NOT EXISTS idx_pedidos_fecha_entrega ON pedidos(fecha_entrega);

-- Confirmar que las columnas fueron agregadas correctamente
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'pedidos' AND column_name = 'fecha_entrega'
    ) THEN
        RAISE NOTICE 'Columna fecha_entrega agregada correctamente';
    END IF;
END $$;