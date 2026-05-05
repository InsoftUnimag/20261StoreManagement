# Plan de Implementación: Integración con Módulos Externos - Backend

**Date**: 2026-04-03  
**Spec**: 13_solicitar_ruta.md, 15_ofrecer_datos_pedido.md  
**Module**: Módulo 1 - Inventario  
**Layer**: Backend - Infrastructure  
**Priority**: P1 (Crítico para comunicación inter-módulos)

---

## Summary

Implementación de la capa de integración del Módulo 1 con sistemas externos: (1) Módulo de Usuarios vía HTTP REST (consulta de clientes), (2) Módulo 2 Logística vía RabbitMQ Producer (solicitar ruta), (3) Módulo 2 Logística vía RabbitMQ Consumer (señal de ruta asignada), y (4) Módulo 3 Financiero vía RabbitMQ Producer (evento pedido creado). Se sigue el patrón de evento ligero (`pedido_id`), obligando a los módulos externos a consultar el detalle completo por REST según el `document_Api.yml`. Incluye manejo de errores, reintentos, circuit breakers y monitoreo.

---

## Arquitectura y Estructura de Archivos

Al aplicar **Clean Architecture**, se tienen tres capas concéntricas:

```
┌───────────────────────────────────────┐
│         infrastructure                │  ← Controllers REST, JPA, Colas, Config
│  ┌─────────────────────────────────┐  │
│  │         application             │  │  ← Use Cases (lógica de aplicación)
│  │  ┌───────────────────────────┐  │  │
│  │  │         domain            │  │  │  ← Entities, Ports, Excepciones
│  │  └───────────────────────────┘  │  │
│  └─────────────────────────────────┘  │
└───────────────────────────────────────┘
```

La estructura específica para este componente implementada es:

```
├── domain/                      # Entidades, puertos (interfaces), excepciones
│   ├── repository/
│   │   └── ClienteServicePort.java          # Puerto definido para el módulo cliente
│   └── exception/
│       └── ExternalServiceException.java    # Fallos externos (integración)
├── application/                 # Casos de uso (@Service)
│   └── usecase/                 # Consumidos o alimentados por esta capa
│       └── ...
└── infrastructure/
    ├── web/                     # Controles internos de red
    │   ├── controller/
    │   │   └── ExternalPedidosController.java # Endpoints dedicados
    │   └── dto/
    │       ├── ClienteExternalDTO.java      # DTO para módulos externos
    │       ├── LineaExternalDTO.java
    │       └── PedidoExternalDTO.java
    ├── messaging/               # RabbitMQ (Producers/Consumers)
    │   ├── config/
    │   │   └── RabbitMQConfig.java          # Exchange, Queue, Routing Key
    │   ├── consumer/
    │   │   └── RutaAsignadaConsumer.java    # Escucha colas y llama Use Case
    │   └── producer/
    │       ├── PedidoCreadoProducer.java    # Emisión asíncrona hacia módulo 3
    │       └── SolicitudRutaProducer.java   # Emisión hacia módulo 2
    ├── external/                # Integraciones HTTP
    │   └── ModuloUsuariosAdapter.java       # Call HTTP al módulo de usuarios
    └── observability/           # Salud del sistema
        ├── ExternalServicesHealthIndicator.java # Verifica que RabbitMQ/HTTP viva
        └── IntegrationLogger.java
```

---

## Dependencies

**Blocked by**:
- Setup (Phase 1 del plan maestro): Infraestructura de RabbitMQ, HTTP client configurado
- Plan Pedidos Backend (requiere use cases ComprometerInventarioUseCase)

**Blocks**:
- Ninguno (es soporte transversal para otros planes)

**External Dependencies**:
- **Módulo de Usuarios**: HTTP REST API (puerto 8081, ruta: `/api/usuarios/clientes/{cedula}`)
- **Módulo 2 Logística**: RabbitMQ Exchange `inventario.pedidos`, Routing Key `ruta.solicitar`. Y Exchange `logistica.eventos`, Queue `inventario.ruta-asignada`.
- **Módulo 3 Financiero**: RabbitMQ Exchange `inventario.pedidos`, Routing Key `pedido.creado`

---

## Integration Points

### 1. Módulo de Usuarios (HTTP REST Consumer)

**Purpose**: Consultar datos de clientes para validación en pedidos

**Protocol**: HTTP REST (síncrono)

**Endpoint**: `GET /api/usuarios/clientes/{cedula}`

**Request**:
```
GET http://localhost:8081/api/usuarios/clientes/1234567890
Headers:
  Content-Type: application/json
  Accept: application/json
```

**Response 200 OK**:
```json
{
  "cedula": "1234567890",
  "nombre": "Juan Pérez",
  "telefono": "+57 300 123 4567",
  "email": "juan@example.com",
  "direccion": "Calle 123 #45-67, Bogotá",
  "activo": true
}
```

**Response 404 Not Found**: Cliente no existe

**Resilience**:
- **Timeout**: 5 seg (configurable)
- **Retry**: 1 reintento con backoff 500 ms
- **Circuit Breaker**: 3 fallos → abrir por 60 seg (Resilience4j)
- **Fallback**: Retornar Optional.empty() y log error

**Performance**: SC-021 requiere ≤ 2 seg

---

### 2. Módulo 2 Logística (RabbitMQ Producer - Solicitar Ruta)

**Purpose**: Notificar a Logística que hay un pedido en estado ESPERANDO_RUTA para que asigne vehículo. (Spec 13)

**Protocol**: RabbitMQ (asíncrono, fire-and-forget)

**Exchange**: `inventario.pedidos` (topic exchange)

**Routing Key**: `ruta.solicitar`

**Message Schema** (Payload ultra-ligero):
```json
{
  "pedido_id": "uuid",
  "numero_pedido": "PED-20260403-001",
  "estado": "ESPERANDO_RUTA"
}
```

*Nota Backend:* Los datos requeridos como pesos, dirección de entrega y cliente se exponen a través del Controller interno definido en el `document_Api.yml` bajo la ruta: `GET /api/v1/external/pedidos/{pedidoId}?modulo=transporte`.

**Producer Logic**:
1. Crear mensaje al finalizar el `CreatePedidoUseCase`.
2. Publicar asíncronamente con `@Async`.
3. Log estructurado: `Ruta solicitada para pedido: {numero_pedido}`.

---

### 3. Módulo 2 Logística (RabbitMQ Consumer - Ruta Asignada)

**Purpose**: Recibir señal de ruta asignada para comprometer inventario.

**Protocol**: RabbitMQ (asíncrono)

**Queue**: `inventario.ruta-asignada` (durabile)

**Exchange**: `logistica.eventos` (topic exchange)

**Routing Key**: `ruta.asignada`

**Message Schema**:
```json
{
  "pedido_id": "uuid",
  "ruta_id": "uuid",
  "fecha_asignacion": "2026-04-03T11:00:00Z"
}
```

**Consumer Logic**:
1. Recibir mensaje.
2. Validar schema (pedido_id, ruta_id no vacíos).
3. Llamar REST / UseCase: `ComprometerInventarioUseCase(pedido_id, ruta_id)`.
4. Si éxito: ACK (mensaje confirmado)
5. Si error (pedido no existe o ya comprometido): ACK + log warning (idempotencia).
6. Si error transitorio (BD caída): NACK con requeue.

**Resilience**:
- **Retry**: Máx 3 reintentos con backoff exponencial.

---

### 4. Módulo 3 Financiero (RabbitMQ Producer)

**Purpose**: Notificar creación de pedido para generación de factura y liquidación final (Spec 15)

**Protocol**: RabbitMQ (asíncrono, fire-and-forget)

**Exchange**: `inventario.pedidos` (topic exchange)

**Routing Key**: `pedido.creado`

**Message Schema** (Payload ultra-ligero para API call posterior):
```json
{
  "pedido_id": "uuid",
  "numero_pedido": "PED-20260403-001",
  "evento": "PEDIDO_CREADO"
}
```
*Nota Backend:* Siguiendo el estándar, el módulo Financiero consumirá los costos, datos del cliente y lista de lotes desde el endpoint REST diseñado en el OpenAPI usando: `GET /api/v1/external/pedidos/{pedidoId}?modulo=financiero` el cual retorna el schema `PedidoFinancieroResponse`.

**Producer Logic**:
1. Crear mensaje con datos del pedido
2. Publicar a exchange con routing key
3. No esperar confirmación (fire-and-forget)
4. Log info: "Mensaje publicado: pedido.creado - {numero_pedido}"
5. Si falla publicación: Log error pero NO fallar creación de pedido

**Resilience**:
- **Async**: Publicación asíncrona (@Async)
- **Retry**: Máx 2 reintentos con backoff 500 ms
- **Fallback**: Log error y continuar (no bloquear pedido)

---

## Implementation Tasks

### Phase 1: HTTP Client para Módulo Usuarios

**T001: Configurar RestTemplate/WebClient**
- Path: `infrastructure/external/config/HttpClientConfig.java`
- Bean: RestTemplate con timeouts configurados
  - Connection timeout: 2 seg
  - Read timeout: 5 seg
- Interceptor para logging de requests/responses

**T002: Implementar ModuloUsuariosAdapter**
- Path: `infrastructure/external/ModuloUsuariosAdapter.java`
- Implementa: `ClienteServicePort` (domain/ports)
- Método: `Optional<Cliente> findByCedula(String cedula)`
- Lógica:
  1. GET `${modulo.usuarios.base-url}/api/usuarios/clientes/{cedula}`
  2. Parse JSON → Cliente entity
  3. Return Optional.of(cliente)
  4. Catch 404 → return Optional.empty()
  5. Catch 5xx, timeout → throw ClienteServiceException

**T003: Configurar Circuit Breaker con Resilience4j**
- Dependency: `io.github.resilience4j:resilience4j-spring-boot3`
- Configuración en `application.yml`:
  ```yaml
  resilience4j.circuitbreaker:
    instances:
      moduloUsuarios:
        failure-rate-threshold: 50
        slow-call-rate-threshold: 50
        slow-call-duration-threshold: 2s
        wait-duration-in-open-state: 60s
        permitted-number-of-calls-in-half-open-state: 3
        sliding-window-type: COUNT_BASED
        sliding-window-size: 10
  ```
- Anotar método con `@CircuitBreaker(name = "moduloUsuarios")`

**T004: Configurar Retry**
- Configuración en `application.yml`:
  ```yaml
  resilience4j.retry:
    instances:
      moduloUsuarios:
        max-attempts: 2
        wait-duration: 500ms
        retry-exceptions:
          - java.net.SocketTimeoutException
          - org.springframework.web.client.ResourceAccessException
  ```
- Anotar método con `@Retry(name = "moduloUsuarios")`

### Phase 2: RabbitMQ Setup

**T005: Configurar RabbitMQ Connection**
- Dependency: `org.springframework.boot:spring-boot-starter-amqp`
- Configuración en `application.yml`:
  ```yaml
  spring.rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    listener:
      simple:
        prefetch: 10
        default-requeue-rejected: false
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000ms
          multiplier: 2.0
  ```

**T006: Declarar Exchange, Queues y Bindings**
- Path: `infrastructure/messaging/config/RabbitMQConfig.java`
- Declarar:
  1. **Consumer (Módulo 2)**:
     - Exchange: `logistica.eventos` (topic)
     - Queue: `inventario.ruta-asignada` (durable)
     - DLQ: `inventario.ruta-asignada.dlq` (durable)
     - Binding: queue → exchange con routing key `ruta.asignada`
  2. **Producer (Módulo 3)**:
     - Exchange: `inventario.pedidos` (topic)
     - Queues no declaradas aquí (responsabilidad del Módulo 3)

### Phase 3: RabbitMQ Consumer (Módulo 2)

**T007: Crear DTO para mensaje RutaAsignada**
- Path: `infrastructure/messaging/dto/RutaAsignadaMessage.java`
- Atributos: pedido_id, ruta_id, fecha_asignacion
- Validaciones: @NotNull, @NotBlank

**T008: Implementar RutaAsignadaConsumer**
- Path: `infrastructure/messaging/consumers/RutaAsignadaConsumer.java`
- Anotación: `@RabbitListener(queues = "inventario.ruta-asignada")`
- Método: `void onRutaAsignada(RutaAsignadaMessage message)`
- Lógica:
  1. Log info: "Mensaje recibido: ruta.asignada - {pedido_id}"
  2. Validar schema (JsonSchema o Bean Validation)
  3. Buscar Pedido por pedido_id
  4. Si no existe: log error + return (ACK automático)
  5. Si estado != ESPERANDO_RUTA: log warning "Pedido ya procesado" + return (idempotencia)
  6. Llamar `ComprometerInventarioUseCase(pedido_id, ruta_id)`
  7. Si éxito: log info + return (ACK)
  8. Si error transitorio: throw exception → NACK + requeue
  9. Si 3 fallos: mensaje va a DLQ

**T009: Configurar manejo de errores**
- ErrorHandler customizado:
  - `AmqpRejectAndDontRequeueException` → ACK (no reintentar)
  - `DataAccessException` → NACK + requeue (error transitorio)
  - Otras exceptions → NACK + requeue

### Phase 4: RabbitMQ Producer (Módulo 3)

**T010: Crear DTO para mensaje PedidoCreado**
- Path: `infrastructure/messaging/dto/PedidoCreadoMessage.java`
- Atributos: pedido_id, numero_pedido, cliente_cc, fecha_creacion, asesor_id, lineas[], total_unidades

**T011: Implementar PedidoCreadoProducer**
- Path: `infrastructure/messaging/producers/PedidoCreadoProducer.java`
- Bean: Inject `RabbitTemplate`
- Método: `void publicarPedidoCreado(Pedido pedido)`
- Lógica:
  1. Mapear Pedido → PedidoCreadoMessage
  2. `rabbitTemplate.convertAndSend("inventario.pedidos", "pedido.creado", message)`
  3. Log info: "Mensaje publicado: pedido.creado - {numero_pedido}"
  4. Catch exception: log error + continuar (no fallar creación de pedido)

**T012: Configurar publicación asíncrona**
- Anotación: `@Async` en método publicarPedidoCreado
- Configurar `@EnableAsync` en Application class
- Thread pool config en `application.yml`:
  ```yaml
  spring.task.execution:
    pool:
      core-size: 2
      max-size: 5
      queue-capacity: 100
  ```

### Phase 5: Monitoring & Logging

**T013: Implementar logging estructurado**
- Path: `infrastructure/logging/IntegrationLogger.java`
- Usar SLF4J con structured logging (JSON format)
- Logs de integración:
  - HTTP request/response: URL, status, duration
  - RabbitMQ consume: queue, routing key, message_id, duration, result (ACK/NACK)
  - RabbitMQ publish: exchange, routing key, message_id, result

**T014: Configurar métricas con Micrometer**
- Dependency: `io.micrometer:micrometer-registry-prometheus`
- Métricas a exponer:
  - `http_client_requests_seconds` (HTTP a Módulo Usuarios)
  - `rabbitmq_consumer_messages_total` (mensajes consumidos)
  - `rabbitmq_consumer_errors_total` (errores en consumer)
  - `rabbitmq_producer_messages_total` (mensajes publicados)
  - `circuit_breaker_state` (estado del circuit breaker)
- Endpoint: `/actuator/metrics`, `/actuator/prometheus`

**T015: Configurar health checks**
- Path: `infrastructure/health/ExternalServicesHealthIndicator.java`
- Health checks:
  - Módulo Usuarios: Ping `/actuator/health` (cada 30 seg)
  - RabbitMQ: Spring AMQP auto-configured health indicator
- Endpoint: `/actuator/health` con detail level FULL

### Phase 6: Testing

**T016: Unit tests - ModuloUsuariosAdapter con WireMock**
- Test GET cliente exitoso (200 OK)
- Test cliente no encontrado (404)
- Test timeout (simular delay > 5 seg)
- Test circuit breaker (3 fallos → open state)

**T017: Integration tests - RabbitMQ Consumer**
- Test mensaje válido → pedido comprometido
- Test pedido no existe → ACK + log error
- Test pedido ya comprometido → ACK + log warning (idempotencia)
- Test error de BD → NACK + requeue (simular con Testcontainers)
- Test 3 fallos → mensaje va a DLQ

**T018: Integration tests - RabbitMQ Producer**
- Test publicar pedido → verificar mensaje en queue
- Test schema del mensaje (validar JSON)
- Test fallo de publicación → log error, pedido NO falla

**T019: Contract tests**
- Consumer contract test: Validar schema de mensaje `ruta.asignada` (Pact)
- Producer contract test: Validar schema de mensaje `pedido.creado` (Pact)

---

## Tests

### Unit Tests
- ModuloUsuariosAdapter: 6 tests (WireMock)

### Integration Tests
- RabbitMQ Consumer: 6 tests (Testcontainers)
- RabbitMQ Producer: 3 tests (Testcontainers)
- Contract tests: 2 tests (Pact)

---

## Configuration Properties

```yaml
# application.yml

# Módulo Usuarios
modulo:
  usuarios:
    base-url: http://localhost:8081
    timeout:
      connect: 2s
      read: 5s

# RabbitMQ
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    listener:
      simple:
        prefetch: 10
        default-requeue-rejected: false
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000ms
          multiplier: 2.0

# Resilience4j
resilience4j:
  circuitbreaker:
    instances:
      moduloUsuarios:
        failure-rate-threshold: 50
        slow-call-rate-threshold: 50
        slow-call-duration-threshold: 2s
        wait-duration-in-open-state: 60s
        sliding-window-size: 10
  retry:
    instances:
      moduloUsuarios:
        max-attempts: 2
        wait-duration: 500ms

# Async
spring:
  task:
    execution:
      pool:
        core-size: 2
        max-size: 5
        queue-capacity: 100

# Monitoring
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  health:
    show-details: always
```

---

## Acceptance Criteria

**FR-052**: Módulo 1 consulta Módulo de Usuarios para validar clientes activos antes de crear pedido.

**Spec 15**: Módulo 1 consume mensaje de Módulo 2 Logística para comprometer inventario cuando ruta es asignada.

**Spec 15**: Módulo 1 publica mensaje a Módulo 3 Financiero cuando pedido es creado.

**SC-021**: Consulta de cliente completa en ≤ 2 segundos.

**Resilience**: Circuit breaker protege el sistema si Módulo Usuarios no responde.

**Idempotencia**: Mensajes duplicados de ruta asignada NO re-comprometen inventario.

**Observability**: Métricas y logs estructurados para troubleshooting de integraciones.

---

## Notes & Best Practices

1. **Circuit Breaker es crítico**: Si Módulo Usuarios cae, el circuit breaker evita que Módulo 1 se sature con timeouts. Después de 3 fallos, abrir circuito por 60 seg.

2. **Fire-and-forget para Módulo 3**: La publicación a Módulo Financiero NO debe bloquear la creación de pedido. Usar @Async y log error si falla.

3. **Idempotencia en Consumer**: Verificar si pedido ya está COMPROMETIDO antes de procesar mensaje. Esto evita re-compromiso si mensaje llega duplicado.

4. **DLQ (Dead Letter Queue)**: Mensajes que fallan 3 veces van a DLQ para análisis manual. NO reintentar indefinidamente.

5. **Prefetch limitado**: Configurar prefetch=10 para evitar que un consumer lento acumule muchos mensajes en memoria.

6. **Logging estructurado**: Usar JSON format para logs de integración (facilita búsqueda en ELK/Splunk). Incluir: timestamp, service, operation, duration, result, error.

7. **Contract testing**: Usar Pact para validar schemas de mensajes. Si Módulo 2 cambia schema de `ruta.asignada`, los tests fallarán.

8. **Health checks**: Exponer `/actuator/health` con detalle de Módulo Usuarios y RabbitMQ. Útil para monitoreo (Kubernetes liveness/readiness probes).

9. **Timeouts agresivos**: 2 seg para connect, 5 seg para read. SC-021 requiere ≤ 2 seg para consulta de cliente. No esperar más de 5 seg.

10. **Monitoring crítico**: Exponer métricas de:
    - Tasa de éxito/fallo de HTTP (Módulo Usuarios)
    - Tasa de ACK/NACK de mensajes (RabbitMQ)
    - Estado del circuit breaker (open/closed)
    - Latencia de integraciones (P50, P95, P99)
