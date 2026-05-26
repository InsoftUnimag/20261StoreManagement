-- V6: Datos de prueba SOLO CERVEZAS - Six-pack, Caja, Barril, Estiba
-- Requiere V1-V5 aplicados. Usa ON CONFLICT para ser idempotente.

-- ============================================================
-- 1. PRODUCTOS (34 SKUs solo cervezas)
-- Presentaciones: Six-pack, Caja, Barril, Estiba
-- ============================================================
INSERT INTO producto (sku_id, marca, presentacion, contenido_ml, peso_logistico_kg, creado_el, activo) VALUES
-- Six-pack (6 unidades)
('SKU-001','Poker','Six-pack',330,2.20,NOW(),true),
('SKU-002','Aguila','Six-pack',330,2.20,NOW(),true),
('SKU-003','Club Colombia','Six-pack',330,2.30,NOW(),true),
('SKU-004','Pilsen','Six-pack',330,2.15,NOW(),true),
('SKU-005','Costena','Six-pack',330,2.15,NOW(),true),
('SKU-006','Corona','Six-pack',355,2.40,NOW(),true),
('SKU-007','Heineken','Six-pack',330,2.25,NOW(),true),
('SKU-008','Budweiser','Six-pack',330,2.20,NOW(),true),
-- Caja (12 unidades)
('SKU-009','Poker','Caja',330,4.50,NOW(),true),
('SKU-010','Aguila','Caja',330,4.50,NOW(),true),
('SKU-011','Club Colombia','Caja',330,4.60,NOW(),true),
('SKU-012','Pilsen','Caja',330,4.40,NOW(),true),
('SKU-013','Costena','Caja',330,4.40,NOW(),true),
('SKU-014','Corona','Caja',355,4.80,NOW(),true),
('SKU-015','Heineken','Caja',330,4.55,NOW(),true),
('SKU-016','Budweiser','Caja',330,4.50,NOW(),true),
('SKU-017','Stella Artois','Caja',330,4.65,NOW(),true),
('SKU-018','Andina','Caja',330,4.45,NOW(),true),
('SKU-019','Miller','Caja',330,4.50,NOW(),true),
('SKU-020','Redds','Caja',269,3.80,NOW(),true),
('SKU-021','Strongbow','Caja',330,4.55,NOW(),true),
('SKU-022','Buque','Caja',340,4.60,NOW(),true),
('SKU-023','Tecate','Caja',330,4.50,NOW(),true),
('SKU-024','Modelo Especial','Caja',355,4.80,NOW(),true),
-- Barril
('SKU-025','Poker','Barril',20000,28.00,NOW(),true),
('SKU-026','Aguila','Barril',20000,28.00,NOW(),true),
('SKU-027','Club Colombia','Barril',10000,15.00,NOW(),true),
('SKU-028','Heineken','Barril',20000,28.50,NOW(),true),
('SKU-029','Stella Artois','Barril',10000,15.50,NOW(),true),
('SKU-030','Corona','Barril',20000,28.20,NOW(),true),
-- Estiba (pallet)
('SKU-031','Poker','Estiba',330,480.00,NOW(),true),
('SKU-032','Aguila','Estiba',330,480.00,NOW(),true),
('SKU-033','Club Colombia','Estiba',330,490.00,NOW(),true),
('SKU-034','Heineken','Estiba',330,485.00,NOW(),true)
ON CONFLICT (sku_id) DO NOTHING;

-- ============================================================
-- 2. MANIFIESTOS Y DETALLES (cervecerias / distribuidores)
-- ============================================================
INSERT INTO manifiesto (manifiesto_id, numero_manifiesto, fecha_emision, proveedor, estado) VALUES
(1,'MAN-HISTORICO','2026-01-01','STOCK INICIAL','RECEPCIONADO_TOTAL'),
(2,'MAN-2026-001','2026-04-20','Bavaria S.A.','RECEPCIONADO_TOTAL'),
(3,'MAN-2026-002','2026-04-22','Heineken Colombia','RECEPCIONADO_PARCIAL'),
(4,'MAN-2026-003','2026-04-25','InBev Colombia','PENDIENTE'),
(5,'MAN-2026-004','2026-04-28','Cerveceria del Valle','PENDIENTE'),
(6,'MAN-2026-005','2026-04-30','Distribuidora Cervezas Importadas','RECEPCIONADO_TOTAL'),
(7,'MAN-2026-006','2026-05-01','Cerveceria Artesanal Colombiana','PENDIENTE'),
(8,'MAN-2026-007','2026-05-05','Cerveceria del Valle','PENDIENTE')
ON CONFLICT (manifiesto_id) DO NOTHING;

INSERT INTO detalle_manifiesto (detalle_id, manifiesto_id, sku_id, cantidad_esperada, cantidad_recibida) VALUES
(1,2,'SKU-009',200,200),
(2,2,'SKU-010',150,150),
(3,2,'SKU-011',120,100),
(4,2,'SKU-025',30,30),
(5,3,'SKU-015',180,120),
(6,3,'SKU-028',20,15),
(7,4,'SKU-016',150,0),
(8,4,'SKU-017',100,0),
(9,5,'SKU-020',200,0),
(10,5,'SKU-021',120,0),
(11,6,'SKU-006',100,100),
(12,6,'SKU-014',80,80),
(13,6,'SKU-024',60,60),
(14,6,'SKU-023',90,90)
ON CONFLICT (detalle_id) DO NOTHING;

-- 2.1 RECEPCIONES
INSERT INTO recepcion (recepcion_id, numero_recepcion, manifiesto_id, operario_id, fecha_recepcion, notas) VALUES
(1, 'REC-HISTORICO', 1, 5, '2026-01-01 08:00:00', 'Recepcion de stock inicial para arranque de sistema'),
(2, 'REC-20260422-0001', 2, 5, '2026-04-22 10:30:00', 'Recepcion total Bavaria S.A.'),
(3, 'REC-20260424-0001', 3, 5, '2026-04-24 09:15:00', 'Recepcion parcial Heineken'),
(4, 'REC-20260430-0001', 6, 5, '2026-04-30 14:00:00', 'Recepcion total cervezas importadas')
ON CONFLICT (recepcion_id) DO NOTHING;

-- ============================================================
-- 3. LOTES (stock diverso y fechas FEFO)
-- ============================================================
INSERT INTO lote (codigo_lote, sku_id, cantidad, fecha_vencimiento, fecha_expedicion, disponible, flag_urgencia_fefo, costo_unitario_producto, recepcion_id, creado_el) VALUES
-- Poker Six-pack y Caja
('LOT-001-A','SKU-001',120,CURRENT_DATE+90,'2026-01-15',true,false,8500.00,1,NOW()),
('LOT-001-B','SKU-001',80,CURRENT_DATE+240,'2026-03-01',true,false,8200.00,1,NOW()),
('LOT-009-A','SKU-009',200,CURRENT_DATE+180,'2026-04-22',true,false,16500.00,2,NOW()),
('LOT-009-B','SKU-009',150,CURRENT_DATE+300,'2026-06-01',true,false,16000.00,1,NOW()),
-- Aguila Six-pack y Caja
('LOT-002-A','SKU-002',130,CURRENT_DATE+75,'2026-01-20',true,false,8600.00,1,NOW()),
('LOT-010-A','SKU-010',150,CURRENT_DATE+195,'2026-04-22',true,false,16800.00,2,NOW()),
-- Club Colombia Six-pack, Caja, Barril
('LOT-003-A','SKU-003',90,CURRENT_DATE+120,'2026-02-10',true,false,9800.00,1,NOW()),
('LOT-011-A','SKU-011',100,CURRENT_DATE+210,'2026-04-22',true,false,19000.00,2,NOW()),
('LOT-027-A','SKU-027',15,CURRENT_DATE+365,'2026-04-22',true,false,95000.00,2,NOW()),
-- Pilsen Six-pack y Caja
('LOT-004-A','SKU-004',140,CURRENT_DATE+60,'2026-01-25',true,false,7800.00,1,NOW()),
('LOT-012-A','SKU-012',120,CURRENT_DATE+170,'2026-05-10',true,false,15000.00,1,NOW()),
-- Costena Six-pack y Caja
('LOT-005-A','SKU-005',110,CURRENT_DATE+45,'2026-02-01',true,true,7500.00,1,NOW()),
('LOT-013-A','SKU-013',95,CURRENT_DATE+160,'2026-04-22',true,false,14500.00,2,NOW()),
-- Corona Six-pack, Caja, Barril
('LOT-006-A','SKU-006',60,CURRENT_DATE+200,'2026-03-15',true,false,12500.00,1,NOW()),
('LOT-014-A','SKU-014',80,CURRENT_DATE+270,'2026-04-30',true,false,24500.00,4,NOW()),
('LOT-030-A','SKU-030',8,CURRENT_DATE+400,'2026-04-30',true,false,145000.00,4,NOW()),
-- Heineken Six-pack, Caja, Barril, Estiba
('LOT-007-A','SKU-007',70,CURRENT_DATE+180,'2026-03-01',true,false,13500.00,1,NOW()),
('LOT-015-A','SKU-015',120,CURRENT_DATE+240,'2026-04-24',true,false,26500.00,3,NOW()),
('LOT-028-A','SKU-028',15,CURRENT_DATE+365,'2026-04-24',true,false,165000.00,3,NOW()),
('LOT-034-A','SKU-034',3,CURRENT_DATE+450,'2026-04-24',true,false,950000.00,3,NOW()),
-- Budweiser Six-pack y Caja
('LOT-008-A','SKU-008',100,CURRENT_DATE+130,'2026-02-20',true,false,11000.00,1,NOW()),
('LOT-016-A','SKU-016',40,CURRENT_DATE+250,'2026-04-25',true,false,22500.00,1,NOW()),
-- Stella Artois Caja y Barril
('LOT-017-A','SKU-017',30,CURRENT_DATE+280,'2026-04-25',true,false,28500.00,1,NOW()),
('LOT-029-A','SKU-029',5,CURRENT_DATE+400,'2026-04-25',true,false,165000.00,1,NOW()),
-- Andina Caja
('LOT-018-A','SKU-018',85,CURRENT_DATE+165,'2026-05-01',true,false,15500.00,1,NOW()),
-- Miller Caja
('LOT-019-A','SKU-019',65,CURRENT_DATE+190,'2026-04-30',true,false,19500.00,4,NOW()),
-- Redds Caja
('LOT-020-A','SKU-020',55,CURRENT_DATE+210,'2026-04-28',true,false,14500.00,1,NOW()),
-- Strongbow Caja
('LOT-021-A','SKU-021',45,CURRENT_DATE+220,'2026-04-28',true,false,16500.00,1,NOW()),
-- Buque Caja
('LOT-022-A','SKU-022',75,CURRENT_DATE+175,'2026-05-01',true,false,14800.00,1,NOW()),
-- Tecate Caja
('LOT-023-A','SKU-023',35,CURRENT_DATE+260,'2026-04-30',true,false,18500.00,4,NOW()),
-- Modelo Especial Caja
('LOT-024-A','SKU-024',25,CURRENT_DATE+280,'2026-04-30',true,false,22500.00,4,NOW()),
-- Poker Barril y Estiba
('LOT-025-A','SKU-025',10,CURRENT_DATE+300,'2026-04-22',true,false,125000.00,2,NOW()),
('LOT-031-A','SKU-031',5,CURRENT_DATE+400,'2026-04-22',true,false,680000.00,2,NOW()),
-- Aguila Barril y Estiba
('LOT-026-A','SKU-026',12,CURRENT_DATE+320,'2026-04-22',true,false,128000.00,2,NOW()),
('LOT-032-A','SKU-032',4,CURRENT_DATE+420,'2026-04-22',true,false,690000.00,2,NOW()),
-- Club Colombia Estiba
('LOT-033-A','SKU-033',3,CURRENT_DATE+380,'2026-04-22',true,false,720000.00,2,NOW())
ON CONFLICT (codigo_lote) DO NOTHING;

-- ============================================================
-- 4. STOCK GLOBAL SKU
-- ============================================================
INSERT INTO stock_global_sku (sku_id, disponibles, comprometidos, fisico_total, precio)
SELECT
    l.sku_id,
    SUM(l.cantidad) AS disponibles,
    0 AS comprometidos,
    SUM(l.cantidad) AS fisico_total,
    MAX(l.costo_unitario_producto) AS precio
FROM lote l
WHERE l.disponible = true
GROUP BY l.sku_id
ON CONFLICT (sku_id) DO UPDATE SET
    disponibles = EXCLUDED.disponibles,
    fisico_total = EXCLUDED.fisico_total,
    precio = EXCLUDED.precio;

-- ============================================================
-- 5. EXCEPCIONES DE INVENTARIO
-- ============================================================
INSERT INTO excepcion_inventario (excepcion_id, tipo_excepcion, codigo_lote, sku_id, cantidad_afectada, fecha_registro, operario_id, descripcion, evidencia_url) VALUES
(1,'AVERIA','LOT-001-A','SKU-001',3,NOW()-INTERVAL'3 days',1,'Six-packs rotos detectados durante picking',NULL),
(2,'VENCIMIENTO','LOT-005-A','SKU-005',5,NOW()-INTERVAL'2 days',3,'Six-pack Costena vencido detectado en revision',NULL),
(3,'DIFERENCIA','LOT-009-A','SKU-009',8,NOW()-INTERVAL'1 day',3,'Diferencia en inventario de Caja Poker',NULL),
(4,'FALTANTE','LOT-028-A','SKU-028',1,NOW()-INTERVAL'12 hours',1,'Barril Heineken faltante al confirmar despacho',NULL),
(5,'AVERIA','LOT-034-A','SKU-034',1,NOW()-INTERVAL'2 hours',3,'Estiba Heineken con empaque danado en supervisión',NULL),
(6,'DIFERENCIA','LOT-025-A','SKU-025',2,NOW()-INTERVAL'1 hour',3,'Diferencia en Barriles Poker contados',NULL)
ON CONFLICT (excepcion_id) DO NOTHING;

-- ============================================================
-- 6. MOVIMIENTOS DE INVENTARIO
-- ============================================================
INSERT INTO movimiento_inventario (movimiento_id, codigo_lote, tipo_movimiento, cantidad, fecha_movimiento, pedido_id, excepcion_id, operario_id, observaciones) VALUES
(1,'LOT-001-A','ENTRADA',120,NOW()-INTERVAL'15 days',NULL,NULL,5,'Ingreso inicial lote LOT-001-A'),
(2,'LOT-002-A','ENTRADA',130,NOW()-INTERVAL'14 days',NULL,NULL,5,'Ingreso inicial lote LOT-002-A'),
(3,'LOT-003-A','ENTRADA',90,NOW()-INTERVAL'14 days',NULL,NULL,5,'Ingreso inicial lote LOT-003-A'),
(4,'LOT-009-A','ENTRADA',200,NOW()-INTERVAL'10 days',NULL,NULL,5,'Ingreso lote LOT-009-A (Manifiesto Bavaria)'),
(5,'LOT-010-A','ENTRADA',150,NOW()-INTERVAL'10 days',NULL,NULL,5,'Ingreso lote LOT-010-A (Manifiesto Bavaria)'),
(6,'LOT-001-A','BAJA_AVERIA',3,NOW()-INTERVAL'3 days',NULL,1,1,'Avería registrada por picking'),
(7,'LOT-005-A','BAJA_VENCIMIENTO',5,NOW()-INTERVAL'2 days',NULL,2,3,'Baja por vencimiento en revision de estante'),
(8,'LOT-003-A','COMPROMISO',25,NOW()-INTERVAL'4 days',2,NULL,4,'Compromiso pedido PED-2026-002'),
(9,'LOT-001-A','PICKING',12,NOW()-INTERVAL'2 days',4,NULL,1,'Picking confirmado PED-2026-004'),
(10,'LOT-028-A','SALIDA',1,NOW()-INTERVAL'1 day',5,NULL,2,'Despacho Barril Heineken PED-2026-005')
ON CONFLICT (movimiento_id) DO NOTHING;

-- ============================================================
-- 7. PEDIDOS, PRODUCTOS_PEDIDO Y LOTES_COMPROMETIDOS
-- ============================================================
INSERT INTO pedidos (pedido_id, numero_pedido, cliente_cc, cliente_nombre, direccion_entrega, fecha_creacion, estado, ruta_id, fecha_compromiso, asesor_id, operario_picking_id, operario_despacho_id) VALUES
(1,'PED-2026-001','1234567890','Tienda El Porvenir','Calle 10 #20-30, Medellin',NOW()-INTERVAL'5 days','ESPERANDO_RUTA',NULL,NULL,4,NULL,NULL),
(2,'PED-2026-002','9876543210','Minimercado Central','Carrera 45 #12-34, Envigado',NOW()-INTERVAL'4 days','COMPROMETIDO',NULL,NULL,4,NULL,NULL),
(3,'PED-2026-003','5555555555','Supermercado Mercafacil','Calle 8 Sur #50-25, Itagui',NOW()-INTERVAL'3 days','COMPROMETIDO',NULL,NULL,4,NULL,NULL),
(4,'PED-2026-004','1111111111','Licorera Don Juan','Av. Poblado #5-10, Medellin',NOW()-INTERVAL'2 days','EN_PICKING',NULL,NULL,4,1,NULL),
(5,'PED-2026-005','2222222222','Estanco La 70','Circular 1 #70-20, Medellin',NOW()-INTERVAL'1 day','DESPACHADO',NULL,NULL,4,1,2),
(6,'PED-2026-006','3333333333','Barrio La Cerveza','Calle 100 #20-10, Medellin',NOW()-INTERVAL'6 hours','ENTREGADO',NULL,NULL,4,1,2),
(7,'PED-2026-007','4444444444','Super Cervecero','Carrera 80 #33-44, Medellin',NOW()-INTERVAL'3 hours','ESPERANDO_RUTA',NULL,NULL,4,NULL,NULL),
(8,'PED-2026-008','6666666666','Mercado Cervecero','Calle 50 #40-30, Bello',NOW()-INTERVAL'1 hour','COMPROMETIDO',NULL,NULL,4,NULL,NULL),
(9,'PED-2026-009','7777777777','Autoservicio Express','Av 80 #10-10, Medellin',NOW()-INTERVAL'30 min','PICKUP',NULL,NULL,4,1,NULL),
(10,'PED-2026-010','8888888888','Tienda de Conveniencia','Calle 25 #45-60, Itagui',NOW()-INTERVAL'10 min','ESPERANDO_RUTA',NULL,NULL,4,NULL,NULL)
ON CONFLICT (pedido_id) DO NOTHING;

INSERT INTO productos_pedido (producto_pedido_id, pedido_id, sku_id, cantidad_solicitada, cantidad_confirmada) VALUES
(1,1,'SKU-001',10,0),
(2,1,'SKU-009',5,0),
(3,2,'SKU-003',12,12),
(4,2,'SKU-011',6,6),
(5,3,'SKU-007',8,8),
(6,3,'SKU-015',4,4),
(7,4,'SKU-001',6,6),
(8,5,'SKU-028',1,1),
(9,7,'SKU-010',10,0),
(10,8,'SKU-013',8,0),
(11,9,'SKU-019',6,6),
(12,10,'SKU-020',12,0)
ON CONFLICT (producto_pedido_id) DO NOTHING;

INSERT INTO lotes_comprometidos (compromiso_id, producto_pedido_id, codigo_lote, cantidad_comprometida, fecha_compromiso) VALUES
(1,3,'LOT-003-A',12,NOW()-INTERVAL'4 days'),
(2,4,'LOT-011-A',6,NOW()-INTERVAL'4 days'),
(3,5,'LOT-007-A',8,NOW()-INTERVAL'3 days'),
(4,6,'LOT-015-A',4,NOW()-INTERVAL'3 days'),
(5,7,'LOT-001-B',6,NOW()-INTERVAL'2 days'),
(6,8,'LOT-028-A',1,NOW()-INTERVAL'1 day')
ON CONFLICT (compromiso_id) DO NOTHING;

-- ============================================================
-- 8. REGISTROS DE PICKING
-- ============================================================
INSERT INTO registro_picking (registro_picking_id, pedido_id, operario_id, fecha_picking, observaciones) VALUES
(1,2,1,NOW()-INTERVAL'2 days','Picking completado cervezas'),
(2,3,1,NOW()-INTERVAL'1 day','Picking completado'),
(3,4,1,NOW()-INTERVAL'12 hours','En proceso'),
(4,5,1,NOW()-INTERVAL'6 hours','Picking completado Barril')
ON CONFLICT (registro_picking_id) DO NOTHING;

-- ============================================================
-- 9. REGISTROS DE DESPACHO
-- ============================================================
INSERT INTO registro_despacho (registro_despacho_id, pedido_id, operario_id, transportista, placa_vehiculo, fecha_despacho, observaciones) VALUES
(1,5,2,'Transportes Rapido','ABZ-123',NOW()-INTERVAL'2 days','Despacho Barril Heineken'),
(2,6,2,'Transportes Express','XYZ-456',NOW()-INTERVAL'1 day','Despacho pedido entregado')
ON CONFLICT (registro_despacho_id) DO NOTHING;

-- ============================================================
-- 10. SINCRONIZAR SECUENCES (BIGSERIAL) TRAS INSERTS EXPLÍCITOS
-- ============================================================
SELECT setval('manifiesto_manifiesto_id_seq',      COALESCE((SELECT MAX(manifiesto_id)     FROM manifiesto),     1));
SELECT setval('detalle_manifiesto_detalle_id_seq',  COALESCE((SELECT MAX(detalle_id)       FROM detalle_manifiesto), 1));
SELECT setval('recepcion_recepcion_id_seq',         COALESCE((SELECT MAX(recepcion_id)     FROM recepcion),     1));
SELECT setval('excepcion_inventario_excepcion_id_seq', COALESCE((SELECT MAX(excepcion_id) FROM excepcion_inventario), 1));
SELECT setval('movimiento_inventario_movimiento_id_seq', COALESCE((SELECT MAX(movimiento_id) FROM movimiento_inventario), 1));
SELECT setval('pedidos_pedido_id_seq',               COALESCE((SELECT MAX(pedido_id)       FROM pedidos),        1));
SELECT setval('productos_pedido_producto_pedido_id_seq', COALESCE((SELECT MAX(producto_pedido_id) FROM productos_pedido), 1));
SELECT setval('lotes_comprometidos_compromiso_id_seq', COALESCE((SELECT MAX(compromiso_id) FROM lotes_comprometidos), 1));
SELECT setval('registro_picking_registro_picking_id_seq', COALESCE((SELECT MAX(registro_picking_id) FROM registro_picking), 1));
SELECT setval('registro_despacho_registro_despacho_id_seq', COALESCE((SELECT MAX(registro_despacho_id) FROM registro_despacho), 1));
