# Plan de Implementación: Gestión de Pedidos - Backend

**Date**: 2026-04-03  
**Specs**: 07_consultar_datos_cliente.md · 08_realizar_pedido.md · 09_consultar_detalle_pedido.md · 10_consultar_lista_pedidos.md  
**Module**: Módulo 1 - Inventario  
**Layer**: Backend  
**Priority**: P1 (Núcleo comercial del módulo)

---

## Summary

Núcleo comercial del módulo. Permite consultar clientes (Módulo Usuarios), crear pedidos sin comprometer inventario (estado Esperando Ruta), comprometer lotes FEFO cuando Módulo 2 asigna ruta, y consultar pedidos. Integración crítica: consume Módulo Usuarios (HTTP), consume Módulo 2 Logística (RabbitMQ), publica a Módulo 3 Financiero (RabbitMQ).

---

## Dependencies

**Blocked by**:
- Plan Gestión SKU Backend (requiere productos)
- Plan Recepción Backend (requiere lotes con stock)
- Setup de integración (Phase 1 del plan maestro: RabbitMQ, HTTP client)

**Blocks**:
- Plan Picking/Despacho Backend (requiere pedidos en estado Comprometido)
- Plan Frontend Pedidos (consume estos endpoints)

**External Dependencies**:
- **Módulo de Usuarios** (HTTP REST): Consultar datos de cliente - SC-021 (≤ 2 seg)
- **Módulo 2 Logística** (RabbitMQ Consumer): Recibir señal de ruta asignada
- **Módulo 3 Financiero** (RabbitMQ Producer): Enviar datos de pedido creado

---

## Entities & Domain

### Cliente (External Entity)
**Propósito**: Entidad del Módulo de Usuarios, consumida por HTTP.

**Atributos** (solo lectura):
- `cedula`: String (PK en módulo externo)
- `nombre`: String
- `telefono`: String
- `email`: String
- `direccion`: String
- `activo`: Boolean

**Business Rules**:
- Cliente debe estar activo para crear pedido - FR-052
- Módulo 1 NO crea ni modifica clientes (solo consulta)

### Pedido
**Propósito**: Solicitud de productos de un cliente.

**Atributos**:
- `pedido_id`: UUID (PK, autogenerado)
- `numero_pedido`: String (único, formato: "PED-YYYYMMDD-NNN", ej: "PED-20260403-001")
- `cliente_cc`: String (FK lógica → Módulo Usuarios, inmutable)
- `fecha_creacion`: DateTime
- `estado`: Enum (Esperando Ruta, Comprometido, En Picking, Despachado)
- `ruta_id`: UUID (nullable, asignado por Módulo 2)
- `fecha_compromiso`: DateTime (nullable, cuando estado → Comprometido)
- `asesor_id`: UUID (FK → Usuario, quien creó el pedido)

**Constraints**:
- UNIQUE(numero_pedido)
- cliente_cc es inmutable (no se puede cambiar cliente de un pedido)

**Business Rules**:
- Pedido creado en estado Esperando Ruta SIN comprometer lotes - FR-057
- Compromiso de lotes solo cuando Módulo 2 asigna ruta - FR-058
- Si stock insuficiente al crear → rechazo inmediato - FR-055
- Si stock insuficiente al comprometer → compromiso parcial + notificación - SC-026

**State Machine**:
```
Esperando Ruta → Comprometido → En Picking → Despachado
      ↓
  [Cancelado] (opcional, fuera de alcance Fase 1)
```

### ProductoPedido
**Propósito**: Línea de pedido (producto + cantidad).

**Atributos**:
- `producto_pedido_id`: UUID (PK, autogenerado)
- `pedido_id`: UUID (FK → Pedido)
- `sku_id`: UUID (FK → Producto)
- `cantidad_solicitada`: Integer (inmutable, lo que pidió el cliente)
- `cantidad_confirmada`: Integer (mutable, lo que se pudo comprometer, inicialmente = cantidad_solicitada)

**Constraints**:
- cantidad_solicitada > 0
- cantidad_confirmada <= cantidad_solicitada
- cantidad_confirmada >= 0

**Business Rules**:
- cantidad_confirmada se ajusta al comprometer si stock insuficiente - SC-026
- Si cantidad_confirmada < cantidad_solicitada → generar alerta/notificación

### LoteComprometido
**Propósito**: Registro de lotes reservados para un pedido (FEFO).

**Atributos**:
- `compromiso_id`: UUID (PK, autogenerado)
- `producto_pedido_id`: UUID (FK → ProductoPedido)
- `codigo_lote`: String (FK → Lote)
- `cantidad_comprometida`: Integer
- `fecha_compromiso`: DateTime

**Constraints**:
- cantidad_comprometida > 0

**Business Rules**:
- LoteComprometido solo se crea cuando estado = Comprometido (ruta asignada) - FR-058
- Lotes se seleccionan con FEFO (First Expired First Out) - FR-061
- Al comprometer: Lote.cantidad se reduce, se crea MovimientoInventario tipo Compromiso

---

## Use Cases

### UC-001: Consultar Datos de Cliente (Spec 07)

**Actor**: Asesor Comercial

**Flujo Principal**:
1. Asesor ingresa cédula del cliente
2. Sistema llama HTTP GET al Módulo de Usuarios: `/api/usuarios/clientes/{cedula}`
3. Sistema valida:
   - Cliente existe en Módulo Usuarios
   - Cliente tiene activo = true
4. Sistema retorna datos del cliente: nombre, teléfono, email, dirección

**Business Logic**:
- Timeout de consulta HTTP: 5 seg (configurable) - SC-021 requiere ≤ 2 seg
- Circuit breaker: Si Módulo Usuarios no responde (3 fallos), abrir circuito por 1 min
- Retry: 1 reintento automático con backoff 500 ms

**Error Handling**:
- 404 Not Found: Cliente no existe en Módulo Usuarios
- 400 Bad Request: Cliente existe pero activo = false
- 503 Service Unavailable: Módulo Usuarios no responde

**Performance**:
- Consulta debe completar en ≤ 2 seg - SC-021

### UC-002: Realizar Pedido (Spec 08 - Parte 1: Crear sin comprometer)

**Actor**: Asesor Comercial

**Flujo Principal**:
1. Asesor ingresa:
   - cliente_cc
   - Lista de líneas: [{sku_id, cantidad_solicitada}]
2. Sistema valida:
   - Cliente existe y está activo (llamar UC-001)
   - Todos los sku_id existen en catálogo
   - Todas las cantidades > 0
   - Stock disponible >= cantidad_solicitada para CADA línea (sin comprometer aún)
3. Sistema ejecuta transacción:
   a. Crear Pedido (estado: Esperando Ruta, generar numero_pedido)
   b. Para cada línea: crear ProductoPedido (cantidad_confirmada = cantidad_solicitada inicialmente)
   c. NO crear LoteComprometido (aún no hay ruta)
   d. Publicar mensaje a Módulo 3 Financiero (evento: PedidoCreado)
4. Sistema retorna confirmación con pedido_id y numero_pedido

**Business Logic**:
- **Generar numero_pedido**: "PED-YYYYMMDD-NNN" (ej: "PED-20260403-001")
  - Secuencial por día (consultar último número del día + 1)
- **Validación de stock SIN comprometer**: Consultar SUM(Lote.cantidad) por sku_id
  - Si stock < cantidad_solicitada → rechazo inmediato - FR-055
- **Pedido queda en Esperando Ruta**: Sin lotes comprometidos - FR-057
- **Mensaje asíncrono a Módulo 3**: Fire-and-forget, no bloquea respuesta al usuario

**Validations**:
- Cliente no encontrado → 404
- Cliente inactivo → 400 con mensaje "Cliente inactivo, no puede realizar pedidos"
- SKU no existe → 404 con sku_id específico
- Stock insuficiente → 409 con detalle: {sku_id, stock_disponible, cantidad_solicitada}

**Performance**:
- Confirmación de pedido ≤ 5 seg - SC-029

### UC-003: Comprometer Inventario (Spec 08 - Parte 2: Ruta Asignada)

**Actor**: Sistema (trigger: mensaje de Módulo 2 Logística)

**Flujo Principal**:
1. Sistema recibe mensaje de Módulo 2: {pedido_id, ruta_id}
2. Sistema valida:
   - Pedido existe y estado = Esperando Ruta
3. Sistema ejecuta transacción atómica:
   a. Para cada ProductoPedido:
      - Buscar lotes con cantidad > 0, ordenados por fecha_vencimiento ASC (FEFO)
      - Comprometer lotes hasta cubrir cantidad_solicitada (o lo máximo disponible)
      - Para cada lote comprometido:
        * Crear LoteComprometido
        * Reducir Lote.cantidad
        * Crear MovimientoInventario tipo Compromiso
      - Actualizar ProductoPedido.cantidad_confirmada
   b. Actualizar Pedido.estado = Comprometido
   c. Actualizar Pedido.ruta_id = ruta_id
   d. Actualizar Pedido.fecha_compromiso = ahora
4. Si alguna línea no pudo comprometer cantidad_solicitada completa:
   - Continuar con compromiso parcial
   - Generar alerta/notificación (log, evento) - SC-026
5. Retornar confirmación (o log de proceso)

**Business Logic**:
- **FEFO estricto**: Algoritmo selecciona lotes con fecha_vencimiento más cercana primero - FR-061
- **Compromiso parcial**: Si stock insuficiente, comprometer lo disponible - SC-026
- **Atomicidad**: Si falla algún paso, rollback completo (pedido vuelve a Esperando Ruta)
- **Idempotencia**: Si mensaje duplicado, verificar si pedido ya está Comprometido (no re-comprometer)

**Algoritmo FEFO**:
```
Para cada ProductoPedido:
  cantidad_pendiente = cantidad_solicitada
  lotes_disponibles = SELECT lote WHERE sku_id = X AND cantidad > 0 ORDER BY fecha_vencimiento ASC
  
  Para cada lote en lotes_disponibles:
    cantidad_a_comprometer = MIN(cantidad_pendiente, lote.cantidad)
    
    Crear LoteComprometido(codigo_lote, cantidad_a_comprometer)
    lote.cantidad -= cantidad_a_comprometer
    Crear MovimientoInventario(tipo: Compromiso, cantidad: -cantidad_a_comprometer)
    
    cantidad_pendiente -= cantidad_a_comprometer
    
    Si cantidad_pendiente == 0:
      BREAK (línea completa)
  
  ProductoPedido.cantidad_confirmada = cantidad_solicitada - cantidad_pendiente
  
  Si cantidad_pendiente > 0:
    LOG ALERTA: "Stock insuficiente para pedido {numero_pedido}, SKU {sku_id}: solicitado {cantidad_solicitada}, confirmado {cantidad_confirmada}"
```

**Error Handling**:
- Pedido no existe → log error, no reintentar
- Pedido no está en Esperando Ruta → log warning (posible duplicado), no reintentar
- Fallo en transacción → log error, reintentar (hasta 3 intentos con backoff exponencial)

**Performance**:
- Compromiso de pedido con 20 líneas ≤ 10 seg

### UC-004: Consultar Detalle de Pedido (Spec 09)

**Actor**: Asesor Comercial, Operario, Supervisor

**Flujo**:
1. Usuario solicita detalle de pedido por pedido_id o numero_pedido
2. Sistema consulta:
   - Datos del Pedido
   - Datos del Cliente (llamar Módulo Usuarios por cliente_cc)
   - Líneas del pedido (ProductoPedido con información de Producto)
   - Si estado >= Comprometido: Lotes comprometidos por línea
3. Sistema retorna:
   - Información completa del pedido
   - Cliente (nombre, teléfono, dirección)
   - Líneas con: producto (marca, presentacion), cantidad_solicitada, cantidad_confirmada
   - Si comprometido: detalle de lotes (codigo_lote, fecha_vencimiento, cantidad)

**Business Logic**:
- Si cliente_cc no se encuentra en Módulo Usuarios (cliente eliminado), retornar cliente_cc como texto sin datos adicionales
- Ordenar líneas por orden de creación
- Ordenar lotes comprometidos por fecha_compromiso (FEFO)

### UC-005: Consultar Lista de Pedidos (Spec 10)

**Actor**: Asesor Comercial, Supervisor

**Flujo**:
1. Usuario solicita lista con filtros:
   - estado (opcional)
   - fecha_desde, fecha_hasta (opcional)
   - cliente_cc (opcional)
   - numero_pedido (búsqueda parcial, opcional)
   - page, size (paginación)
2. Sistema consulta Pedidos con filtros aplicados
3. Sistema retorna lista paginada con:
   - pedido_id, numero_pedido, cliente_cc, fecha_creacion, estado
   - Total de líneas, cantidad total de unidades

**Business Logic**:
- Ordenar por fecha_creacion DESC (más recientes primero)
- Paginación: Default size=20, Max size=100

---

## Endpoints / API

### GET /api/v1/clientes/{cedula}
**Purpose**: Consultar datos de cliente (proxy a Módulo Usuarios)

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

**Response 404 Not Found**:
```json
{
  "error": "CLIENTE_NOT_FOUND",
  "message": "Cliente con cédula 1234567890 no existe en el sistema"
}
```

**Response 400 Bad Request** (cliente inactivo):
```json
{
  "error": "CLIENTE_INACTIVO",
  "message": "Cliente está inactivo y no puede realizar pedidos"
}
```

**Response 503 Service Unavailable**:
```json
{
  "error": "SERVICIO_NO_DISPONIBLE",
  "message": "Módulo de Usuarios no está disponible. Intente más tarde."
}
```

### POST /api/v1/pedidos
**Purpose**: Crear pedido (sin comprometer inventario)

**Request Body**:
```json
{
  "cliente_cc": "1234567890",
  "asesor_id": "uuid",
  "lineas": [
    {
      "sku_id": "uuid",
      "cantidad_solicitada": 120
    },
    {
      "sku_id": "uuid",
      "cantidad_solicitada": 240
    }
  ]
}
```

**Response 201 Created**:
```json
{
  "pedido_id": "uuid",
  "numero_pedido": "PED-20260403-001",
  "estado": "Esperando Ruta",
  "fecha_creacion": "2026-04-03T10:30:00Z",
  "cliente": {
    "cedula": "1234567890",
    "nombre": "Juan Pérez"
  },
  "lineas": [
    {
      "producto_pedido_id": "uuid",
      "sku": {
        "sku_id": "uuid",
        "marca": "Pilsen",
        "presentacion": "Six-pack"
      },
      "cantidad_solicitada": 120,
      "cantidad_confirmada": 120
    }
  ],
  "total_unidades": 360
}
```

**Response 409 Conflict** (stock insuficiente):
```json
{
  "error": "STOCK_INSUFICIENTE",
  "message": "Stock insuficiente para completar el pedido",
  "detalles": [
    {
      "sku_id": "uuid",
      "marca": "Pilsen",
      "presentacion": "Six-pack",
      "cantidad_solicitada": 500,
      "stock_disponible": 240
    }
  ]
}
```

**Validations**:
- 400: cliente_cc vacío, líneas vacías, cantidad ≤ 0
- 404: Cliente no encontrado, SKU no existe

### GET /api/v1/pedidos/{id}
**Purpose**: Consultar detalle completo de pedido

**Path Param**: `id` puede ser UUID (pedido_id) o String (numero_pedido)

**Response 200 OK**:
```json
{
  "pedido_id": "uuid",
  "numero_pedido": "PED-20260403-001",
  "estado": "Comprometido",
  "fecha_creacion": "2026-04-03T10:30:00Z",
  "fecha_compromiso": "2026-04-03T11:00:00Z",
  "ruta_id": "uuid",
  "cliente": {
    "cedula": "1234567890",
    "nombre": "Juan Pérez",
    "telefono": "+57 300 123 4567",
    "direccion": "Calle 123 #45-67, Bogotá"
  },
  "asesor": {
    "asesor_id": "uuid",
    "nombre": "María López"
  },
  "lineas": [
    {
      "producto_pedido_id": "uuid",
      "sku": {
        "sku_id": "uuid",
        "marca": "Pilsen",
        "presentacion": "Six-pack",
        "contenido_ml": 1980
      },
      "cantidad_solicitada": 120,
      "cantidad_confirmada": 120,
      "lotes_comprometidos": [
        {
          "codigo_lote": "LOT-2026-001",
          "codigo_lote": "LOT-2025-001",
          "fecha_vencimiento": "2026-06-15",
          "cantidad_comprometida": 80
        },
        {
          "codigo_lote": "LOT-2026-001",
          "codigo_lote": "LOT-2025-020",
          "fecha_vencimiento": "2026-07-20",
          "cantidad_comprometida": 40
        }
      ]
    }
  ],
  "total_solicitado": 360,
  "total_confirmado": 360
}
```

**Response 404 Not Found**:
```json
{
  "error": "PEDIDO_NOT_FOUND",
  "message": "Pedido no encontrado"
}
```

### GET /api/v1/pedidos
**Purpose**: Listar pedidos con filtros

**Query Params**:
- `estado` (opcional): Esperando Ruta | Comprometido | En Picking | Despachado
- `fecha_desde`, `fecha_hasta` (opcional): ISO Date
- `cliente_cc` (opcional): String
- `numero_pedido` (opcional): String (búsqueda parcial con LIKE)
- `page` (default: 0), `size` (default: 20)

**Response 200 OK**:
```json
{
  "pedidos": [
    {
      "pedido_id": "uuid",
      "numero_pedido": "PED-20260403-001",
      "cliente_cc": "1234567890",
      "cliente_nombre": "Juan Pérez",
      "fecha_creacion": "2026-04-03T10:30:00Z",
      "estado": "Comprometido",
      "total_lineas": 3,
      "total_unidades": 360
    }
  ],
  "pagination": {
    "total_elements": 145,
    "total_pages": 8,
    "current_page": 0,
    "page_size": 20
  }
}
```

---

## Implementation Tasks

### Phase 1: Domain Entities

**T001: Crear entidad Pedido (domain)**
- Path: `domain/entities/Pedido.java`
- Atributos según especificación
- Enum EstadoPedido: ESPERANDO_RUTA, COMPROMETIDO, EN_PICKING, DESPACHADO
- Validaciones: cliente_cc no vacío, fecha_creacion no futura

**T002: Crear entidad ProductoPedido (domain)**
- Path: `domain/entities/ProductoPedido.java`
- Atributos según especificación
- Validación: cantidad_confirmada <= cantidad_solicitada

**T003: Crear entidad LoteComprometido (domain)**
- Path: `domain/entities/LoteComprometido.java`
- Atributos según especificación
- Validación: cantidad_comprometida > 0

**T004: Crear entidad Cliente (domain) - External**
- Path: `domain/entities/Cliente.java`
- Atributos: cedula, nombre, telefono, email, direccion, activo
- Nota: Entity NO persistida localmente, solo DTO del servicio externo

### Phase 2: Repository Interfaces & Ports

**T005: Crear PedidoRepository (domain/repositories)**
- Métodos:
  - `UUID save(Pedido pedido)`
  - `Optional<Pedido> findById(UUID id)`
  - `Optional<Pedido> findByNumeroPedido(String numeroPedido)`
  - `String generarNumeroPedido(LocalDate fecha)` // PED-YYYYMMDD-NNN
  - `Page<Pedido> findByFilters(EstadoPedido estado, String clienteCc, String numeroPedido, LocalDate desde, LocalDate hasta, Pageable pageable)`
  - `void update(Pedido pedido)`

**T006: Crear ProductoPedidoRepository (domain/repositories)**
- Métodos:
  - `List<UUID> saveAll(List<ProductoPedido> lineas)`
  - `List<ProductoPedido> findByPedidoId(UUID pedidoId)`
  - `void update(ProductoPedido productoPedido)`

**T007: Crear LoteComprometidoRepository (domain/repositories)**
- Métodos:
  - `List<UUID> saveAll(List<LoteComprometido> compromisos)`
  - `List<LoteComprometido> findByProductoPedidoId(UUID productoPedidoId)`
  - `List<LoteComprometido> findByPedidoId(UUID pedidoId)`

**T008: Crear ClienteServicePort (domain/ports)**
- Interface para integración con Módulo Usuarios
- Métodos:
  - `Optional<Cliente> findByCedula(String cedula)` throws ClienteServiceException
- Implementación en infrastructure/external

### Phase 3: Use Cases (Application Layer)

**T009: Implementar ConsultarClienteUseCase**
- Path: `application/usecases/ConsultarClienteUseCase.java`
- Input: cedula
- Output: ClienteDTO
- Lógica: Llamar ClienteServicePort, validar activo = true
- Timeout: 5 seg, Retry: 1 intento
- Circuit Breaker: 3 fallos → abrir por 60 seg

**T010: Implementar RealizarPedidoUseCase**
- Path: `application/usecases/RealizarPedidoUseCase.java`
- Input: RealizarPedidoCommand (cliente_cc, asesor_id, lineas)
- Output: PedidoDTO
- Lógica:
  1. Validar cliente (llamar ConsultarClienteUseCase)
  2. Validar SKUs existen (ProductoRepository)
  3. Validar stock disponible SIN comprometer (LoteRepository: SUM(cantidad) por sku)
  4. Transacción:
     - Generar numero_pedido (PED-YYYYMMDD-NNN secuencial)
     - Crear Pedido (estado: ESPERANDO_RUTA)
     - Crear ProductoPedidos
     - Publicar mensaje PedidoCreado a Módulo 3
  5. Retornar PedidoDTO

**T011: Implementar ComprometerInventarioUseCase**
- Path: `application/usecases/ComprometerInventarioUseCase.java`
- Input: ComprometerInventarioCommand (pedido_id, ruta_id)
- Output: ComprometerInventarioResult (confirmaciones, alertas)
- Lógica:
  1. Buscar Pedido (validar estado = ESPERANDO_RUTA)
  2. Transacción atómica:
     - Para cada ProductoPedido: aplicar algoritmo FEFO
     - Crear LoteComprometidos
     - Reducir Lote.cantidad
     - Crear MovimientoInventario tipo COMPROMISO
     - Actualizar ProductoPedido.cantidad_confirmada
     - Actualizar Pedido.estado = COMPROMETIDO, ruta_id, fecha_compromiso
  3. Si compromisos parciales: generar alertas
  4. Log resultado
- **Idempotencia**: Verificar si ya está Comprometido antes de procesar

**T012: Implementar ConsultarDetallePedidoUseCase**
- Path: `application/usecases/ConsultarDetallePedidoUseCase.java`
- Input: pedido_id o numero_pedido
- Output: DetallePedidoDTO (pedido, cliente, lineas, lotes)
- Lógica:
  1. Buscar Pedido (404 si no existe)
  2. Consultar Cliente por cliente_cc (ClienteServicePort)
  3. Buscar ProductoPedidos
  4. Si estado >= COMPROMETIDO: buscar LoteComprometidos
  5. Joins con Producto, Lote
  6. Retornar DTO completo

**T013: Implementar ConsultarListaPedidosUseCase**
- Path: `application/usecases/ConsultarListaPedidosUseCase.java`
- Input: FiltrosPedidosDTO (estado, cliente_cc, numero_pedido, fecha_desde, fecha_hasta, pageable)
- Output: Page<PedidoResumenDTO>
- Lógica: Consultar PedidoRepository con filtros, paginación

### Phase 4: Infrastructure - External Adapter

**T014: Implementar ModuloUsuariosAdapter (infrastructure/external)**
- Path: `infrastructure/external/ModuloUsuariosAdapter.java`
- Implementa: ClienteServicePort
- Tecnología: RestTemplate o WebClient (Spring)
- Configuración:
  - URL base: `${modulo.usuarios.base-url}` (ej: http://localhost:8081)
  - Timeout: 5 seg
  - Retry: 1 intento con backoff 500 ms
- Endpoint: GET `/api/usuarios/clientes/{cedula}`
- Parsing: JSON → Cliente entity
- Error handling:
  - 404 → return Optional.empty()
  - 5xx, timeout → throw ClienteServiceException (circuit breaker)

**T015: Configurar Circuit Breaker (Resilience4j)**
- Configuración en application.yml:
  ```yaml
  resilience4j.circuitbreaker:
    instances:
      moduloUsuarios:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 60s
        sliding-window-size: 10
  ```

### Phase 5: Infrastructure - Messaging

**T016: Implementar RutaAsignadaConsumer (infrastructure/messaging)**
- Path: `infrastructure/messaging/RutaAsignadaConsumer.java`
- Tecnología: RabbitMQ Listener (Spring AMQP)
- Queue: `inventario.ruta-asignada` (bound a exchange del Módulo 2)
- Message Schema:
  ```json
  {
    "pedido_id": "uuid",
    "ruta_id": "uuid",
    "fecha_asignacion": "2026-04-03T11:00:00Z"
  }
  ```
- Lógica:
  1. Parse mensaje
  2. Llamar ComprometerInventarioUseCase
  3. Acknowledge mensaje (ACK)
  4. Error handling: NACK con requeue (máx 3 reintentos)

**T017: Implementar PedidoCreadoProducer (infrastructure/messaging)**
- Path: `infrastructure/messaging/PedidoCreadoProducer.java`
- Tecnología: RabbitTemplate (Spring AMQP)
- Exchange: `inventario.pedidos` (topic exchange)
- Routing Key: `pedido.creado`
- Message Schema:
  ```json
  {
    "pedido_id": "uuid",
    "numero_pedido": "PED-20260403-001",
    "cliente_cc": "1234567890",
    "fecha_creacion": "2026-04-03T10:30:00Z",
    "lineas": [
      {
        "sku_id": "uuid",
        "cantidad": 120
      }
    ],
    "total_unidades": 360
  }
  ```
- Fire-and-forget: No esperar confirmación (async)

### Phase 6: Infrastructure - Database

**T018: Crear schema SQL con Flyway**
- Path: `infrastructure/db/migrations/V005__create_pedidos.sql`
- Tablas:
  - `pedidos` (PK: pedido_id, UNIQUE: numero_pedido, FK: cliente_cc lógica, asesor_id)
  - `productos_pedido` (PK: producto_pedido_id, FK: pedido_id, sku_id)
  - `lotes_comprometidos` (PK: compromiso_id, FK: producto_pedido_id, codigo_lote)
- Índices:
  - `idx_pedidos_numero` ON pedidos(numero_pedido)
  - `idx_pedidos_cliente` ON pedidos(cliente_cc)
  - `idx_pedidos_estado` ON pedidos(estado)
  - `idx_pedidos_fecha` ON pedidos(fecha_creacion)
  - `idx_productos_pedido_pedido` ON productos_pedido(pedido_id)
  - `idx_lotes_comprometidos_producto` ON lotes_comprometidos(producto_pedido_id)

**T019: Implementar JPA Entities (infrastructure/persistence)**
- PedidoEntity.java (con @Entity, @Enumerated, @OneToMany)
- ProductoPedidoEntity.java
- LoteComprometidoEntity.java

**T020: Implementar JPA Repositories**
- JpaPedidoRepository extends JpaRepository
  - Query custom: `@Query("SELECT COALESCE(MAX(CAST(SUBSTRING(p.numeroPedido, -3) AS int)), 0) FROM PedidoEntity p WHERE p.numeroPedido LIKE :prefix")`
    para generar secuencial por día
  - Specification para filtros dinámicos
- JpaProductoPedidoRepository
- JpaLoteComprometidoRepository

**T021: Implementar Repository Adapters**
- PedidoRepositoryImpl implements PedidoRepository
- ProductoPedidoRepositoryImpl implements ProductoPedidoRepository
- LoteComprometidoRepositoryImpl implements LoteComprometidoRepository
- Mappers: Entity ↔ Domain

### Phase 7: REST Controllers

**T022: Implementar ClienteController**
- Path: `infrastructure/web/controllers/ClienteController.java`
- GET /api/v1/clientes/{cedula} → ConsultarClienteUseCase
- Error handling: 404, 400, 503

**T023: Implementar PedidoController**
- Path: `infrastructure/web/controllers/PedidoController.java`
- POST /api/v1/pedidos → RealizarPedidoUseCase
- GET /api/v1/pedidos/{id} → ConsultarDetallePedidoUseCase
- GET /api/v1/pedidos → ConsultarListaPedidosUseCase
- Validaciones con @Valid, Jakarta Validation
- Error handling: 400, 404, 409, 503

### Phase 8: Testing

**T024: Unit tests - Domain entities**
- Test EstadoPedido transitions
- Test ProductoPedido: cantidad_confirmada <= cantidad_solicitada

**T025: Unit tests - Use Cases**
- Test RealizarPedidoUseCase:
  - Happy path: Pedido creado con estado ESPERANDO_RUTA
  - Edge case: Stock justo suficiente (límite)
  - Error case: Stock insuficiente lanza StockInsuficienteException
  - Error case: Cliente inactivo lanza ClienteInactivoException
- Test ComprometerInventarioUseCase:
  - Happy path: FEFO selecciona lotes correctos (mock 3 lotes, verificar orden)
  - Edge case: Stock insuficiente, compromiso parcial con alerta
  - Edge case: Mensaje duplicado (idempotencia): no re-comprometer
  - Error case: Pedido no en ESPERANDO_RUTA → log warning

**T026: Integration tests - External Service (WireMock)**
- Test ConsultarClienteUseCase con WireMock:
  - Mock Módulo Usuarios responde 200 OK
  - Mock responde 404 Not Found
  - Mock responde 503 (timeout) → Circuit Breaker abre

**T027: Integration tests - Messaging**
- Test RutaAsignadaConsumer:
  - Enviar mensaje válido → pedido cambia a COMPROMETIDO
  - Enviar mensaje inválido (pedido_id no existe) → NACK
  - Verificar creación de LoteComprometidos en DB
- Test PedidoCreadoProducer:
  - Crear pedido → verificar mensaje en queue `inventario.pedidos`
  - Verificar schema del mensaje

**T028: Integration tests - Controllers**
- Test POST /api/v1/pedidos:
  - Crear pedido válido retorna 201 con numero_pedido
  - Stock insuficiente retorna 409 con detalles
  - Cliente inactivo retorna 400
- Test GET /api/v1/pedidos/{id}:
  - Retorna detalle completo con lotes comprometidos
  - Retorna 404 si pedido no existe

**T029: E2E test - Flujo completo**
- Crear pedido → verificar estado ESPERANDO_RUTA
- Enviar mensaje ruta asignada → verificar estado COMPROMETIDO
- Consultar detalle → verificar lotes comprometidos con FEFO correcto

---

## Tests

### Unit Tests
- Domain entities: 5 tests
- Use Cases: 20 tests (incluyendo FEFO, idempotencia, compromisos parciales)

### Integration Tests
- External service (WireMock): 5 tests
- Messaging: 4 tests
- Controllers: 8 tests
- E2E: 3 tests

### Performance Tests
- Confirmación de pedido ≤ 5 seg - SC-029
- Consulta cliente ≤ 2 seg - SC-021
- Compromiso pedido (20 líneas) ≤ 10 seg

---

## Acceptance Criteria

**FR-052**: Solo clientes activos pueden realizar pedidos (validación en Módulo Usuarios).

**FR-055**: Si stock insuficiente al crear pedido, rechazo inmediato (409 Conflict).

**FR-057**: Pedido creado en estado Esperando Ruta SIN comprometer lotes.

**FR-058**: Compromiso de lotes solo cuando Módulo 2 asigna ruta (mensaje asíncrono).

**FR-061**: Lotes comprometidos con FEFO (First Expired First Out): fecha_vencimiento ASC.

**SC-026**: Si stock insuficiente al comprometer, se compromete lo disponible y se notifica/alerta.

**SC-021**: Consulta de cliente ≤ 2 segundos.

**SC-029**: Confirmación de pedido ≤ 5 segundos.

---

## Notes & Best Practices

1. **Dos fases del pedido**: Crear (sin comprometer) vs Comprometer (con ruta). Esta separación es crítica - FR-057, FR-058.

2. **Algoritmo FEFO**: Implementar en ComprometerInventarioUseCase. Ordenar lotes por fecha_vencimiento ASC, iterar hasta cubrir cantidad_solicitada.

3. **Generación de numero_pedido**: Formato "PED-YYYYMMDD-NNN". Secuencial por día (consultar MAX del día + 1). Usar transacción optimista o pessimistic lock para evitar duplicados en concurrencia.

4. **Idempotencia en RutaAsignadaConsumer**: Verificar si pedido ya está COMPROMETIDO antes de procesar. Si ya está, log warning y ACK (no es error).

5. **Circuit Breaker en Módulo Usuarios**: Resilience4j configurado con umbral 50%, ventana de 10 requests, wait 60 seg. Si abre, retornar 503 Service Unavailable al frontend.

6. **Fire-and-forget para Módulo 3**: Publicar mensaje PedidoCreado de forma asíncrona (@Async). No bloquear respuesta al usuario. Si falla publicación, log error pero NO fallar creación de pedido.

7. **Compromiso parcial**: Si alguna línea no puede comprometer cantidad_solicitada completa, comprometer lo disponible y generar ALERTA (log structured + evento). Frontend debe mostrar warning al operario.

8. **Atomicidad del compromiso**: ComprometerInventarioUseCase debe ser transaccional. Si falla algún paso (reducir stock, crear movimiento), rollback completo. Pedido vuelve a ESPERANDO_RUTA para reintento.

9. **Performance del FEFO**: Query `SELECT lote WHERE sku_id = X AND cantidad > 0 ORDER BY fecha_vencimiento ASC` debe usar índice `idx_lotes_sku_vencimiento`. Cargar TODOS los lotes del SKU en memoria (raramente más de 10-20 lotes por SKU).

10. **Testing de integración crítico**: Mock Módulo Usuarios con WireMock. Simular timeouts, 5xx, circuit breaker. Test E2E del flujo completo: crear → asignar ruta → verificar compromiso FEFO.
