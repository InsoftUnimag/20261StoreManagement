# Plan de Implementación: Picking y Despacho - Frontend

**Date**: 2026-04-03  
**Specs**: 11_consultar_pedidos_picking.md · 12_confirmar_picking.md · 13_listar_pedidos_despacho.md · 14_confirmar_despacho.md  
**Module**: Módulo 1 - Inventario  
**Layer**: Frontend  
**Priority**: P1

---

## Summary

Interfaces para operaciones de almacén: Picking (recolección) y Despacho (salida). Operarios consultan pedidos listos, confirman picking/despacho, y ven detalles de lotes a recolectar. Estados: Comprometido → En Picking → Despachado.

---

## Dependencies

**Blocked by**:
- Plan Picking/Despacho Backend (endpoints REST)

---

## Pages & Components

### 1. PickingPage
- Lista de pedidos en estado Comprometido (listos para picking)
- Tabla: Número Pedido, Cliente, Ruta, Fecha Compromiso, Total Unidades
- Ordenamiento FIFO (más antiguos primero)
- Click en fila → modal ConfirmarPickingModal

**API Call**: `GET /api/v1/picking/pedidos`

### 2. ConfirmarPickingModal
- Card con información del pedido
- Tabla de líneas con lotes comprometidos (para facilitar recolección):
  - Producto, Código Lote, Cantidad, Ubicación (si existe)
- Input: Observaciones (opcional)
- Botón "Confirmar Picking"
- POST /api/v1/picking/confirmar
- Success: Cerrar modal, recargar lista, toast "Picking confirmado"

### 3. DespachoPage
- Lista de pedidos en estado En Picking (listos para despacho)
- Tabla: Número Pedido, Cliente, Dirección, Fecha Picking, Tiempo desde picking, Alerta
- Badge de alerta roja si tiempo desde picking > 5 min (SC-035)
- Ordenamiento FIFO (más antiguos primero)
- Click en fila → modal ConfirmarDespachoModal

**API Call**: `GET /api/v1/despacho/pedidos`

### 4. ConfirmarDespachoModal
- Card con información del pedido
- Form inputs:
  - Transportista: text input (required)
  - Placa Vehículo: text input (opcional)
  - Observaciones: textarea (opcional)
- Resumen de líneas (read-only)
- Botón "Confirmar Despacho"
- POST /api/v1/despacho/confirmar
- Success: Cerrar modal, recargar lista, toast "Despacho confirmado"

---

## Services

**PickingService**:
- `getPedidosParaPicking(page, size)` → GET /api/v1/picking/pedidos
- `confirmarPicking(pedidoId, data)` → POST /api/v1/picking/confirmar
- `getDetallePicking(pedidoId)` → GET /api/v1/picking/{pedido_id}/detalle

**DespachoService**:
- `getPedidosParaDespacho(page, size)` → GET /api/v1/despacho/pedidos
- `confirmarDespacho(pedidoId, data)` → POST /api/v1/despacho/confirmar
- `getDetalleDespacho(pedidoId)` → GET /api/v1/despacho/{pedido_id}/detalle

---

## Key Components

**PickingTable**: Tabla de pedidos para picking con FIFO
**DespachoTable**: Tabla de pedidos para despacho con alerta de tiempo
**ConfirmarPickingModal**: Modal para confirmar picking
**ConfirmarDespachoModal**: Modal para confirmar despacho
**LotesRecolectarTable**: Tabla de lotes a recolectar (guía para operario)
**AlertaTiempoBadge**: Badge rojo si tiempo > 5 min

---

## Implementation Tasks

**T001-T002**: Crear services (PickingService, DespachoService)
**T003-T008**: Crear components (PickingTable, DespachoTable, ConfirmarPickingModal, ConfirmarDespachoModal, LotesRecolectarTable, AlertaTiempoBadge)
**T009-T010**: Implementar pages (PickingPage, DespachoPage)
**T011-T013**: Configurar routing, cálculo de tiempo, styling, testing

---

## Tests
- Services: 4 tests
- Components: 6 tests
- Pages: 2 tests

---

## Acceptance Criteria

**FR-067**: Solo pedidos en estado Comprometido pueden pasar a Picking.
**FR-068**: Picking NO modifica stock (solo cambia estado).
**FR-070**: Solo pedidos en estado En Picking pueden pasar a Despachado.
**FR-071**: Despacho reduce stock y genera MovimientoInventario.
**SC-035**: Alerta visual si picking no completado en 5 min.
