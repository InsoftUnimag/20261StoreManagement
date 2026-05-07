# Plan de Implementación: Consulta de Inventario - Frontend

**Date**: 2026-04-03  
**Specs**: 05_consultar_stock_disponible.md · 06_consultar_movimientos.md  
**Module**: Módulo 1 - Inventario  
**Layer**: Frontend  
**Priority**: P2

---

## Summary

Interfaces de consulta para visualizar stock disponible por SKU con detalle de lotes (FEFO) y kardex (movimientos de inventario). Solo lectura, consume endpoints de consulta del backend.

---

## Dependencies

**Blocked by**:
- Plan Consulta Inventario Backend (endpoints REST)

---

## Pages & Components

### 1. StockDisponiblePage
- Buscador de productos (autocomplete por marca)
- Card principal: Stock total con gráfico de gauge
- Tabla de lotes: Código, Vencimiento, Stock, Días hasta vencer
- Lotes ordenados por FEFO (fecha_vencimiento ASC)
- Badge de alerta si lote vence en < 30 días

**API Call**: `GET /api/v1/inventario/stock/{sku_id}`

### 2. KardexPage
- Filtros: SKU, Lote, Tipo de movimiento, Rango de fechas
- Tabla de movimientos: Fecha, Tipo, Cantidad, Lote, Producto, Operario
- Paginación (20 registros por página)
- Ordenamiento: fecha DESC (más recientes primero)
- Íconos por tipo de movimiento (Entrada: +, Salida: -, Compromiso: 🔒)

**API Call**: `GET /api/v1/inventario/movimientos?filters&page&size`

### 3. DashboardInventarioPage
- Resumen general: Total SKUs, Total lotes, Stock total unidades
- Alertas: Próximos a vencer, Stock bajo, Excepciones abiertas
- Gráfico de movimientos del día (Entradas vs Salidas)
- Últimos movimientos (tabla con 10 más recientes)

**API Call**: `GET /api/v1/inventario/resumen`

---

## Services

**InventarioService**:
- `getStockPorSku(skuId)`
- `getMovimientos(filtros, page, size)`
- `getDetalleLote(loteId)`
- `getResumenInventario()`

---

## Key Components

**StockCard**: Card con stock total y gráfico gauge
**LoteTable**: Tabla de lotes con FEFO y alertas
**MovimientoTable**: Tabla de kardex con filtros
**MovimientoIcon**: Íconos coloreados por tipo
**FiltrosKardex**: Panel de filtros colapsable

---

## Implementation Tasks

**T001-T002**: Crear services (InventarioService)
**T003-T007**: Crear components (StockCard, LoteTable, MovimientoTable, MovimientoIcon, FiltrosKardex)
**T008-T010**: Implementar pages (StockDisponiblePage, KardexPage, DashboardInventarioPage)
**T011-T013**: Configurar routing, charts (Chart.js o Recharts), styling, testing

---

## Tests
- Services: 4 tests
- Components: 5 tests
- Pages: 3 tests

---

## Acceptance Criteria

**FR-034**: Operarios pueden consultar stock disponible de cualquier SKU.
**FR-035**: Lotes se muestran ordenados por fecha de vencimiento (FEFO).
**FR-043**: Supervisores pueden consultar kardex con filtros.
**FR-044**: Kardex incluye información contextual completa.
