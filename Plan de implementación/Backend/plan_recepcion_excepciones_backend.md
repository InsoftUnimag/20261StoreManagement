# Plan de Implementación: Recepción de Mercancía y Excepciones - Backend

**Date**: 2026-04-03  
**Specs**: 04_registrar_recepcion.md · 16_registrar_excepcion.md  
**Module**: Módulo 1 - Inventario  
**Layer**: Backend  
**Priority**: P1 (Bloqueante para operación del almacén)

---

## Summary

Implementación del registro de recepción de mercancía contra manifiestos y gestión de excepciones de inventario. La recepción debe ser atómica (Lote + MovimientoInventario + actualización de stock en una sola transacción). Las excepciones registran anomalías (averías, vencimientos, diferencias, faltantes) para trazabilidad.

---

## Dependencies

**Blocked by**:
- Plan Gestión SKU Backend (requiere productos existentes)
- Setup BD (Phase 1 del plan maestro)

**Blocks**:
- Plan Consulta Inventario Backend (necesita lotes para calcular stock)
- Plan Pedidos Backend (necesita lotes disponibles)

**External Dependencies**:
- Manifiesto (entidad externa, cargada manualmente fuera del Módulo 1)
- DetalleManifiesto (líneas de manifiesto)

---

## Entities & Domain

### Manifiesto
**Propósito**: Documento de fábrica con productos esperados (entidad externa al Módulo 1).

**Atributos**:
- `manifiesto_id`: UUID (PK)
- `numero_manifiesto`: String (número del documento físico)
- `fecha_emision`: Date
- `proveedor`: String
- `estado`: Enum (Pendiente, Recepcionado Parcial, Recepcionado Total)

**Business Rules**:
- Módulo 1 solo LISTA manifiestos pendientes (no crea ni modifica)
- La carga de manifiestos es responsabilidad de otro sistema/módulo

### DetalleManifiesto
**Atributos**:
- `detalle_id`: UUID (PK)
- `manifiesto_id`: UUID (FK)
- `sku_id`: UUID (FK → Producto)
- `cantidad_esperada`: Integer
- `cantidad_recibida`: Integer (acumulado)

### Recepcion
**Propósito**: Registro de recepción física de mercancía.

**Atributos**:
- `recepcion_id`: UUID (PK, autogenerado)
- `manifiesto_id`: UUID (FK, puede ser null si recepción sin manifiesto)
- `operario_id`: UUID (FK → Usuario)
- `fecha_recepcion`: DateTime
- `notas`: Text (opcional)

**Business Rules**:
- La recepción debe ser ATOMICA - FR-021
- Una recepción puede cubrir múltiples líneas de manifiesto
- Si hay diferencias con manifiesto → crear ExcepcionInventario automáticamente

### Lote
**Propósito**: Unidad de trazabilidad con fecha de vencimiento para FEFO.

**Atributos**:
- `codigo_lote`: String (PK, Código de fábrica)
- `sku_id`: UUID (FK → Producto, inmutable)
- `cantidad`: Integer (Unidades físicas actuales)
- `fecha_vencimiento`: Date (Mandatorio para control FEFO)
- `fecha_expedicion`: Date
- `disponible`: Boolean
- `flag_urgencia_fefo`: Boolean (Activo si vencimiento cercano)
- `costo_unitario_producto`: Decimal (Costo en pesos colombianos)
- `recepcion_id`: UUID (FK → Recepcion)

**Constraints**:
- UNIQUE(sku_id, codigo_lote, fecha_vencimiento) - evita duplicados
- cantidad ≥ 0
- fecha_vencimiento >= fecha_fabricacion

**Business Rules**:
- Lotes con cantidad = 0 NO se eliminan (auditoría) - FR-026
- Lotes NO se comprometen al crear pedido, solo cuando Módulo 2 asigna ruta (FR-057, FR-058)
- FEFO (First Expired First Out) para selección de lotes - FR-061

### MovimientoInventario (Kardex)
**Propósito**: Registro contable de movimientos de stock para auditoría.

**Atributos**:
- `movimiento_id`: UUID (PK, autogenerado)
- `codigo_lote`: String (FK → Lote)
- `tipo_movimiento`: Enum (Entrada, Compromiso, Picking, Salida, Baja Avería, Baja Vencimiento, Faltante)
- `cantidad`: Integer (positivo o negativo según tipo)
- `fecha_movimiento`: DateTime
- `pedido_id`: UUID (FK → Pedido, nullable, solo para Compromiso/Picking/Salida)
- `excepcion_id`: UUID (FK → ExcepcionInventario, nullable, solo para Bajas/Faltantes)
- `operario_id`: UUID (FK → Usuario, nullable)
- `observaciones`: Text (opcional)

**Business Rules**:
- Cada cambio en cantidad de Lote genera un MovimientoInventario
- Tipos de movimiento:
  - **Entrada**: Recepción de mercancía (+)
  - **Compromiso**: Reserva de lote cuando Módulo 2 asigna ruta (-)
  - **Picking**: Confirmación de recolección (no cambia stock, ya estaba comprometido)
  - **Salida**: Confirmación de despacho (-)
  - **Baja Avería**: Descarte por daño (-)
  - **Baja Vencimiento**: Descarte por caducidad (-)
  - **Faltante**: Ajuste por diferencia de inventario (-)

### ExcepcionInventario
**Propósito**: Registro de anomalías para investigación y trazabilidad.

**Atributos**:
- `excepcion_id`: UUID (PK, autogenerado)
- `tipo_excepcion`: Enum (Avería, Vencimiento, Diferencia, Faltante)
- `codigo_lote`: String (FK → Lote, puede ser null)
- `sku_id`: UUID (FK → Producto, siempre presente)
- `cantidad_afectada`: Integer
- `fecha_registro`: DateTime
- `operario_id`: UUID (FK → Usuario)
- `descripcion`: Text (obligatorio, ej: "Pallet dañado en recepción")
- `evidencia_url`: String (nullable, foto/documento)
- `estado`: Enum (Abierta, En Investigación, Cerrada)

**Business Rules**:
- Toda excepción genera MovimientoInventario si reduce stock - FR-096
- Excepciones de tipo Diferencia se crean automáticamente si cantidad_recibida ≠ cantidad_esperada - FR-020
- Excepciones pueden originarse en: Recepción, Picking, Auditoría manual

---

## Use Cases

### UC-001: Registrar Recepción de Mercancía (Spec 04)

**Actor**: Operario de Almacén

**Flujo Principal**:
1. Operario selecciona manifiesto pendiente (o recepción sin manifiesto)
2. Para cada SKU recibido:
   - Ingresa: sku_id, codigo_lote, fecha_vencimiento, cantidad_recibida
3. Sistema valida:
   - sku_id existe en catálogo
   - cantidad_recibida > 0
   - fecha_vencimiento es futura
4. Sistema ejecuta transacción atómica:
   a. Crear Recepcion
   b. Para cada línea:
      - Crear Lote (o actualizar si ya existe)
      - Crear MovimientoInventario tipo Entrada
      - Incrementar Lote.cantidad
      - Actualizar DetalleManifiesto.cantidad_recibida
   c. Si cantidad_recibida ≠ cantidad_esperada → crear ExcepcionInventario tipo Diferencia
   d. Actualizar Manifiesto.estado (Parcial/Total según líneas completadas)
5. Sistema retorna confirmación con recepcion_id

**Business Logic**:
- **Atomicidad**: Si falla cualquier paso, rollback completo - FR-021
- **Detección automática de diferencias**: Si cantidad_recibida < cantidad_esperada → excepción Faltante - FR-020
- **UNIQUE constraint**: Sistema evita duplicar lotes (sku_id + codigo_lote + fecha_vencimiento)

**Performance**:
- Registro de recepción ≤ 10 segundos (incluso con 50 líneas) - SC-023

### UC-002: Listar Manifiestos Pendientes (Spec 03.5 recomendada)

**Actor**: Operario de Almacén

**Flujo**:
1. Operario solicita lista de manifiestos
2. Sistema consulta Manifiestos con estado = Pendiente o Recepcionado Parcial
3. Retorna lista con:
   - numero_manifiesto, proveedor, fecha_emision
   - Progreso: líneas completadas / líneas totales

**Business Logic**:
- Solo lectura (Módulo 1 no crea ni modifica manifiestos)
- Ordenar por fecha_emision ASC (más antiguos primero)

### UC-003: Registrar Excepción de Inventario (Spec 16)

**Actor**: Operario de Almacén, Supervisor

**Flujo Principal**:
1. Usuario detecta anomalía (avería, vencimiento, faltante)
2. Ingresa:
   - tipo_excepcion
   - sku_id (y opcionalmente codigo_lote)
   - cantidad_afectada
   - descripcion (obligatorio)
   - evidencia_url (opcional)
3. Sistema valida:
   - sku_id existe
   - Si codigo_lote presente → existe y cantidad >= cantidad_afectada
   - descripcion no vacía
4. Sistema ejecuta transacción:
   a. Crear ExcepcionInventario (estado: Abierta)
   b. Si tipo requiere baja de stock (Avería, Vencimiento, Faltante):
      - Crear MovimientoInventario tipo correspondiente
      - Decrementar Lote.cantidad
5. Retorna confirmación con excepcion_id

**Business Logic**:
- **Excepciones de tipo Diferencia**: Creadas automáticamente por UC-001 - FR-020
- **Excepciones de tipo Avería/Vencimiento**: Creadas manualmente - FR-094, FR-095
- **Excepciones de tipo Faltante**: Pueden ser automáticas (recepción) o manuales (auditoría)

**Validaciones**:
- No se permite baja de stock si lote no tiene cantidad suficiente - FR-096

### UC-004: Consultar Excepciones

**Actor**: Supervisor, Operario

**Flujo**:
1. Usuario solicita lista de excepciones (filtros: tipo, estado, fecha)
2. Sistema retorna excepciones con información completa:
   - Datos de excepción
   - Producto y lote afectados
   - Operario que registró
3. Usuario puede ver detalle de excepción_id específica

---

## Endpoints / API

### POST /api/v1/recepciones
**Purpose**: Registrar recepción de mercancía

**Request Body**:
```json
{
  "manifiesto_id": "uuid", // opcional (null si recepción sin manifiesto)
  "operario_id": "uuid",
  "lineas_recepcion": [
    {
      "sku_id": "uuid",
      "codigo_lote": "LOT-2025-001",
      "fecha_vencimiento": "2026-12-31",
      "fecha_fabricacion": "2025-01-15", // opcional
      "cantidad_recibida": 240
    }
  ],
  "notas": "Recepción completa sin novedades" // opcional
}
```

**Response 201 Created**:
```json
{
  "recepcion_id": "uuid",
  "fecha_recepcion": "2026-04-03T10:30:00Z",
  "lotes_creados": [
    {
      "codigo_lote": "LOT-2026-001",
      "sku_id": "uuid",
      "codigo_lote": "LOT-2025-001",
      "cantidad": 240
    }
  ],
  "excepciones_generadas": [ // si hubo diferencias
    {
      "excepcion_id": "uuid",
      "tipo": "Diferencia",
      "cantidad_afectada": -10,
      "descripcion": "Diferencia automática: Esperado 250, Recibido 240"
    }
  ]
}
```

**Validations**:
- 400 Bad Request: sku_id no existe, cantidad ≤ 0, fecha_vencimiento en pasado
- 409 Conflict: Lote duplicado (mismo sku + codigo_lote + fecha_vencimiento)
- 500 Internal Server Error: Fallo en transacción atómica

### GET /api/v1/manifiestos/pendientes
**Purpose**: Listar manifiestos pendientes de recepción

**Query Params**:
- `proveedor` (opcional): Filtrar por proveedor
- `fecha_desde`, `fecha_hasta` (opcional): Rango de fechas

**Response 200 OK**:
```json
{
  "manifiestos": [
    {
      "manifiesto_id": "uuid",
      "numero_manifiesto": "MAN-2025-042",
      "proveedor": "Bavaria",
      "fecha_emision": "2025-03-28",
      "estado": "Pendiente",
      "progreso": {
        "lineas_completadas": 0,
        "lineas_totales": 12
      }
    }
  ]
}
```

### GET /api/v1/manifiestos/{id}/detalles
**Purpose**: Ver líneas de un manifiesto para recepción

**Response 200 OK**:
```json
{
  "manifiesto_id": "uuid",
  "numero_manifiesto": "MAN-2025-042",
  "lineas": [
    {
      "detalle_id": "uuid",
      "sku": {
        "sku_id": "uuid",
        "marca": "Pilsen",
        "presentacion": "Six-pack"
      },
      "cantidad_esperada": 100,
      "cantidad_recibida": 0
    }
  ]
}
```

### POST /api/v1/excepciones
**Purpose**: Registrar excepción de inventario

**Request Body**:
```json
{
  "tipo_excepcion": "Avería", // Avería | Vencimiento | Diferencia | Faltante
  "sku_id": "uuid",
  "codigo_lote": "LOT-2026-001", // opcional
  "cantidad_afectada": 12,
  "descripcion": "Cajas dañadas por humedad durante transporte",
  "evidencia_url": "https://storage/foto123.jpg", // opcional
  "operario_id": "uuid"
}
```

**Response 201 Created**:
```json
{
  "excepcion_id": "uuid",
  "tipo_excepcion": "Avería",
  "estado": "Abierta",
  "fecha_registro": "2026-04-03T11:15:00Z",
  "movimiento_generado": {
    "movimiento_id": "uuid",
    "tipo_movimiento": "Baja Avería",
    "cantidad": -12
  }
}
```

**Validations**:
- 400 Bad Request: descripcion vacía, cantidad ≤ 0
- 404 Not Found: sku_id o codigo_lote no existen
- 409 Conflict: Stock insuficiente en lote para dar de baja

### GET /api/v1/excepciones
**Purpose**: Consultar excepciones registradas

**Query Params**:
- `tipo` (opcional): Filtrar por tipo_excepcion
- `estado` (opcional): Abierta | En Investigación | Cerrada
- `fecha_desde`, `fecha_hasta` (opcional)
- `sku_id` (opcional)
- `page`, `size` (paginación)

**Response 200 OK**:
```json
{
  "excepciones": [
    {
      "excepcion_id": "uuid",
      "tipo_excepcion": "Avería",
      "sku": {
        "sku_id": "uuid",
        "marca": "Pilsen",
        "presentacion": "Unidad"
      },
      "codigo_lote": "LOT-2026-001",
      "cantidad_afectada": 12,
      "fecha_registro": "2026-04-03T11:15:00Z",
      "operario_nombre": "Juan Pérez",
      "estado": "Abierta"
    }
  ],
  "pagination": {
    "total_elements": 45,
    "total_pages": 5,
    "current_page": 0
  }
}
```

### GET /api/v1/excepciones/{id}
**Purpose**: Ver detalle completo de una excepción

**Response 200 OK**:
```json
{
  "excepcion_id": "uuid",
  "tipo_excepcion": "Avería",
  "sku": {
    "sku_id": "uuid",
    "marca": "Pilsen",
    "presentacion": "Unidad",
    "contenido_ml": 330
  },
  "lote": {
    "codigo_lote": "LOT-2026-001",
    "codigo_lote": "LOT-2025-001",
    "fecha_vencimiento": "2026-12-31",
    "cantidad": 228 // después de la baja
  },
  "cantidad_afectada": 12,
  "descripcion": "Cajas dañadas por humedad durante transporte",
  "evidencia_url": "https://storage/foto123.jpg",
  "fecha_registro": "2026-04-03T11:15:00Z",
  "operario": {
    "operario_id": "uuid",
    "nombre": "Juan Pérez"
  },
  "estado": "Abierta",
  "movimiento_asociado": {
    "movimiento_id": "uuid",
    "tipo_movimiento": "Baja Avería",
    "cantidad": -12,
    "fecha": "2026-04-03T11:15:00Z"
  }
}
```

### PATCH /api/v1/excepciones/{id}/estado
**Purpose**: Cambiar estado de excepción (para workflow de investigación)

**Request Body**:
```json
{
  "nuevo_estado": "En Investigación", // En Investigación | Cerrada
  "observaciones": "Contactado proveedor para reclamo"
}
```

**Response 200 OK**:
```json
{
  "excepcion_id": "uuid",
  "estado": "En Investigación",
  "fecha_actualizacion": "2026-04-03T14:00:00Z"
}
```

---

## Implementation Tasks

### Phase 1: Domain Entities (Backend)

**T001: Crear entidad Manifiesto (domain)**
- Path: `domain/entities/Manifiesto.java`
- Atributos: manifiesto_id, numero_manifiesto, fecha_emision, proveedor, estado
- Enum EstadoManifiesto: PENDIENTE, RECEPCIONADO_PARCIAL, RECEPCIONADO_TOTAL
- Sin anotaciones JPA (domain puro)

**T002: Crear entidad DetalleManifiesto (domain)**
- Path: `domain/entities/DetalleManifiesto.java`
- Atributos: detalle_id, manifiesto_id, sku_id, cantidad_esperada, cantidad_recibida
- Relación: FK a Manifiesto y Producto

**T003: Crear entidad Recepcion (domain)**
- Path: `domain/entities/Recepcion.java`
- Atributos: recepcion_id, manifiesto_id (nullable), operario_id, fecha_recepcion, notas
- Validación: fecha_recepcion no puede ser futura

**T004: Crear entidad Lote (domain)**
- Path: `domain/entities/Lote.java`
- Atributos: codigo_lote, sku_id, cantidad, fecha_vencimiento, fecha_expedicion, disponible, flag_urgencia_fefo, costo_unitario_producto, recepcion_id
- Validaciones:
  - cantidad >= 0
  - fecha_vencimiento >= fecha_fabricacion
  - sku_id, fecha_vencimiento, cantidad_inicial son inmutables
- Método: `reducirStock(int cantidad)` con validación

**T005: Crear entidad MovimientoInventario (domain)**
- Path: `domain/entities/MovimientoInventario.java`
- Atributos: movimiento_id, codigo_lote, tipo_movimiento, cantidad, fecha_movimiento, pedido_id, excepcion_id, operario_id, observaciones
- Enum TipoMovimiento: ENTRADA, COMPROMISO, PICKING, SALIDA, BAJA_AVERIA, BAJA_VENCIMIENTO, FALTANTE

**T006: Crear entidad ExcepcionInventario (domain)**
- Path: `domain/entities/ExcepcionInventario.java`
- Atributos: excepcion_id, tipo_excepcion, codigo_lote, sku_id, cantidad_afectada, fecha_registro, operario_id, descripcion, evidencia_url, estado
- Enum TipoExcepcion: AVERIA, VENCIMIENTO, DIFERENCIA, FALTANTE
- Enum EstadoExcepcion: ABIERTA, EN_INVESTIGACION, CERRADA
- Validación: descripcion no puede estar vacía

### Phase 2: Repository Interfaces

**T007: Crear ManifiestoRepository (domain/repositories)**
- Métodos:
  - `List<Manifiesto> findByEstado(EstadoManifiesto estado)`
  - `List<Manifiesto> findPendientes()` (estado IN [PENDIENTE, RECEPCIONADO_PARCIAL])
  - `Optional<Manifiesto> findById(UUID id)`
  - `void save(Manifiesto manifiesto)` // solo para actualizar estado

**T008: Crear DetalleManifiestoRepository (domain/repositories)**
- Métodos:
  - `List<DetalleManifiesto> findByManifiestoId(UUID manifiestoId)`
  - `void save(DetalleManifiesto detalle)` // para actualizar cantidad_recibida

**T009: Crear RecepcionRepository (domain/repositories)**
- Métodos:
  - `UUID save(Recepcion recepcion)` retorna ID
  - `Optional<Recepcion> findById(UUID id)`
  - `List<Recepcion> findByFechaRange(LocalDate desde, LocalDate hasta)`

**T010: Crear LoteRepository (domain/repositories)**
- Métodos:
  - `UUID save(Lote lote)` retorna ID
  - `Optional<Lote> findById(UUID id)`
  - `Optional<Lote> findBySkuAndCodigoAndVencimiento(UUID skuId, String codigoLote, LocalDate fechaVencimiento)`
  - `List<Lote> findBySkuIdOrderByFechaVencimientoAsc(UUID skuId)` // FEFO
  - `List<Lote> findBySkuIdWithStock(UUID skuId)` // cantidad > 0
  - `void update(Lote lote)` // para actualizar cantidad

**T011: Crear MovimientoInventarioRepository (domain/repositories)**
- Métodos:
  - `UUID save(MovimientoInventario movimiento)`
  - `List<MovimientoInventario> findBycodigoLote(UUID codigoLote)` // kardex de un lote
  - `List<MovimientoInventario> findByPedidoId(UUID pedidoId)` // movimientos de un pedido

**T012: Crear ExcepcionInventarioRepository (domain/repositories)**
- Métodos:
  - `UUID save(ExcepcionInventario excepcion)`
  - `Optional<ExcepcionInventario> findById(UUID id)`
  - `Page<ExcepcionInventario> findByFilters(TipoExcepcion tipo, EstadoExcepcion estado, LocalDate desde, LocalDate hasta, UUID skuId, Pageable pageable)`
  - `void update(ExcepcionInventario excepcion)` // para cambiar estado

### Phase 3: Use Cases (Application Layer)

**T013: Implementar RegistrarRecepcionUseCase**
- Path: `application/usecases/RegistrarRecepcionUseCase.java`
- Input DTO: `RegistrarRecepcionCommand` (manifiesto_id, operario_id, lineas, notas)
- Output DTO: `RecepcionResult` (recepcion_id, lotes_creados, excepciones_generadas)
- Lógica:
  1. Validar que todos los sku_id existen (ProductoRepository)
  2. Iniciar transacción
  3. Crear Recepcion
  4. Para cada línea:
     - Buscar si lote ya existe (sku + codigo + vencimiento)
     - Si existe: incrementar cantidad
     - Si no: crear nuevo Lote
     - Crear MovimientoInventario tipo ENTRADA
     - Si manifiesto_id presente: actualizar DetalleManifiesto.cantidad_recibida
     - Si cantidad_recibida ≠ cantidad_esperada: crear ExcepcionInventario tipo DIFERENCIA
  5. Actualizar Manifiesto.estado (calcular si es Parcial o Total)
  6. Commit transacción
- **Atomicidad crítica**: Usar @Transactional, cualquier fallo hace rollback - FR-021

**T014: Implementar ListarManifiestosPendientesUseCase**
- Path: `application/usecases/ListarManifiestosPendientesUseCase.java`
- Input: Filtros opcionales (proveedor, fecha_desde, fecha_hasta)
- Output: Lista de ManifiestoDTO con progreso (lineas_completadas / lineas_totales)
- Lógica:
  1. Consultar ManifiestoRepository.findPendientes()
  2. Para cada manifiesto: calcular progreso desde DetalleManifiesto
  3. Ordenar por fecha_emision ASC

**T015: Implementar ConsultarDetallesManifiestoUseCase**
- Path: `application/usecases/ConsultarDetallesManifiestoUseCase.java`
- Input: manifiesto_id
- Output: ManifiestoDetalleDTO con líneas (detalle_id, sku, cantidad_esperada, cantidad_recibida)
- Lógica:
  1. Buscar Manifiesto (404 si no existe)
  2. Buscar DetalleManifiesto por manifiesto_id
  3. Join con Producto para incluir marca/presentacion

**T016: Implementar RegistrarExcepcionUseCase**
- Path: `application/usecases/RegistrarExcepcionUseCase.java`
- Input DTO: `RegistrarExcepcionCommand` (tipo, sku_id, codigo_lote, cantidad, descripcion, evidencia_url, operario_id)
- Output DTO: `ExcepcionResult` (excepcion_id, movimiento_generado_id)
- Lógica:
  1. Validar sku_id existe (ProductoRepository)
  2. Si codigo_lote presente: validar existe y cantidad >= cantidad_afectada
  3. Iniciar transacción
  4. Crear ExcepcionInventario (estado: ABIERTA)
  5. Si tipo IN [AVERIA, VENCIMIENTO, FALTANTE]:
     - Crear MovimientoInventario (tipo correspondiente)
     - Reducir Lote.cantidad
  6. Commit transacción

**T017: Implementar ConsultarExcepcionesUseCase**
- Path: `application/usecases/ConsultarExcepcionesUseCase.java`
- Input: Filtros (tipo, estado, fecha_desde, fecha_hasta, sku_id, page, size)
- Output: Page<ExcepcionDTO>
- Lógica: Consultar ExcepcionInventarioRepository con filtros

**T018: Implementar ConsultarDetalleExcepcionUseCase**
- Path: `application/usecases/ConsultarDetalleExcepcionUseCase.java`
- Input: excepcion_id
- Output: ExcepcionDetalleDTO (incluye sku, lote, operario, movimiento asociado)
- Lógica: Joins con Producto, Lote, Usuario, MovimientoInventario

**T019: Implementar CambiarEstadoExcepcionUseCase**
- Path: `application/usecases/CambiarEstadoExcepcionUseCase.java`
- Input: excepcion_id, nuevo_estado, observaciones
- Output: ExcepcionDTO actualizada
- Validación: Solo permitir transiciones válidas (Abierta → En Investigación → Cerrada)

### Phase 4: Infrastructure (JPA, DB)

**T020: Crear schema SQL con Flyway**
- Path: `infrastructure/db/migrations/V004__create_recepciones_excepciones.sql`
- Tablas:
  - `manifiestos` (PK: manifiesto_id, UNIQUE: numero_manifiesto)
  - `detalles_manifiesto` (PK: detalle_id, FK: manifiesto_id, sku_id)
  - `recepciones` (PK: recepcion_id, FK: manifiesto_id, operario_id)
  - `lotes` (PK: codigo_lote, FK: sku_id, recepcion_id, UNIQUE(sku_id, codigo_lote, fecha_vencimiento))
  - `movimientos_inventario` (PK: movimiento_id, FK: codigo_lote, pedido_id, excepcion_id, operario_id)
  - `excepciones_inventario` (PK: excepcion_id, FK: codigo_lote, sku_id, operario_id)
- Índices:
  - `idx_lotes_sku_vencimiento` ON lotes(sku_id, fecha_vencimiento) // FEFO
  - `idx_lotes_stock` ON lotes(sku_id, cantidad) WHERE cantidad > 0
  - `idx_movimientos_lote` ON movimientos_inventario(codigo_lote)
  - `idx_excepciones_estado` ON excepciones_inventario(estado)

**T021: Implementar JPA entities (infrastructure/persistence)**
- ManifiestoEntity.java (con @Entity, @Table, @Column)
- DetalleManifiestoEntity.java
- RecepcionEntity.java
- LoteEntity.java
- MovimientoInventarioEntity.java
- ExcepcionInventarioEntity.java
- Usar @Enumerated(EnumType.STRING) para enums

**T022: Implementar JPA Repositories**
- JpaManifiestoRepository extends JpaRepository
- JpaDetalleManifiestoRepository
- JpaRecepcionRepository
- JpaLoteRepository con query FEFO:
  ```java
  @Query("SELECT l FROM LoteEntity l WHERE l.skuId = :skuId AND l.cantidad > 0 ORDER BY l.fechaVencimiento ASC")
  List<LoteEntity> findAvailableBySkuOrderByFEFO(@Param("skuId") UUID skuId);
  ```
- JpaMovimientoInventarioRepository
- JpaExcepcionInventarioRepository con Specification para filtros dinámicos

**T023: Implementar Adapters (infrastructure/adapters)**
- ManifiestoRepositoryImpl implements ManifiestoRepository
- RecepcionRepositoryImpl implements RecepcionRepository
- LoteRepositoryImpl implements LoteRepository
- MovimientoInventarioRepositoryImpl implements MovimientoInventarioRepository
- ExcepcionInventarioRepositoryImpl implements ExcepcionInventarioRepository
- Mappers: Entity ↔ Domain (usar MapStruct o manual)

### Phase 5: REST Controllers

**T024: Implementar RecepcionController**
- Path: `infrastructure/web/controllers/RecepcionController.java`
- POST /api/v1/recepciones → llama RegistrarRecepcionUseCase
- Validaciones con @Valid, Jakarta Validation
- Manejo de errores:
  - 400: Validación fallida
  - 404: SKU no encontrado
  - 409: Lote duplicado
  - 500: Fallo de transacción

**T025: Implementar ManifiestoController**
- Path: `infrastructure/web/controllers/ManifiestoController.java`
- GET /api/v1/manifiestos/pendientes → ListarManifiestosPendientesUseCase
- GET /api/v1/manifiestos/{id}/detalles → ConsultarDetallesManifiestoUseCase
- Respuestas 200 OK con DTOs

**T026: Implementar ExcepcionController**
- Path: `infrastructure/web/controllers/ExcepcionController.java`
- POST /api/v1/excepciones → RegistrarExcepcionUseCase
- GET /api/v1/excepciones → ConsultarExcepcionesUseCase (con filtros)
- GET /api/v1/excepciones/{id} → ConsultarDetalleExcepcionUseCase
- PATCH /api/v1/excepciones/{id}/estado → CambiarEstadoExcepcionUseCase
- Validaciones:
  - 400: Descripción vacía, cantidad <= 0
  - 404: Excepción no encontrada
  - 409: Stock insuficiente para baja

### Phase 6: Testing

**T027: Unit tests - Domain entities**
- Test: Lote.reducirStock() lanza excepción si stock insuficiente
- Test: ExcepcionInventario valida descripción no vacía
- Test: MovimientoInventario valida tipos permitidos

**T028: Unit tests - Use Cases**
- Test RegistrarRecepcionUseCase:
  - Happy path: Recepción sin manifiesto crea lotes correctamente
  - Happy path: Recepción con manifiesto actualiza DetalleManifiesto
  - Edge case: Diferencia de cantidad genera ExcepcionInventario automática
  - Error case: Rollback si falla creación de MovimientoInventario
- Test RegistrarExcepcionUseCase:
  - Happy path: Excepción tipo Avería reduce stock y crea movimiento
  - Error case: No permite baja si stock insuficiente

**T029: Integration tests - Controllers**
- Test POST /api/v1/recepciones:
  - Recepción completa retorna 201 con lotes creados
  - Diferencia con manifiesto genera excepción automática (verificar en DB)
  - Validación: Fecha vencimiento en pasado retorna 400
- Test GET /api/v1/manifiestos/pendientes:
  - Lista solo manifiestos con estado Pendiente o Parcial
  - Progreso calcula correctamente líneas completadas
- Test POST /api/v1/excepciones:
  - Excepción tipo Avería crea MovimientoInventario y reduce stock
  - Validación: Stock insuficiente retorna 409

**T030: Integration tests - Transaction Atomicity**
- Test crítico: Si falla actualización de Manifiesto.estado, toda la recepción hace rollback
- Test: Si falla creación de MovimientoInventario, la ExcepcionInventario no se crea

---

## Tests

### Unit Tests
- Domain entities: 8 tests
- Use Cases: 15 tests (incluyendo rollback scenarios)

### Integration Tests
- Controllers: 12 tests
- DB transactions: 4 tests (atomicidad crítica)

### Performance Tests
- Recepción con 50 líneas debe completar en ≤ 10 seg - SC-023
- Consulta de manifiestos pendientes ≤ 2 seg

---

## Acceptance Criteria

**FR-020**: Sistema detecta automáticamente diferencias entre cantidad esperada (manifiesto) y recibida, creando ExcepcionInventario tipo Diferencia.

**FR-021**: Recepción es atómica: Lote + MovimientoInventario + stock actualizado en una sola transacción. Si falla algún paso, rollback completo.

**FR-026**: Lotes con cantidad = 0 NO se eliminan de la base de datos (auditoría).

**FR-094**: Operarios pueden registrar excepciones tipo Avería con descripción obligatoria.

**FR-095**: Operarios pueden registrar excepciones tipo Vencimiento para dar de baja productos caducados.

**FR-096**: Toda excepción que reduce stock genera automáticamente MovimientoInventario correspondiente.

**SC-023**: Registro de recepción con 50 líneas completa en ≤ 10 segundos.

---

## Notes & Best Practices

1. **Atomicidad es crítica**: Usar @Transactional(propagation = REQUIRED) en RegistrarRecepcionUseCase y RegistrarExcepcionUseCase. Configurar rollbackFor = Exception.class.

2. **UNIQUE constraint en Lotes**: La combinación (sku_id, codigo_lote, fecha_vencimiento) debe ser única. Si se recibe el mismo lote en recepciones diferentes, incrementar cantidad del lote existente, NO crear duplicado.

3. **Validación de fecha_vencimiento**: Debe ser fecha futura (> hoy). Validar en domain entity y en controller.

4. **Descripción obligatoria en Excepciones**: La descripción es SIEMPRE obligatoria para trazabilidad. Validar en domain y API (no puede ser null ni vacía).

5. **Manifiestos son read-only en Módulo 1**: Solo se actualiza el estado y la cantidad_recibida en DetalleManifiesto. La creación de manifiestos es responsabilidad de otro sistema.

6. **MovimientoInventario como auditoría**: Cada cambio en cantidad debe generar un registro en MovimientoInventario. Es el kardex completo del almacén.

7. **FEFO preparado**: Aunque el algoritmo FEFO se usa principalmente en pedidos (otro plan), el query `findBySkuIdOrderByFechaVencimientoAsc` ya está disponible en LoteRepository.

8. **Excepciones automáticas vs manuales**:
   - Automáticas: Tipo Diferencia (creadas por RegistrarRecepcionUseCase)
   - Manuales: Tipo Avería, Vencimiento, Faltante (creadas por RegistrarExcepcionUseCase)

9. **Performance**: Usar índices en lotes(sku_id, fecha_vencimiento) y lotes(sku_id, cantidad) para queries FEFO rápidas.

10. **Testing de rollback**: Es crítico probar que si falla cualquier paso de la recepción, toda la transacción hace rollback. Simular fallo de base de datos en medio de la transacción.
