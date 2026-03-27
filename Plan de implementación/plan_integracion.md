# Implementation Plan: Integración Inter-Módulos (Mensajería Asíncrona)

**Date**: 2026-03-27
**Spec**: [`15_ofrecer_datos_pedido.md`](../especificaciones/15_ofrecer_datos_pedido.md) · Señal de ruta del Módulo 2 (referenciada en Spec 08)

---

## Summary

Implementación de la comunicación asíncrona entre el Módulo 1 y los otros sistemas:

1. **Productor → Módulo 3 (Financiero)**: Cada vez que se crea un pedido, el Módulo 1 publica sus datos (`id_pedido`, `id_cliente`, `precio_total`, `dirección_entrega`) en una cola. El Módulo 3 los consume para su liquidación.
2. **Consumidor ← Módulo 2 (Logística)**: Cuando el Módulo 2 asigna ruta a un conjunto de pedidos, envía un mensaje a una cola. El Módulo 1 lo consume y activa el compromiso de inventario (FEFO).

La cola asíncrona garantiza que el 0% de pedidos creados queden sin enviarse a finanzas (SC-023) y que la integración sea resiliente (reintento ante fallo de entrega).

---

## Technical Context

**Language/Version**: Java 21
**Framework**: Spring Boot 3.x — Clean Architecture
**Messaging**: Cola asíncrona (RabbitMQ con Spring AMQP)
**Testing**: JUnit 5, Mockito, Spring AMQP Test
**Constraints**:
- El envío al Módulo 3 debe ocurrir inmediatamente después de crear el pedido (FR-048)
- Si el Módulo 3 no confirma recepción → el sistema reintenta el envío (FR-051, FR-049)
- El Módulo 1 solo envía los datos, no espera respuesta síncrona
- El consumidor del Módulo 2 activa `ComprometerInventarioUseCase` (definido en plan_pedidos.md)

---

## Project Structure (Clean Architecture)

```text
domain/
└── port/
    ├── PedidoEventPublisherPort.java   # Puerto de salida para publicar evento de pedido
    └── RutaAsignadaListenerPort.java   # Puerto de entrada para consumir señal de ruta

infrastructure/
├── messaging/
│   ├── config/
│   │   └── RabbitMQConfig.java         # Definición de queues, exchanges, bindings
│   ├── PedidoCreadoProducer.java       # Implementa PedidoEventPublisherPort — publica a Módulo 3
│   ├── RutaAsignadaConsumer.java       # Escucha cola del Módulo 2 → activa ComprometerInventario
│   └── dto/
│       ├── PedidoCreadoMessage.java    # id_pedido, id_cliente, precio_total, direccion_entrega
│       └── RutaAsignadaMessage.java    # pedidoIds[], rutaRef, fechaRecogida
└── web/
    └── (No hay endpoint REST propio — la integración es puramente por mensajes)

# Contratos de mensajes (documentación de la cola):
docs/
└── contratos_mensajes.md               # Formato JSON de cada mensaje de cola
```

---

## Phase 1: Configuración de RabbitMQ

- [ ] T001 Añadir dependencia `spring-boot-starter-amqp` al `build.gradle`/`pom.xml`
- [ ] T002 Crear `RabbitMQConfig.java` en `infrastructure/messaging/config/`:
  - Declarar `Queue`: `modulo1.pedido_creado` (hacia Módulo 3)
  - Declarar `Queue`: `modulo2.ruta_asignada` (desde Módulo 2)
  - Declarar `DirectExchange` y `Binding` para cada cola
- [ ] T003 Configurar en `application.yml`: host, port, usuario, contraseña de RabbitMQ

**Checkpoint**: Spring Boot conecta con RabbitMQ al arrancar.

---

## Phase 2: Foundational — Puertos de Mensajería

- [ ] T004 Crear port `PedidoEventPublisherPort.java` con método `publicarPedidoCreado(PedidoCreadoMessage)`
- [ ] T005 Definir DTOs de mensajes: `PedidoCreadoMessage.java`, `RutaAsignadaMessage.java`
- [ ] T006 Documentar `docs/contratos_mensajes.md` con el esquema JSON exacto de cada mensaje (contrato con el equipo de Módulo 2 y Módulo 3)

---

## Phase 3: US 1 — Productor: Ofrecer Datos del Pedido al Módulo 3 (Spec 15, P1)

**Goal**: Al crear un pedido exitosamente, el Módulo 1 publica automáticamente los datos en la cola `modulo1.pedido_creado`. Si el Módulo 3 no confirma, el sistema reintenta.

**Independent Test**: Crear un pedido → verificar que un mensaje `PedidoCreadoMessage` fue publicado en la cola con `id_pedido`, `id_cliente`, `precio_total` y `direccion_entrega` correctos.

- [ ] T007 [US1-Spec15] Implementar `PedidoCreadoProducer.java` (implementa `PedidoEventPublisherPort`):
  - Serializar el pedido a `PedidoCreadoMessage`
  - Publicar en exchange → cola `modulo1.pedido_creado` usando `RabbitTemplate`
  - Configurar confirmaciones del publisher (publisher confirms) para reintentar ante fallo
- [ ] T008 [US1-Spec15] Inyectar `PedidoEventPublisherPort` en `RealizarPedidoUseCase` (plan_pedidos.md T012) y llamarlo al final de la creación exitosa del pedido
- [ ] T009 [US1-Spec15] Tests unitarios de `PedidoCreadoProducer` con Mockito (verificar que se publica el mensaje)
- [ ] T010 [US1-Spec15] Test de integración: crear pedido end-to-end y verificar mensaje en cola (usar `RabbitTemplate.receive()` en el test)

**Checkpoint**: `SC-023` verificado — 0% de pedidos sin publicar al Módulo 3.

---

## Phase 4: US — Consumidor: Recibir Señal de Ruta del Módulo 2 (Spec 08 — integración)

**Goal**: Al recibir el mensaje del Módulo 2 con la ruta asignada, el Módulo 1 activa el compromiso de inventario del pedido.

**Independent Test**: Publicar manualmente un `RutaAsignadaMessage` en la cola → verificar que el pedido pasa a estado COMPROMETIDO y los lotes quedan reservados por FEFO.

- [ ] T011 Implementar `RutaAsignadaConsumer.java` con `@RabbitListener`:
  - Deserializar `RutaAsignadaMessage`
  - Invocar `ComprometerInventarioUseCase.ejecutar(pedidoId, rutaRef)` (plan_pedidos.md T014)
  - Manejar excepciones: si falla el compromiso → enviar a dead-letter queue para revisión manual
- [ ] T012 Tests de integración del consumidor (publicar mensaje en cola de test → verificar estado del pedido)

**Checkpoint**: El pedido pasa automáticamente a COMPROMETIDO al recibir la señal del Módulo 2.

---

## Dependencies & Execution Order

- Este plan depende de `plan_pedidos.md` (necesita `RealizarPedidoUseCase` y `ComprometerInventarioUseCase`)
- T001-T003 (config de RabbitMQ) pueden hacerse en paralelo con Phases anteriores del proyecto
- T007-T010 (productor) depende de T008 en `plan_pedidos.md` (crear pedido)
- T011-T012 (consumidor) depende de T014 en `plan_pedidos.md` (comprometer inventario)

## Notes

- Los contratos de mensajes (`contratos_mensajes.md`) deben acordarse con los equipos del Módulo 2 y Módulo 3 antes de implementar
- Usar `ObjectMapper` (Jackson) para serialización/deserialización JSON
- Configurar una Dead Letter Queue (DLQ) para mensajes que fallen repetidamente
- El reintento ante fallo de entrega se maneja con configuración de `retry` en Spring AMQP, no con lógica manual
