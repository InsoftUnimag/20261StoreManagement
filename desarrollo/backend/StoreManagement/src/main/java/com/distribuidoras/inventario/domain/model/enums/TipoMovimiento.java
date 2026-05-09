package com.distribuidoras.inventario.domain.model.enums;

/**
 * Tipos de movimiento de inventario (Kardex).
 * Spec: 04_registrar_ingreso_productos.md, 16_reportar_excepciones_inventario.md
 */
public enum TipoMovimiento {
    ENTRADA,
    COMPROMISO,
    PICKING,
    SALIDA,
    BAJA_AVERIA,
    BAJA_VENCIMIENTO,
    FALTANTE,
    REASIGNACION
}
