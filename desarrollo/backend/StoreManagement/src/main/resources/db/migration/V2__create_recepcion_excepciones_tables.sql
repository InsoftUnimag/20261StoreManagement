-- V2: Crear tablas para Recepción de Mercancía y Excepciones de Inventario
-- Spec: 04_registrar_ingreso_productos.md, 16_reportar_excepciones_inventario.md
-- Formato skuId: SKU-001, SKU-012, SKU-111, etc.

-- Manifiesto: documento de fábrica con productos esperados
CREATE TABLE manifiesto (
    manifiesto_id       BIGSERIAL        PRIMARY KEY,
    numero_manifiesto VARCHAR(50)     NOT NULL UNIQUE,
    fecha_emision     DATE            NOT NULL,
    proveedor         VARCHAR(200)    NOT NULL,
    estado            VARCHAR(30)     NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado IN ('PENDIENTE', 'RECEPCIONADO_PARCIAL', 'RECEPCIONADO_TOTAL')),
    creado_el        TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Detalle de manifiesto: líneas con cantidades esperadas por SKU
CREATE TABLE detalle_manifiesto (
    detalle_id        BIGSERIAL        PRIMARY KEY,
    manifiesto_id     BIGINT            NOT NULL,
    sku_id           VARCHAR(20)     NOT NULL,
    cantidad_esperada INTEGER       NOT NULL CHECK (cantidad_esperada > 0),
    cantidad_recibida INTEGER       NOT NULL DEFAULT 0,

    CONSTRAINT fk_detalle_manifiesto FOREIGN KEY (manifiesto_id)
        REFERENCES manifiesto (manifiesto_id),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (sku_id)
        REFERENCES producto (sku_id)
);

-- Recepción: evento de recepción física de mercancía
CREATE TABLE recepcion (
    recepcion_id     BIGSERIAL        PRIMARY KEY,
    numero_recepcion VARCHAR(50)     NOT NULL UNIQUE,
    manifiesto_id    BIGINT,
    operario_id     BIGINT,
    fecha_recepcion TIMESTAMP       NOT NULL DEFAULT NOW(),
    notas            TEXT,

    CONSTRAINT fk_recepcion_manifiesto FOREIGN KEY (manifiesto_id)
        REFERENCES manifiesto (manifiesto_id)
);

-- Lote: unidad de trazabilidad con fecha de vencimiento (FEFO)
CREATE TABLE lote (
    codigo_lote       VARCHAR(100)    PRIMARY KEY,
    sku_id           VARCHAR(20)     NOT NULL,
    cantidad         INTEGER         NOT NULL CHECK (cantidad >= 0),
    fecha_vencimiento DATE            NOT NULL,
    fecha_expedicion  DATE,
    disponible       BOOLEAN          NOT NULL DEFAULT TRUE,
    flag_urgencia_fefo BOOLEAN          NOT NULL DEFAULT FALSE,
    costo_unitario_producto DECIMAL(15,2),
    recepcion_id     BIGINT            NOT NULL,
    creado_el        TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_lote_producto FOREIGN KEY (sku_id)
        REFERENCES producto (sku_id),
    CONSTRAINT fk_lote_recepcion FOREIGN KEY (recepcion_id)
        REFERENCES recepcion (recepcion_id),
    CONSTRAINT uk_lote_sku_codigo_vencimiento UNIQUE (sku_id, codigo_lote, fecha_vencimiento)
);

CREATE INDEX idx_lote_sku_vencimiento ON lote (sku_id, fecha_vencimiento);
CREATE INDEX idx_lote_sku_cantidad ON lote (sku_id, cantidad);

-- Stock Global SKU: Consolidado de stock
CREATE TABLE stock_global_sku (
    sku_id           VARCHAR(20)     PRIMARY KEY,
    disponibles     INTEGER         NOT NULL DEFAULT 0,
    comprometidos   INTEGER         NOT NULL DEFAULT 0,
    fisico_total    INTEGER         NOT NULL DEFAULT 0,
    precio          DECIMAL(15,2)   DEFAULT 0.00,

    CONSTRAINT fk_stock_global_producto FOREIGN KEY (sku_id)
        REFERENCES producto (sku_id)
);

-- Excepción de inventario: registro de anomalías
CREATE TABLE excepcion_inventario (
    excepcion_id    BIGSERIAL        PRIMARY KEY,
    tipo_excepcion  VARCHAR(30)     NOT NULL
        CHECK (tipo_excepcion IN ('AVERIA', 'VENCIMIENTO', 'DIFERENCIA', 'FALTANTE')),
    codigo_lote     VARCHAR(100),
    sku_id          VARCHAR(20)     NOT NULL,
    cantidad_afectada INTEGER       NOT NULL CHECK (cantidad_afectada > 0),
    fecha_registro  TIMESTAMP       NOT NULL DEFAULT NOW(),
    operario_id    BIGINT,
    descripcion    TEXT            NOT NULL,
    evidencia_url VARCHAR(500),

    CONSTRAINT fk_excepcion_lote FOREIGN KEY (codigo_lote)
        REFERENCES lote (codigo_lote),
    CONSTRAINT fk_excepcion_producto FOREIGN KEY (sku_id)
        REFERENCES producto (sku_id)
);

CREATE INDEX idx_excepcion_tipo ON excepcion_inventario (tipo_excepcion);

-- Movimiento de inventario (Kardex): registro contable de movimientos de stock
CREATE TABLE movimiento_inventario (
    movimiento_id    BIGSERIAL        PRIMARY KEY,
    codigo_lote     VARCHAR(100)    NOT NULL,
    tipo_movimiento VARCHAR(30)    NOT NULL
        CHECK (tipo_movimiento IN ('ENTRADA', 'COMPROMISO', 'PICKING', 'SALIDA',
               'BAJA_AVERIA', 'BAJA_VENCIMIENTO', 'FALTANTE')),
    cantidad        INTEGER         NOT NULL,
    fecha_movimiento TIMESTAMP      NOT NULL DEFAULT NOW(),
    pedido_id       BIGINT,
    excepcion_id    BIGINT,
    operario_id     BIGINT,
    observaciones   TEXT,

    CONSTRAINT fk_movimiento_lote FOREIGN KEY (codigo_lote)
        REFERENCES lote (codigo_lote),
    CONSTRAINT fk_movimiento_excepcion FOREIGN KEY (excepcion_id)
        REFERENCES excepcion_inventario (excepcion_id)
);

CREATE INDEX idx_movimiento_lote ON movimiento_inventario (codigo_lote);
CREATE INDEX idx_movimiento_tipo ON movimiento_inventario (tipo_movimiento);