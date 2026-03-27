# Implementation Plan: Módulo 1 — Gestión de Inventario y Abastecimiento

**Date**: 2026-03-27
**Specs**: `especificaciones/01` al `especificaciones/16`
**Equipo**: Módulo 1

---
## Summary

Implementación full-stack del sistema de inventario para una distribuidora de bebidas. El backend expondrá una API REST con Java 21 + Spring Boot y persistirá datos en PostgreSQL. El frontend consumirá esa API usando React + Vite. La comunicación con el Módulo 2 (Logística) y el Módulo 3 (Financiero) se realizará mediante colas asíncronas. El Módulo de Usuarios es un servicio externo ya existente que se consume por HTTP.

---

## Technical Context

**Language/Version Backend**: Java 21
**Framework Backend**: Spring Boot 3.x (Web MVC, Data JPA, Validation)
**Language/Version Frontend**: JavaScript (ES2023)
**Framework Frontend**: React 19 + Vite 8
**Storage**: PostgreSQL
**Testing Backend**: JUnit 5, Mockito, Spring Boot Test
**Testing Frontend**: Jest + React Testing Library
**Messaging (inter-módulos)**: Cola asíncrona (RabbitMQ — a confirmar tecnología exacta con el equipo)
**External Services**: Módulo de Usuarios (consulta por CC/NIT, HTTP REST)
**Target Platform**: Server-side (Spring Boot) + SPA (React)
**Performance Goals**:
- Confirmación de pedido ≤ 5 seg (SC-029)
- Carga de catálogo ≤ 3 seg para 500 productos (SC-025)
- Consulta al módulo de usuarios ≤ 2 seg (SC-021)
- Confirmación de picking ≤ 5 min (SC-035)
**Constraints**: Operación de recepción de mercancía debe ser **atómica** (FR-021). Stock comprometido NO cuenta como disponible.

---

## Project Structure

### Arquitectura: Clean Architecture

```
┌───────────────────────────────────────┐
│            infrastructure             │  ← Controllers REST, JPA, Colas
│  ┌─────────────────────────────────┐  │
│  │         application             │  │  ← Use Cases (lógica de aplicación)
│  │  ┌───────────────────────────┐  │  │
│  │  │         domain            │  │  │  ← Entities, Ports (interfaces), Excepciones
│  │  └───────────────────────────┘  │  │
│  └─────────────────────────────────┘  │
└───────────────────────────────────────┘
```

```text
20261StoreManagement/
├── especificaciones/          # 16 specs del módulo (ya existentes)
├── Plan de implementación/    # Este archivo + planes por feature
├── actores/                   # Documentación de actores
│
├── backend/                   # [NEW] API REST — Spring Boot (Clean Architecture)
│   ├── src/main/java/com/distribuidora/modulo1/
│   │   ├── domain/
│   │   │   ├── model/             # Entidades puras del negocio (sin anotaciones JPA)
│   │   │   ├── port/              # Interfaces de repositorio (puertos de salida)
│   │   │   └── exception/         # Excepciones del dominio
│   │   ├── application/
│   │   │   └── usecase/           # Casos de uso (lógica de aplicación)
│   │   └── infrastructure/
│   │       ├── persistence/       # Entidades JPA + implementaciones de repositorios
│   │       ├── web/               # Controllers REST + DTOs (Request/Response)
│   │       ├── messaging/         # Productores y consumidores de cola asíncrona
│   │       ├── external/          # Adaptadores a servicios externos (Módulo Usuarios)
│   │       └── config/            # Beans de Spring (CORS, seguridad, mensajería)
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/          # Scripts SQL (Flyway)
│   └── src/test/java/com/distribuidora/modulo1/
│       ├── application/usecase/   # Tests unitarios de casos de uso (sin Spring)
│       └── infrastructure/web/    # Tests de integración de controllers
│
└── prototipo/                     # [EXISTING → EVOLUCIONA A FRONTEND]
    └── src/
        ├── components/            # Componentes reutilizables por actor
        ├── pages/                 # Páginas por feature (una por spec)
        ├── services/              # Clientes HTTP hacia la API backend
        └── App.jsx
```

**Structure Decision**: Clean Architecture con tres capas concéntricas. La capa `domain` no tiene dependencias externas (ni Spring, ni JPA). Los `use cases` en `application` dependen solo del `domain`. La capa `infrastructure` depende de las dos internas y contiene toda la tecnología (Spring, JPA, RabbitMQ). El frontend React en `/prototipo` evoluciona el prototipo existente.

---

## Entidades del Dominio (Base para la BD)

| Entidad | Spec origen | Campos clave |
|---|---|---|
| `Producto` | 01, 02 | sku_id (auto), marca, presentacion, contenido_ml, peso_logistico_kg |
| `BitacoraProducto` | 02 | sku_id_ref, campo, valor_anterior, valor_nuevo, descripcion, fecha |
| `Manifiesto` | 04 | numero, fecha, origen (planta) |
| `Recepcion` | 04 | numero, fecha, operario_ref, manifiesto_ref |
| `Lote` | 04 | codigo_lote, sku_ref, fecha_vencimiento, cantidad, estado, flag_urgencia_fefo, costo_cop |
| `MovimientoInventario` | 04, 11, 16 | tipo, cantidad, lote_ref, pedido_ref, fecha |
| `Pedido` | 08 | numero, cliente_cc, fecha, estado, ruta_ref |
| `ProductoPedido` | 08 | pedido_ref, sku_ref, cantidad_solicitada, cantidad_confirmada |
| `LoteComprometido` | 08 | producto_pedido_ref, lote_ref, cantidad |
| `RegistroPicking` | 11 | pedido_ref, lote_ref, cantidad_esperada, cantidad_real, operario, fecha |
| `ExcepcionInventario` | 16 | tipo, lote_ref, cantidad_afectada, descripcion, operario, fecha, origen_ref |

---

## Phase 1: Setup — Infraestructura Compartida

**Purpose**: Configurar el proyecto desde cero para que el equipo pueda desarrollar.

- [ ] T001 Crear proyecto Spring Boot con dependencias: `web`, `data-jpa`, `validation`, `postgresql`, configurar `pom.xml`/`build.gradle`
- [ ] T002 Configurar `application.yml`: conexión a PostgreSQL (dev + test)
- [ ] T003 Integrar Flyway para migraciones de base de datos
- [ ] T004 Configurar CORS para permitir llamadas desde el frontend React
- [ ] T005 Implementar handler global de excepciones (`@RestControllerAdvice`) con estructura de error estándar
- [ ] T006 Crear script de migración base `V1__init_schema.sql` con todas las tablas del dominio
- [ ] T007 Configurar dependencia de mensajería asíncrona (RabbitMQ client en Spring) para inter-módulos
- [ ] T008 Estructurar carpetas del frontend (pages/, components/, services/) en `/prototipo/src`

**Checkpoint**: El proyecto compila, la BD se levanta con el schema y el handler de errores responde JSON estándar.

---

## Phase 2: Foundational — Datos Maestros de Producto (Specs 01, 02, 03)

**Purpose**: El `Producto` (SKU) es la base de todo. Sin él, no hay lotes, ni pedidos, ni inventario.

### Feature Plan individual → `plan_crear_plantilla_producto.md`

- [ ] T009 Crear entidad `Producto` + `BitacoraProducto` en `model/`
- [ ] T010 Crear `ProductoRepository` + `BitacoraProductoRepository`
- [ ] T011 Definir DTOs: `ProductoRequest`, `ProductoResponse`, `BitacoraResponse`
- [ ] T012 [US1-Spec01] Implementar `ProductoService.crearProducto()` con validación de duplicados y autogeneración de SKU
- [ ] T013 [US1-Spec01] Implementar `POST /api/productos` en `ProductoController`
- [ ] T014 [US1-Spec02] Implementar `ProductoService.modificarProducto()` con bitácora y alerta de peso logístico
- [ ] T015 [US1-Spec02] Implementar `PUT /api/productos/{skuId}` y `DELETE /api/productos/{skuId}`
- [ ] T016 [US1-Spec03] Implementar `GET /api/productos` (catálogo con disponibilidad para Asesor Comercial)
- [ ] T017 Tests unitarios e integración para specs 01, 02, 03
- [ ] T018 Frontend: Páginas de Catálogo, Crear SKU y Editar SKU conectadas a la API

**Checkpoint**: El Supervisor puede crear/editar SKUs. El Asesor ve el catálogo en tiempo real.

---

## Phase 3: Recepción de Mercancía (Spec 04 + 16)

**Purpose**: Punto de entrada del inventario físico. Sin esta fase, no hay stock.

### Feature Plan individual → `plan_registrar_ingreso.md`

- [ ] T019 [Spec04] Crear entidades `Recepcion`, `Lote`, `MovimientoInventario`, `Manifiesto`
- [ ] T020 [Spec04] Implementar `RecepcionService.registrarIngreso()` (lógica atómica: lote + movimiento + stock)
- [ ] T021 [Spec04] Implementar `POST /api/recepciones`
- [ ] T022 [Spec16] Implementar `ExcepcionInventario` + `ExcepcionService` (Avería, Vencimiento, Diferencia)
- [ ] T023 [Spec16] Implementar `POST /api/excepciones` con descuento automático de stock
- [ ] T024 [Spec16] Implementar job automático diario de detección de lotes vencidos (`@Scheduled`)
- [ ] T025 Tests unitarios e integración para specs 04 y 16
- [ ] T026 Frontend: Formulario de recepción y pantalla de reporte de excepciones

**Checkpoint**: El Operario de Recepción puede registrar lotes. El Supervisor puede reportar averías y vencimientos.

---

## Phase 4: Consulta de Inventario (Specs 05, 06)

**Purpose**: Visibilidad del stock en tiempo real. Prerequisito para crear pedidos.

### Feature Plan individual → `plan_consultar_inventario.md`

- [ ] T027 [Spec05] Implementar `GET /api/inventario/{skuId}` con lotes ordenados por FEFO
- [ ] T028 [Spec05] Lógica de resaltado de lotes en umbral crítico (campo configurable en `application.yml`)
- [ ] T029 [Spec06] Implementar `GET /api/disponibilidad?skuId={id}&cantidad={n}` (verificación para pedido)
- [ ] T030 Tests para specs 05 y 06
- [ ] T031 Frontend: Pantalla de inventario con tabla FEFO + indicadores visuales

**Checkpoint**: El Supervisor ve el inventario completo por FEFO. La verificación de disponibilidad funciona.

---

## Phase 5: Gestión de Pedidos (Specs 07, 08, 09, 10)

**Purpose**: Núcleo comercial del módulo. Depende de disponibilidad (Phase 4).

### Feature Plan individual → `plan_realizar_pedido.md`

- [ ] T032 [Spec07] Implementar `GET /api/clientes/{cc}` como proxy al Módulo de Usuarios externo
- [ ] T033 [Spec08] Crear entidades `Pedido`, `ProductoPedido`, `LoteComprometido`
- [ ] T034 [Spec08] Implementar `PedidoService.crearPedido()` → estado "Esperando Ruta" sin comprometer lotes
- [ ] T035 [Spec08] Implementar `POST /api/pedidos`
- [ ] T036 [Spec08] Implementar endpoint/listener que recibe señal del Módulo 2 y ejecuta compromiso FEFO de lotes
- [ ] T037 [Spec09] Implementar `GET /api/pedidos/{numeroPedido}` (detalle completo)
- [ ] T038 [Spec10] Implementar `GET /api/pedidos/comprometidos` (lista para picking)
- [ ] T039 Tests para specs 07, 08, 09, 10
- [ ] T040 Frontend: Pantalla de crear pedido, consulta de cliente y detalle de pedido

**Checkpoint**: El Asesor crea pedidos. Los lotes se comprometen cuando llega la señal del Módulo 2.

---

## Phase 6: Despacho y Logística (Specs 11, 12, 13, 14)

**Purpose**: Flujo de salida física del inventario.

### Feature Plan individual → `plan_confirmar_picking.md`

- [ ] T041 [Spec11] Crear entidad `RegistroPicking`
- [ ] T042 [Spec11] Implementar `PickingService.confirmarPicking()` → "Comprometido" → "En Picking"
- [ ] T043 [Spec11] Implementar `POST /api/picking/confirmar` con lógica de faltante parcial
- [ ] T044 [Spec12] Implementar `GET /api/manifiestos/{vehiculo}` (lista de carga consolidada)
- [ ] T045 [Spec13] Implementar `GET /api/rutas/{pedidoId}` (proxy/integración con Módulo 2)
- [ ] T046 [Spec14] Implementar `POST /api/despachos/confirmar` (registro de salida del vehículo)
- [ ] T047 Tests para specs 11, 12, 13, 14
- [ ] T048 Frontend: Pantalla de picking, manifiesto de carga y confirmación de despacho

**Checkpoint**: El Operario confirma picking. El Conductor tiene manifiesto y puede confirmar despacho.

---

## Phase 7: Integración Inter-Módulos (Specs 08-señal, 15)

**Purpose**: Comunicación asíncrona con Módulo 2 y Módulo 3.

- [ ] T049 [Spec08] Implementar **consumidor de cola** que recibe `{ruta, fecha_recogida}` del Módulo 2 y activa compromiso de inventario
- [ ] T050 [Spec15] Implementar **productor de cola** que publica datos del pedido (`id_pedido, id_cliente, precio_total, dirección_entrega`) al Módulo 3 al crear un pedido
- [ ] T051 Tests de integración para los flujos de mensajería
- [ ] T052 Documentar contratos de mensajes (formato JSON de los mensajes de cola)

**Checkpoint**: Los mensajes fluyen correctamente entre los módulos.

---

## Phase 8: Polish & Cross-Cutting

- [ ] T053 Paginación en todos los listados (`/pedidos/comprometidos`, catálogo)
- [ ] T054 Logging estructurado en todas las operaciones críticas (recepción, pedido, picking)
- [ ] T055 Variables de entorno externalizadas (no hardcoded en `application.yml`)
- [ ] T056 Documentación de la API con Swagger / OpenAPI 3
- [ ] T057 Revisión de métricas de performance (tiempos de respuesta según Success Criteria)

---

## Dependencies & Execution Order

```
Phase 1 (Setup)
    └── Phase 2 (Datos Maestros - SKU) ← BLOQUEANTE para todo
            └── Phase 3 (Recepción de Mercancía) ← BLOQUEANTE para inventario
                    ├── Phase 4 (Consulta de Inventario) ← BLOQUEANTE para pedidos
                    │       └── Phase 5 (Gestión de Pedidos)
                    │               └── Phase 6 (Despacho y Picking)
                    │                       └── Phase 7 (Integración Inter-Módulos)
                    │                               └── Phase 8 (Polish)
                    └── Phase 3 continúa con Excepciones (Spec 16)
```

### Spec Dependencies críticas
- **Spec 01** es prerequisito de todo lo demás (sin SKU no hay nada)
- **Spec 04** es prerequisito de Specs 05, 06, 08, 10, 11, 16
- **Spec 06 (disponibilidad)** es prerequisito de Spec 08 (pedido) — se llama internamente
- **Spec 08 (pedido)** es prerequisito de Specs 09, 10, 11, 14, 15

---

## Notes

- Cada fase tiene su propio **Feature Plan** individual (siguiendo el formato de `customer-crud-plan.md`)
- Usar `[US1-Spec01]` etc. para trazar cada tarea a su spec y user story
- Nunca combinar lógica de dos specs en un mismo service method
- Hacer **commit por tarea** completada
- Cada fase debe ser verificable de forma independiente antes de avanzar a la siguiente
