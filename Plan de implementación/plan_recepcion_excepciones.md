# Implementation Plan: Recepción de Mercancía y Excepciones de Inventario

**Date**: 2026-03-27
**Specs**: [`04_registrar_ingreso_productos.md`](../especificaciones/04_registrar_ingreso_productos.md) · [`16_reportar_excepciones_inventario.md`](../especificaciones/16_reportar_excepciones_inventario.md)

---

## Summary

Implementación del flujo de entrada de mercancía al Centro de Distribución. El `Operario de Recepción` registra lotes de productos validando contra el manifiesto de fábrica. La operación es **atómica** (Lote + MovimientoInventario + actualización de stock en una sola transacción). El `Supervisor de Inventario` puede reportar excepciones (averías, vencimientos) que descuentan stock inmediatamente.

---

## Technical Context

**Language/Version**: Java 21
**Framework**: Spring Boot 3.x — Clean Architecture
**Storage**: PostgreSQL — tablas `manifiesto`, `recepcion`, `lote`, `movimiento_inventario`, `excepcion_inventario`
**Testing**: JUnit 5, Mockito (unit), @SpringBootTest (integración)
**Performance Goals**: Operación de recepción atómica — ningún dato parcial persiste ante fallo (FR-021)
**Constraints**:
- Fechas de vencimiento deben ser futuras (FR-014)
- No se permiten códigos de lote duplicados por SKU (FR-018)
- Excepciones no son reversibles directamente (requieren corrección con trazabilidad)

---

## Project Structure (Clean Architecture)

```text
domain/
├── model/
│   ├── Lote.java              # Entidad pura: codigo, skuRef, fechaVenc, cantidad, estado, flagFefo, costo
│   ├── Recepcion.java         # numero (autogenerado), fecha, operarioRef, manifiestoRef
│   ├── MovimientoInventario.java  # tipo, cantidad, loteRef, pedidoRef, fecha
│   └── ExcepcionInventario.java   # tipo, loteRef, cantidadAfectada, descripcion, origenRef
├── port/
│   ├── LoteRepository.java
│   ├── RecepcionRepository.java
│   ├── MovimientoInventarioRepository.java
│   └── ExcepcionInventarioRepository.java
└── exception/
    ├── LoteDuplicadoException.java
    ├── FechaVencimientoInvalidaException.java
    └── CantidadExcepcionInvalidaException.java

application/usecase/
├── RegistrarIngresoUseCase.java        # Spec 04
├── ReportarExcepcionUseCase.java       # Spec 16
└── DetectarLotesVencidosUseCase.java   # Spec 16 — job diario

infrastructure/
├── persistence/
│   ├── LoteJpaEntity.java
│   ├── LoteJpaRepository.java
│   ├── LoteRepositoryAdapter.java
│   ├── RecepcionJpaEntity.java
│   ├── RecepcionJpaRepository.java
│   └── ...
├── web/
│   ├── RecepcionController.java        # POST /api/recepciones
│   ├── ExcepcionController.java        # POST /api/excepciones
│   └── dto/
│       ├── RecepcionRequest.java
│       ├── RecepcionResponse.java
│       ├── ExcepcionRequest.java
│       └── ExcepcionResponse.java
└── scheduler/
    └── VencimientoScheduler.java       # @Scheduled — job diario

prototipo/src/pages/
├── RecepcionPage.jsx
└── ExcepcionesPage.jsx
```

---

## Phase 1: Setup de tablas

- [ ] T001 Crear `V3__create_recepcion_lote_tables.sql`:
  - Tabla `manifiesto`: id, numero, fecha, origen
  - Tabla `recepcion`: numero (PK autogenerado), fecha, operario_ref, manifiesto_ref
  - Tabla `lote`: codigo_lote, sku_ref, fecha_vencimiento, cantidad, estado (ENUM: DISPONIBLE, COMPROMETIDO, EN_PICKING, DESPACHADO, AVERIA, VENCIDO), flag_urgencia_fefo, fecha_expedicion, costo_cop
  - Tabla `movimiento_inventario`: id, tipo (ENUM: ENTRADA, COMPROMISO, PICKING, BAJA_AVERIA, BAJA_VENCIMIENTO, FALTANTE_PICKING), cantidad, lote_ref, recepcion_ref, pedido_ref, fecha
  - Tabla `excepcion_inventario`: id, tipo (ENUM: AVERIA, VENCIMIENTO, DIFERENCIA_INVENTARIO, FALTANTE), lote_ref, cantidad_afectada, descripcion, operario, fecha, origen_ref

---

## Phase 2: Foundational — Modelos y Puertos

- [ ] T002 Crear entidades de dominio puras: `Lote`, `Recepcion`, `MovimientoInventario`, `ExcepcionInventario`
- [ ] T003 Crear ports (interfaces): `LoteRepository`, `RecepcionRepository`, `MovimientoInventarioRepository`, `ExcepcionInventarioRepository`
- [ ] T004 Crear excepciones de dominio: `LoteDuplicadoException`, `FechaVencimientoInvalidaException`, `CantidadExcepcionInvalidaException`
- [ ] T005 Crear entidades JPA y adaptadores de repositorio en `infrastructure/persistence/`
- [ ] T006 Definir DTOs en `infrastructure/web/dto/`

**Checkpoint**: Las tablas existen en BD, las entidades de dominio están definidas y compila el proyecto.

---

## Phase 3: US 1 — Registrar Ingreso de Mercancía (Spec 04, P1)

**Goal**: El Operario registra un lote. El sistema valida contra manifiesto, crea el lote en estado DISPONIBLE, incrementa el stock y registra el movimiento de inventario, todo en una sola transacción atómica.

**Independent Test**: `POST /api/recepciones` con lote válido → lote creado en DISPONIBLE, stock SKU incrementado, MovimientoInventario tipo "Entrada" registrado, responde 201 con número de recepción.

- [ ] T007 [US1-Spec04] Implementar `RegistrarIngresoUseCase.ejecutar()`:
  - Validar fecha vencimiento > fecha actual
  - Validar que código de lote no exista para el mismo SKU
  - Validar cantidad > 0
  - Si cantidad difiere del manifiesto → extender a `ReportarExcepcionUseCase` tipo DIFERENCIA_INVENTARIO
  - Crear `Lote` estado DISPONIBLE
  - Si fecha vencimiento ≤ N días (umbral configurable) → activar `flag_urgencia_fefo = true` y generar alerta
  - Registrar `MovimientoInventario` tipo ENTRADA
  - Generar número de recepción único
  - Operación totalmente atómica (`@Transactional`)
- [ ] T008 [US1-Spec04] Implementar `POST /api/recepciones` en `RecepcionController`
- [ ] T009 [US1-Spec04] Tests unitarios de `RegistrarIngresoUseCase` (positivo, fecha vencida, lote duplicado, cantidad cero)
- [ ] T010 [US1-Spec04] Tests de integración de `POST /api/recepciones`
- [ ] T011 [US1-Spec04] Frontend: `RecepcionPage.jsx` con formulario de ingreso de lote

**Checkpoint**: `SC-007`, `SC-008`, `SC-009`, `SC-010`, `SC-011` verificados.

---

## Phase 4: US 1-3 — Reportar Excepción de Inventario (Spec 16, P1)

**Goal**: El Supervisor (o Operario durante picking) puede reportar averías y vencimientos. El stock se descuenta inmediatamente y el Supervisor recibe notificación.

**Independent Test**: `POST /api/excepciones` tipo AVERIA sobre fracción de lote → stock descontado, MovimientoInventario negativo registrado, notificación generada.

- [ ] T012 [US1-Spec16] Implementar `ReportarExcepcionUseCase.ejecutar()`:
  - Validar: cantidad_excepcion <= stock_actual_lote
  - Descontar stock del lote
  - Si cantidad = total del lote → cambiar estado lote a AVERIA o VENCIDO y bloquear
  - Registrar `MovimientoInventario` negativo (BAJA_AVERIA o BAJA_VENCIMIENTO)
  - Generar notificación al Supervisor (log / evento de aplicación)
- [ ] T013 [US2-Spec16] Implementar excepción de discrepancia en recepción (llamada desde T007)
- [ ] T014 [US3-Spec16] Implementar excepción detectada durante picking (se invocará desde PickingUseCase)
- [ ] T015 [US4-Spec16] Implementar `DetectarLotesVencidosUseCase` con `@Scheduled(cron = "0 0 1 * * ?")` (diariamente a la 1 AM)
- [ ] T016 [US1-Spec16] Implementar `POST /api/excepciones` en `ExcepcionController`
- [ ] T017 [US1-Spec16] Tests unitarios de `ReportarExcepcionUseCase` (avería parcial, avería total, vencimiento, cantidad inválida)
- [ ] T018 [US1-Spec16] Tests de integración de `POST /api/excepciones`
- [ ] T019 [US1-Spec16] Frontend: `ExcepcionesPage.jsx` con formulario de reporte

**Checkpoint**: `SC-013`, `SC-014`, `SC-015`, `SC-016` verificados.

---

## Dependencies & Execution Order

- T001 → T002-T006 (foundational) → T007-T011 (recepción) → T012-T019 (excepciones)
- `ReportarExcepcionUseCase` (T012) es invocado internamente desde `RegistrarIngresoUseCase` (T007) para discrepancias
- `DetectarLotesVencidosUseCase` (T015) puede desarrollarse en paralelo con T012-T014
- El Spec 16 (excepciones durante picking) se integrará completamente en el plan de Confirmar Picking

## Notes

- La atomicidad de la recepción se garantiza con `@Transactional` en el use case
- El umbral crítico de vencimiento (N días) debe ser un parámetro configurable en `application.yml`
- Los lotes en estado AVERIA o VENCIDO nunca deben aparecer en consultas de disponibilidad
