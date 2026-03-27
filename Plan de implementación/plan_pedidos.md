# Implementation Plan: Gestión de Pedidos

**Date**: 2026-03-27
**Specs**: [`07_consultar_datos_cliente.md`](../especificaciones/07_consultar_datos_cliente.md) · [`08_realizar_pedido.md`](../especificaciones/08_realizar_pedido.md) · [`09_consultar_detalle_pedido.md`](../especificaciones/09_consultar_detalle_pedido.md)

---

## Summary

Núcleo comercial del módulo. El `Asesor Comercial` consulta datos de un cliente (consumiendo el Módulo de Usuarios externo), luego crea un pedido que queda en estado **"Esperando Ruta"** sin comprometer lotes. Cuando el `Módulo 2 (Logística)` envía la señal de ruta asignada, el sistema re-verifica disponibilidad y compromete los lotes siguiendo FEFO. Adicionalmente, al crear el pedido se publica un mensaje asíncrono al `Módulo 3 (Financiero)`.

---

## Technical Context

**Language/Version**: Java 21
**Framework**: Spring Boot 3.x — Clean Architecture
**Storage**: PostgreSQL — tablas `pedido`, `producto_pedido`, `lote_comprometido`
**External Services**:
- **Módulo de Usuarios** (HTTP REST — servicio ya existe, solo se consume)
- **Módulo 2 — Logística** (consumidor de cola asíncrona: señal de ruta)
- **Módulo 3 — Financiero** (productor de cola asíncrona: datos del pedido)
**Testing**: JUnit 5, Mockito (unit), @SpringBootTest + WireMock (integración con externo)
**Constraints**:
- Los lotes NO se comprometen al crear el pedido, solo al recibir la señal del Módulo 2 (FR-057)
- Si hay stock insuficiente al crear el pedido → pedido rechazado (FR-055)
- Si hay stock insuficiente al comprometer (ruta asignada) → se compromete lo disponible y se notifica (SC-026)
- El pedido necesita un cliente activo y válido (FR-052)

---

## Project Structure (Clean Architecture)

```text
domain/
├── model/
│   ├── Pedido.java             # numero (autogenerado), clienteCc, fecha, estado (ENUM)
│   │                           # estados: ESPERANDO_RUTA | COMPROMETIDO | EN_PICKING | DESPACHADO
│   ├── ProductoPedido.java     # pedidoRef, skuRef, cantidadSolicitada, cantidadConfirmada
│   └── LoteComprometido.java   # productoPedidoRef, loteRef, cantidad
├── port/
│   ├── PedidoRepository.java
│   ├── ProductoPedidoRepository.java
│   └── ClienteServicePort.java   # Puerto para consulta al Módulo de Usuarios externo
└── exception/
    ├── ClienteNoEncontradoException.java
    ├── ClienteInactivoException.java
    └── StockInsuficienteException.java

application/usecase/
├── ConsultarClienteUseCase.java          # Spec 07
├── RealizarPedidoUseCase.java            # Spec 08 — crea el pedido
├── ComprometerInventarioUseCase.java     # Spec 08 — activado por señal del Módulo 2
└── ConsultarDetallePedidoUseCase.java    # Spec 09

infrastructure/
├── persistence/
│   ├── PedidoJpaEntity.java
│   ├── PedidoJpaRepository.java
│   └── PedidoRepositoryAdapter.java
├── external/
│   └── ModuloUsuariosAdapter.java    # Implementa ClienteServicePort — llama HTTP al módulo externo
├── messaging/
│   ├── RutaAsignadaConsumer.java     # Escucha cola del Módulo 2 → activa ComprometerInventarioUseCase
│   └── PedidoCreadoProducer.java     # Publica datos del pedido a cola del Módulo 3
└── web/
    ├── ClienteController.java        # GET /api/clientes/{cc}
    ├── PedidoController.java         # POST /api/pedidos · GET /api/pedidos/{numero}
    └── dto/
        ├── ClienteResponse.java
        ├── PedidoRequest.java
        ├── PedidoResponse.java
        └── DetallePedidoResponse.java

prototipo/src/pages/
├── ConsultarClientePage.jsx
├── RealizarPedidoPage.jsx
└── DetallePedidoPage.jsx
```

---

## Phase 1: Setup de tablas

- [ ] T001 Crear `V4__create_pedido_tables.sql`:
  - Tabla `pedido`: numero (PK UUID), cliente_cc (FK lógica), fecha, estado (ENUM), ruta_ref
  - Tabla `producto_pedido`: id, pedido_ref, sku_ref, cantidad_solicitada, cantidad_confirmada
  - Tabla `lote_comprometido`: id, producto_pedido_ref, lote_ref, cantidad

---

## Phase 2: Foundational — Modelos y Puertos

- [ ] T002 Crear entidades de dominio: `Pedido`, `ProductoPedido`, `LoteComprometido`
- [ ] T003 Crear ports: `PedidoRepository`, `ProductoPedidoRepository`, `ClienteServicePort`
- [ ] T004 Crear excepciones de dominio: `ClienteNoEncontradoException`, `ClienteInactivoException`, `StockInsuficienteException`
- [ ] T005 Implementar `ModuloUsuariosAdapter` en `infrastructure/external/` que realiza `GET` HTTP al Módulo de Usuarios externo
- [ ] T006 Crear DTOs de request y response
- [ ] T007 Implementar entidades JPA y adaptadores de repositorio

**Checkpoint**: El adaptador del Módulo de Usuarios funciona (se puede mockear en tests).

---

## Phase 3: US 1 — Consultar Datos de Cliente (Spec 07, P1)

**Goal**: El Asesor puede buscar un cliente por CC/NIT para vincularlo al pedido.

**Independent Test**: `GET /api/clientes/{cc}` con CC existente → retorna nombre, estado, contacto. Con CC inexistente → 404.

- [ ] T008 [US1-Spec07] Implementar `ConsultarClienteUseCase.ejecutar(String cc)`:
  - Llamar al `ClienteServicePort` (que delega al Módulo de Usuarios externo)
  - Si no existe → lanzar `ClienteNoEncontradoException`
  - Si existe pero inactivo → retornar datos con indicador `estado: INACTIVO`
- [ ] T009 [US1-Spec07] Implementar `GET /api/clientes/{cc}` en `ClienteController`
- [ ] T010 [US1-Spec07] Tests unitarios con Mockito del `ConsultarClienteUseCase` (encontrado, no encontrado, inactivo)
- [ ] T011 [US1-Spec07] Tests de integración con WireMock simulando el Módulo de Usuarios externo

**Checkpoint**: `SC-019`, `SC-020`, `SC-021` verificados.

---

## Phase 4: US 1 — Realizar Pedido (Spec 08, P1)

**Goal**: El Asesor crea el pedido. El sistema verifica disponibilidad, genera número único y lo deja en "Esperando Ruta" sin tocar los lotes.

**Independent Test**: `POST /api/pedidos` con stock suficiente → pedido creado en ESPERANDO_RUTA, lotes NO comprometidos, stock disponible no cambia, 201 con número de pedido.

- [ ] T012 [US1-Spec08] Implementar `RealizarPedidoUseCase.ejecutar(PedidoRequest)`:
  - Verificar que cliente existe y está activo (delega a `ConsultarClienteUseCase`)
  - Verificar disponibilidad de cada SKU (delega a `ConsultarDisponibilidadUseCase` del plan_consulta_inventario)
  - Si stock insuficiente para algún SKU → informar, permitir confirmar solo con disponibles
  - Crear `Pedido` estado ESPERANDO_RUTA — sin comprometer lotes
  - Publicar evento a cola del Módulo 3 via `PedidoCreadoProducer`
- [ ] T013 [US1-Spec08] Implementar `POST /api/pedidos` en `PedidoController`
- [ ] T014 [US2-Spec08] Implementar `ComprometerInventarioUseCase.ejecutar(UUID pedidoId, String rutaRef)`:
  - Re-verificar disponibilidad en el momento del compromiso
  - Seleccionar lotes FEFO para cada SKU
  - Crear registros `LoteComprometido`
  - Cambiar estado del `Pedido` a COMPROMETIDO
  - Registrar `MovimientoInventario` tipo COMPROMISO por cada lote
- [ ] T015 [US1-Spec08] Tests unitarios de `RealizarPedidoUseCase` (exitoso, sin cliente, stock insuficiente al crear)
- [ ] T016 [US2-Spec08] Tests unitarios de `ComprometerInventarioUseCase` (exitoso, stock insuficiente al comprometer, FEFO correcto)
- [ ] T017 [US1+2-Spec08] Tests de integración de `POST /api/pedidos`
- [ ] T018 [US1-Spec08] Frontend: `RealizarPedidoPage.jsx` con búsqueda de cliente y selección de productos

**Checkpoint**: `SC-025`, `SC-026`, `SC-027`, `SC-028`, `SC-029`, `SC-030` verificados.

---

## Phase 5: US 1 — Consultar Detalle de Pedido (Spec 09, P2)

**Goal**: El Asesor o Supervisor puede consultar el estado y detalle completo de un pedido.

**Independent Test**: `GET /api/pedidos/{numero}` → retorna estado actual, productos, cantidades y lotes comprometidos.

- [ ] T019 [US1-Spec09] Implementar `ConsultarDetallePedidoUseCase.ejecutar(UUID numeroPedido)`
- [ ] T020 [US1-Spec09] Implementar `GET /api/pedidos/{numero}` en `PedidoController`
- [ ] T021 [US1-Spec09] Tests de integración de `GET /api/pedidos/{numero}` (encontrado y no encontrado)
- [ ] T022 [US1-Spec09] Frontend: `DetallePedidoPage.jsx` con líneas de pedido y estado actual

**Checkpoint**: Asesor puede ver el estado de cualquier pedido en tiempo real.

---

## Dependencies & Execution Order

- Este plan depende de: `plan_recepcion_excepciones.md` (lotes) y `plan_consulta_inventario.md` (disponibilidad)
- T008 (Spec 07) puede desarrollarse en paralelo con T001-T007
- T014 (`ComprometerInventarioUseCase`) depende de T012 (`RealizarPedidoUseCase`)
- T019 (Spec 09) puede desarrollarse en paralelo con T014
- `PedidoCreadoProducer` y `RutaAsignadaConsumer` se integran con el Módulo 3 y 2 respectivamente — ver `plan_integracion.md`

## Notes

- NUNCA comprometer lotes al crear el pedido. Este es el invariante de negocio más importante del módulo
- El `ModuloUsuariosAdapter` debe manejar timeout y devolver error de conectividad claro (FR-046)
- La selección FEFO de lotes en `ComprometerInventarioUseCase` debe ignorar lotes en estado AVERIA y VENCIDO
