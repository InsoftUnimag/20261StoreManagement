-- V6: Add costo_total to pedidos and precio_unitario to productos_pedido
ALTER TABLE pedidos ADD COLUMN costo_total DECIMAL(12,2);
ALTER TABLE productos_pedido ADD COLUMN precio_unitario DECIMAL(12,2);
