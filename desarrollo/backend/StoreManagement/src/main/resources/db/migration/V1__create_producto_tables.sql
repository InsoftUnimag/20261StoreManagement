-- V2: Crear tablas de Producto (SKU) y Bitácora de Producto
-- Spec: 01_crear_plantilla_producto.md, 02_modificar_plantilla_producto.md

CREATE TABLE producto (
    sku_id          UUID            PRIMARY KEY,
    marca           VARCHAR(100)    NOT NULL,
    presentacion    VARCHAR(100)    NOT NULL,
    contenido_ml    INTEGER         NOT NULL,
    peso_logistico_kg DECIMAL(10,3) NOT NULL CHECK (peso_logistico_kg > 0),
    creado_el       TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_producto_marca_presentacion UNIQUE (marca, presentacion)
);

CREATE INDEX idx_producto_marca ON producto (marca);
CREATE INDEX idx_producto_presentacion ON producto (presentacion);

CREATE TABLE bitacora_producto (
    id              BIGSERIAL       PRIMARY KEY,
    sku_id_ref      UUID            NOT NULL,
    campo           VARCHAR(50)     NOT NULL,
    valor_anterior  VARCHAR(255),
    valor_nuevo     VARCHAR(255),
    descripcion     VARCHAR(500),
    fecha           TIMESTAMP       NOT NULL DEFAULT NOW(),
    usuario         VARCHAR(100),

    CONSTRAINT fk_bitacora_producto FOREIGN KEY (sku_id_ref)
        REFERENCES producto (sku_id) ON DELETE CASCADE
);

CREATE INDEX idx_bitacora_sku ON bitacora_producto (sku_id_ref);
