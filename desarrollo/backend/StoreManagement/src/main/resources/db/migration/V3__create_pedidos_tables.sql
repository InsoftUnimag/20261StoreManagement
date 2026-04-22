-- V3__create_pedidos_tables.sql
-- Creates tables for order management (Pedido, ProductoPedido, LoteComprometido)

-- Pedidos table
CREATE TABLE pedidos (
    pedido_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_pedido VARCHAR(50) NOT NULL UNIQUE,
    cliente_cc VARCHAR(50) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'ESPERANDO_RUTA',
    ruta_id UUID,
    fecha_compromiso TIMESTAMP,
    asesor_id UUID
);

CREATE INDEX idx_pedidos_numero ON pedidos(numero_pedido);
CREATE INDEX idx_pedidos_cliente ON pedidos(cliente_cc);
CREATE INDEX idx_pedidos_estado ON pedidos(estado);
CREATE INDEX idx_pedidos_fecha ON pedidos(fecha_creacion);

-- Order lines table
CREATE TABLE productos_pedido (
    producto_pedido_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL REFERENCES pedidos(pedido_id) ON DELETE CASCADE,
    sku_id UUID NOT NULL,
    cantidad_solicitada INTEGER NOT NULL CHECK (cantidad_solicitada > 0),
    cantidad_confirmada INTEGER NOT NULL CHECK (cantidad_confirmada >= 0)
);

CREATE INDEX idx_productos_pedido_pedido ON productos_pedido(pedido_id);
CREATE INDEX idx_productos_pedido_sku ON productos_pedido(sku_id);

-- Committed lots table
CREATE TABLE lotes_comprometidos (
    compromiso_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    producto_pedido_id UUID NOT NULL REFERENCES productos_pedido(producto_pedido_id) ON DELETE CASCADE,
    codigo_lote VARCHAR(100) NOT NULL,
    cantidad_comprometida INTEGER NOT NULL CHECK (cantidad_comprometida > 0),
    fecha_compromiso TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_lotes_comprometidos_producto ON lotes_comprometidos(producto_pedido_id);
CREATE INDEX idx_lotes_comprometidos_lote ON lotes_comprometidos(codigo_lote);
