# Implementation Plan: Picking y Despacho

**Date**: 2026-03-27
**Specs**: [`10_listar_pedidos_comprometidos.md`](../especificaciones/10_listar_pedidos_comprometidos.md) · [`11_confirmar_picking.md`](../especificaciones/11_confirmar_picking.md) · [`12_listar_manifiesto.md`](../especificaciones/12_listar_manifiesto.md) · [`13_solicitar_ruta.md`](../especificaciones/13_solicitar_ruta.md) · [`14_confirmar_despacho.md`](../especificaciones/14_confirmar_despacho.md)

---

## Summary

Flujo de salida física del inventario. El `Operario de Picking` consulta los pedidos comprometidos, confirma el picking físico (lo que cambia el estado de los lotes de COMPROMETIDO → EN_PICKING) y reporta cualquier anomalía. El `Conductor` consulta su manifiesto de carga, solicita la ruta al sistema y confirma la salida del vehículo, lo que cambia los lotes a DESPACHADO.

---

## Technical Context

**Language/Version**: Java 21
**Framework**: Spring Boot 3.x — Clean Architecture
**Storage**: PostgreSQL — lectura de `pedido`, `lote`, escritura en `registro_picking`, `movimiento_inventario`
**Testing**: JUnit 5, Mockito (unit), @SpringBootTest (integración)
**Performance Goals**: Confirmación de picking (1 pedido, ≤ 10 líneas) ≤ 5 minutos (SC-035)
**Constraints**:
- Un pedido en estado EN_PICKING no puede volver a confirmarse (FR-070)
- Picking parcial permitido: se toman solo las unidades reales disponibles (FR-072)
- Control de concurrencia: dos operarios no pueden tomar el mismo lote simultáneamente

---

## Project Structure (Clean Architecture)

```text
domain/
├── model/
│   └── RegistroPicking.java       # pedidoRef, loteRef, cantidadEsperada, cantidadReal, operario, fecha
├── port/
│   └── PickingRepository.java
└── exception/
    └── PedidoYaProcesadoException.java

application/usecase/
├── ListarPedidosComprometidosUseCase.java   # Spec 10
├── ConfirmarPickingUseCase.java             # Spec 11
├── ListarManifiestoUseCase.java             # Spec 12
├── SolicitarRutaUseCase.java                # Spec 13
└── ConfirmarDespachoUseCase.java            # Spec 14

infrastructure/
├── persistence/
│   ├── RegistroPickingJpaEntity.java
│   ├── RegistroPickingJpaRepository.java
│   └── PickingRepositoryAdapter.java
└── web/
    ├── PickingController.java
    ├── ManifiestoController.java
    ├── DespachoController.java
    └── dto/
        ├── PedidoComprometidoResponse.java   # numero, cliente, fecha, productos, lotes asignados
        ├── ConfirmarPickingRequest.java
        ├── ManifiestoResponse.java
        └── DespachoRequest.java

prototipo/src/pages/
├── PedidosComprometidosPage.jsx    # Operario — lista de pedidos a alistar
├── ConfirmarPickingPage.jsx        # Operario — detalle con confirmación por línea
├── ManifiestoPage.jsx              # Conductor — carga del vehículo
└── DespachoPage.jsx                # Conductor — confirmación de salida
```

---

## Phase 1: Setup de tablas

- [ ] T001 Crear `V5__create_picking_tables.sql`:
  - Tabla `registro_picking`: id, pedido_ref, lote_ref, cantidad_esperada, cantidad_real, operario, fecha
  - Índice en `pedido_ref` para consultas rápidas

---

## Phase 2: Foundational — Modelos y Puertos

- [ ] T002 Crear entidad de dominio `RegistroPicking.java`
- [ ] T003 Crear port `PickingRepository.java`
- [ ] T004 Implementar entidades JPA y adaptador de repositorio
- [ ] T005 Definir DTOs de request y response

**Checkpoint**: Estructura compila, las tablas existen en BD.

---

## Phase 3: US 1 — Listar Pedidos Comprometidos (Spec 10, P1)

**Goal**: El Operario de Picking ve todos los pedidos en estado COMPROMETIDO ordenados por fecha ASC (FIFO), con detalle de productos y lotes.

**Independent Test**: `GET /api/picking/pedidos` con pedidos comprometidos activos → lista ordenada FIFO con detalle de líneas y lotes asignados.

- [ ] T006 [US1-Spec10] Implementar `ListarPedidosComprometidosUseCase.ejecutar()`:
  - Filtrar pedidos en estado COMPROMETIDO
  - Ordenar por fecha de creación ASC (FIFO)
  - Incluir detalle: número, cliente, fecha, SKUs, cantidades, lotes asignados, dirección de entrega
  - Filtros opcionales: cliente, fecha
- [ ] T007 [US1-Spec10] Implementar `GET /api/picking/pedidos` en `PickingController`
- [ ] T008 [US1-Spec10] Tests de integración
- [ ] T009 [US1-Spec10] Frontend: `PedidosComprometidosPage.jsx` con tabla filtrable

**Checkpoint**: `SC-030`, `SC-031` verificados.

---

## Phase 4: US 1 — Confirmar Picking (Spec 11, P1)

**Goal**: El Operario confirma que tomó físicamente los productos. Los lotes pasan de COMPROMETIDO → EN_PICKING. Si hay faltante, el sistema busca stock alternativo.

**Independent Test**: `POST /api/picking/confirmar` sobre pedido COMPROMETIDO → lotes cambian a EN_PICKING, MovimientoInventario tipo PICKING registrado, pedido pasa a EN_PICKING.

- [ ] T010 [US1-Spec11] Implementar `ConfirmarPickingUseCase.ejecutar(UUID pedidoId, ConfirmarPickingRequest)`:
  - Verificar que pedido esté en estado COMPROMETIDO (lanzar `PedidoYaProcesadoException` si no)
  - Control de concurrencia con `@Lock(LockModeType.PESSIMISTIC_WRITE)` en el lote
  - Por cada línea: registrar `RegistroPicking` con cantidad real
  - Si `cantidadReal < cantidadEsperada` → buscar stock en otros lotes DISPONIBLE del mismo SKU
  - Si no hay stock extra → registrar `ExcepcionInventario` tipo FALTANTE y notificar al Supervisor
  - Cambiar lotes comprometidos de COMPROMETIDO → EN_PICKING
  - Cambiar estado del pedido a EN_PICKING
  - Registrar `MovimientoInventario` tipo PICKING por cada lote
- [ ] T011 [US1-Spec11] Implementar `POST /api/picking/confirmar` en `PickingController`
- [ ] T012 [US1-Spec11] Tests unitarios (picking completo, picking parcial con stock extra, picking parcial sin stock, pedido ya procesado)
- [ ] T013 [US1-Spec11] Tests de integración
- [ ] T014 [US1-Spec11] Frontend: `ConfirmarPickingPage.jsx` con líneas del pedido y campo de cantidad real por línea

**Checkpoint**: `SC-032`, `SC-033`, `SC-034`, `SC-035` verificados.

---

## Phase 5: US 1 — Listar Manifiesto (Spec 12, P1)

**Goal**: El Conductor ve la lista consolidada de pedidos asignados a su vehículo con el peso total de carga.

**Independent Test**: `GET /api/manifiestos/{vehiculoId}` → lista de pedidos asignados con productos, cantidades y peso logístico total.

- [ ] T015 [US1-Spec12] Implementar `ListarManifiestoUseCase.ejecutar(UUID vehiculoId)`:
  - Consultar pedidos en estado EN_PICKING asignados al vehículo
  - Calcular peso total de carga (usando `peso_logistico_kg` del SKU × cantidad)
- [ ] T016 [US1-Spec12] Implementar `GET /api/manifiestos/{vehiculoId}` en `ManifiestoController`
- [ ] T017 [US1-Spec12] Tests de integración
- [ ] T018 [US1-Spec12] Frontend: `ManifiestoPage.jsx` con lista de pedidos y peso total de carga

---

## Phase 6: US 1 — Solicitar Ruta (Spec 13, P1)

**Goal**: El sistema solicita la ruta optimizada al Módulo 2 para un conjunto de pedidos consolidados.

**Independent Test**: `GET /api/rutas/{vehiculoId}` → retorna la ruta calculada por el Módulo 2 con las paradas ordenadas.

- [ ] T019 [US1-Spec13] Implementar `SolicitarRutaUseCase.ejecutar(UUID vehiculoId)`:
  - Delegar la solicitud de ruta al Módulo 2 (endpoint HTTP del Módulo 2)
  - Retornar la ruta optimizada con paradas secuenciadas
- [ ] T020 [US1-Spec13] Implementar `GET /api/rutas/{vehiculoId}` en `ManifiestoController`
- [ ] T021 [US1-Spec13] Tests de integración con mock del Módulo 2
- [ ] T022 [US1-Spec13] Frontend: botón "Ver Ruta" en `ManifiestoPage.jsx`

---

## Phase 7: US 1 — Confirmar Despacho (Spec 14, P1)

**Goal**: El Conductor confirma la salida del vehículo. Los lotes pasan de EN_PICKING → DESPACHADO y el pedido pasa a DESPACHADO.

**Independent Test**: `POST /api/despachos/confirmar` → lotes cambian a DESPACHADO, pedido cambia a DESPACHADO, movimiento registrado.

- [ ] T023 [US1-Spec14] Implementar `ConfirmarDespachoUseCase.ejecutar(UUID pedidoId)`:
  - Cambiar lotes EN_PICKING → DESPACHADO
  - Cambiar estado del pedido a DESPACHADO
  - Registrar `MovimientoInventario` tipo DESPACHO
- [ ] T024 [US1-Spec14] Implementar `POST /api/despachos/confirmar` en `DespachoController`
- [ ] T025 [US1-Spec14] Tests unitarios e integración
- [ ] T026 [US1-Spec14] Frontend: `DespachoPage.jsx` con confirmación final

**Checkpoint**: El ciclo completo Picking → Despacho funciona de extremo a extremo.

---

## Dependencies & Execution Order

- Depende de `plan_pedidos.md` (necesita pedidos en estado COMPROMETIDO)
- T006 (listar pedidos comprometidos) puede desarrollarse en paralelo con T010 (confirmar picking)
- T015 (manifiesto) depende de T010 (picking confirmado)
- T019 (solicitar ruta) depende del Módulo 2 externo
- T023 (confirmar despacho) depende de T010 y T015

## Notes

- El control de concurrencia en `ConfirmarPickingUseCase` es crítico — dos operarios no pueden tomar el mismo lote
- La excepción de faltante durante picking invoca el `ReportarExcepcionUseCase` del plan_recepcion_excepciones.md
- El peso logístico del manifiesto usa `peso_logistico_kg` definido en la entidad `Producto`
