# Implementation Plan: Consulta de Inventario y Disponibilidad

**Date**: 2026-03-27
**Specs**: [`05_consultar_inventario.md`](../especificaciones/05_consultar_inventario.md) · [`06_consultar_disponibilidad.md`](../especificaciones/06_consultar_disponibilidad.md)

---

## Summary

Implementación de las vistas de stock para dos actores distintos: el **Supervisor de Inventario** necesita visibilidad completa con lotes ordenados por FEFO y lotes críticos resaltados. El **Asesor Comercial** (y el sistema internamente al crear pedidos) necesita verificar si un SKU tiene suficiente stock DISPONIBLE para cubrir una cantidad solicitada.

---

## Technical Context

**Language/Version**: Java 21
**Framework**: Spring Boot 3.x — Clean Architecture
**Storage**: PostgreSQL — lectura sobre tablas `lote`, `movimiento_inventario`
**Testing**: JUnit 5, Mockito (unit), @SpringBootTest (integración)
**Performance Goals**:
- Consulta de inventario en tiempo real (SC-018)
- El 100% de lotes críticos están resaltados (SC-019)
**Constraints**:
- Stock disponible = suma de lotes en estado DISPONIBLE únicamente (excluye COMPROMETIDO)
- La consulta de disponibilidad es invocada automáticamente al crear un pedido (FR-068)

---

## Project Structure (Clean Architecture)

```text
domain/
├── model/               # (Lote ya definido en plan_recepcion_excepciones.md)
└── port/
    └── InventarioQueryPort.java   # Puerto de consulta (lectura FEFO, disponibilidad)

application/usecase/
├── ConsultarInventarioUseCase.java    # Spec 05 — para Supervisor
└── ConsultarDisponibilidadUseCase.java  # Spec 06 — interno y para Asesor

infrastructure/
├── persistence/
│   └── InventarioQueryAdapter.java    # Implementa InventarioQueryPort (queries JPQL/SQL)
└── web/
    ├── InventarioController.java
    └── dto/
        ├── InventarioProductoResponse.java   # SKU + lista de lotes ordenada por FEFO
        ├── LoteDetalleResponse.java          # código, fechaVenc, cantidad, estado, critico
        └── DisponibilidadResponse.java       # skuId, disponible (boolean), stockActual

prototipo/src/pages/
└── InventarioPage.jsx         # Supervisor — tabla FEFO con badges de estado crítico
```

---

## Phase 1: Foundational — Puerto de Consulta de Inventario

- [ ] T001 Crear `InventarioQueryPort.java` con métodos:
  - `List<Lote> consultarPorSku(UUID skuId)` → lotes ordenados por FEFO (fecha_vencimiento ASC)
  - `boolean verificarDisponibilidad(UUID skuId, int cantidad)` → booleano
  - `int stockDisponible(UUID skuId)` → suma de lotes en estado DISPONIBLE
- [ ] T002 Crear `InventarioQueryAdapter.java` (implementa el port) con query optimizada:
  - Filtrar solo lotes estado DISPONIBLE para el stock
  - Ordenar por `fecha_vencimiento ASC`
  - Flag `critico = true` si `fecha_vencimiento <= CURRENT_DATE + N_DIAS` (parámetro configurable)
- [ ] T003 Definir DTOs: `InventarioProductoResponse`, `LoteDetalleResponse`, `DisponibilidadResponse`

**Checkpoint**: Las queries funcionan correctamente en PostgreSQL con datos de prueba.

---

## Phase 2: US 1 — Consultar Inventario Full (Spec 05, P2)

**Goal**: El Supervisor ve el inventario completo de un SKU: stock total, lotes ordenados FEFO, lotes críticos resaltados, diferencia entre stock DISPONIBLE y COMPROMETIDO.

**Independent Test**: `GET /api/inventario/{skuId}` con SKU de múltiples lotes → stock total correcto, lista ordenada por fechaVencimiento ASC, lotes críticos con `critico: true`.

- [ ] T004 [US1-Spec05] Implementar `ConsultarInventarioUseCase.ejecutar(UUID skuId)`:
  - Obtener lotes por SKU (todos los estados para historial)
  - Calcular stock total solo de estado DISPONIBLE
  - Calcular stock comprometido solo de estado COMPROMETIDO
  - Marcar lotes críticos según umbral configurable
- [ ] T005 [US1-Spec05] Implementar `GET /api/inventario/{skuId}` en `InventarioController`
  - Incluir filtros opcionales: `?estado=DISPONIBLE` y `?fechaDesde=`
- [ ] T006 [US1-Spec05] Implementar `GET /api/inventario/{skuId}/movimientos` para auditoría (FR-041)
- [ ] T007 [US1-Spec05] Tests unitarios de `ConsultarInventarioUseCase` (múltiples lotes, sin stock, con críticos)
- [ ] T008 [US1-Spec05] Tests de integración de `GET /api/inventario/{skuId}`
- [ ] T009 [US1-Spec05] Frontend: `InventarioPage.jsx` con tabla ordenada FEFO, badge "CRÍTICO" en lotes por vencer, diferenciación visual DISPONIBLE vs COMPROMETIDO

**Checkpoint**: `SC-017`, `SC-018`, `SC-019` verificados.

---

## Phase 3: US 1 — Consultar Disponibilidad (Spec 06, P1)

**Goal**: El sistema verifica automáticamente si hay stock DISPONIBLE suficiente para cubrir un pedido. También es consultable manualmente por el Asesor Comercial.

**Independent Test**: `GET /api/disponibilidad?skuId={id}&cantidad={n}` → retorna `disponible: true/false` y `stockActual`.

- [ ] T010 [US1-Spec06] Implementar `ConsultarDisponibilidadUseCase.ejecutar(UUID skuId, int cantidadSolicitada)`:
  - Sumar stock de lotes DISPONIBLE
  - Comparar con cantidad solicitada
  - Retornar objeto con: `disponible (boolean)`, `stockActual`, `cantidadSolicitada`, `mensaje`
- [ ] T011 [US1-Spec06] Implementar `GET /api/disponibilidad` en `InventarioController`
- [ ] T012 [US1-Spec06] Tests unitarios para los tres escenarios: stock suficiente, sin stock, stock insuficiente
- [ ] T013 [US1-Spec06] Tests de integración de `GET /api/disponibilidad`

**Checkpoint**: `SC-025` verificado. La verificación de disponibilidad funciona correctamente y es reutilizable por el use case de pedidos.

---

## Dependencies & Execution Order

- Este plan depende de que existan lotes en BD (plan_recepcion_excepciones.md debe estar completo)
- `ConsultarDisponibilidadUseCase` (T010) será **importado y reutilizado** por `RealizarPedidoUseCase` (plan_pedidos.md) — no duplicar lógica
- T004 y T010 pueden desarrollarse en paralelo

## Notes

- El `InventarioQueryPort` es de solo lectura (Command Query Separation)
- El umbral crítico de vencimiento debe ser el mismo parámetro de `application.yml` del plan de recepción
- Stock mostrado al Asesor en el catálogo (Spec 03) también usa `stockDisponible()` de este port
