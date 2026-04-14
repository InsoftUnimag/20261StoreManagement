# Plan de Implementación: Picking y Despacho - Backend

**Date**: 2026-04-03  
**Specs**: 11_consultar_pedidos_picking.md · 12_confirmar_picking.md · 13_listar_pedidos_despacho.md · 14_confirmar_despacho.md  
**Module**: Módulo 1 - Inventario  
**Layer**: Backend  
**Priority**: P1 (Operación crítica del almacén)

---

## Summary

Implementación de procesos operativos de almacén: picking (recolección de productos) y despacho (salida de mercancía). Los operarios consultan pedidos comprometidos, confirman recolección (sin cambiar stock, ya estaba comprometido), y confirman despacho (reduce stock, genera MovimientoInventario tipo Salida). Estados: Comprometido → En Picking → Despachado.

---

## Dependencies

**Blocked by**:
- Plan Pedidos Backend (requiere pedidos en estado Comprometido)
- Plan Recepción Backend (requiere entidades Lote, MovimientoInventario)

**Blocks**:
- Plan Frontend Picking/Despacho (consume estos endpoints)

**External Dependencies**:
- Ninguna (operaciones internas del almacén)

---

## Entities & Domain

### RegistroPicking
**Propósito**: Registro de confirmación de recolección de productos.

**Atributos**:
- `registro_picking_id`: UUID (PK, autogenerado)
- `pedido_id`: UUID (FK → Pedido)
- `operario_id`: UUID (FK → Usuario)
- `fecha_picking`: DateTime
- `observaciones`: Text (opcional)

**Business Rules**:
- Un pedido puede tener solo 1 RegistroPicking (ONE-TO-ONE con Pedido)
- Picking NO cambia stock (ya estaba comprometido) - FR-068
- Pedido debe estar en estado Comprometido para confirmar picking - FR-067

### RegistroDespacho
**Propósito**: Registro de confirmación de salida de mercancía.

**Atributos**:
- `registro_despacho_id`: UUID (PK, autogenerado)
- `pedido_id`: UUID (FK → Pedido)
- `operario_id`: UUID (FK → Usuario)
- `fecha_despacho`: DateTime
- `transportista`: String (nombre del transportista)
- `placa_vehiculo`: String (opcional)
- `observaciones`: Text (opcional)

**Business Rules**:
- Un pedido puede tener solo 1 RegistroDespacho (ONE-TO-ONE con Pedido)
- Despacho reduce stock de lotes comprometidos - FR-071
- Despacho genera MovimientoInventario tipo SALIDA para cada lote - FR-071
- Pedido debe estar en estado En Picking para confirmar despacho - FR-070

### Pedido (extensión)
Nuevos estados y transiciones:
- **En Picking**: Operario confirmó recolección de productos
- **Despachado**: Operario confirmó salida de mercancía

### MovimientoInventario (extensión)
Nuevo tipo de movimiento:
- **Salida**: Despacho de mercancía (reduce cantidad)

---

## Use Cases

### UC-001: Consultar Pedidos para Picking (Spec 11)

**Actor**: Operario de Picking

**Flujo**:
1. Operario solicita lista de pedidos listos para picking
2. Sistema consulta Pedidos con estado = Comprometido
3. Sistema retorna lista ordenada por fecha_compromiso ASC (FIFO: más antiguos primero)
4. Para cada pedido incluye:
   - Datos del pedido (numero_pedido, cliente, fecha)
   - Líneas con lotes comprometidos (para facilitar recolección)
   - Ubicación de lotes (si existe campo, fuera de alcance Fase 1)

**Business Logic**:
- Solo mostrar pedidos en estado Comprometido - FR-067
- Ordenar por fecha_compromiso ASC (FIFO)
- Incluir detalle de lotes comprometidos para que operario sepa qué recolectar

**Performance**:
- Consulta de pedidos para picking ≤ 2 seg

### UC-002: Confirmar Picking (Spec 12)

**Actor**: Operario de Picking

**Flujo Principal**:
1. Operario selecciona pedido_id
2. Sistema valida:
   - Pedido existe
   - Pedido está en estado Comprometido
3. Sistema ejecuta transacción:
   a. Crear RegistroPicking (operario_id, fecha_picking)
   b. Actualizar Pedido.estado = En Picking
   c. NO modificar Lote.cantidad (ya estaba comprometido) - FR-068
   d. NO crear MovimientoInventario (el movimiento fue el Compromiso)
4. Sistema retorna confirmación

**Business Logic**:
- **Picking NO cambia stock**: El stock ya se redujo en el Compromiso - FR-068
- Picking es solo un cambio de estado para tracking operativo
- Tiempo límite: Picking debe completarse en ≤ 5 min desde compromiso - SC-035 (validación en frontend, alerta en backend)

**Validations**:
- Pedido no existe → 404
- Pedido no está en Comprometido → 409 con mensaje "Pedido no está listo para picking"

**Performance**:
- Confirmación de picking ≤ 5 segundos

### UC-003: Listar Pedidos para Despacho (Spec 13)

**Actor**: Operario de Despacho

**Flujo**:
1. Operario solicita lista de pedidos listos para despacho
2. Sistema consulta Pedidos con estado = En Picking
3. Sistema retorna lista ordenada por fecha_picking ASC (FIFO)
4. Para cada pedido incluye:
   - Datos del pedido (numero_pedido, cliente, dirección)
   - Ruta asignada (ruta_id del Módulo 2)
   - Total de unidades a despachar
   - Tiempo desde picking (para alertas si > 5 min)

**Business Logic**:
- Solo mostrar pedidos en estado En Picking - FR-070
- Ordenar por fecha_picking ASC (FIFO)
- Calcular tiempo_desde_picking = now - RegistroPicking.fecha_picking
- Alerta si tiempo_desde_picking > 5 min (SC-035)

**Performance**:
- Consulta de pedidos para despacho ≤ 2 seg

### UC-004: Confirmar Despacho (Spec 14)

**Actor**: Operario de Despacho

**Flujo Principal**:
1. Operario ingresa:
   - pedido_id
   - transportista (nombre)
   - placa_vehiculo (opcional)
   - observaciones (opcional)
2. Sistema valida:
   - Pedido existe
   - Pedido está en estado En Picking
3. Sistema ejecuta transacción atómica:
   a. Buscar LoteComprometidos del pedido
   b. Para cada LoteComprometido:
      - Reducir Lote.cantidad (cantidad_comprometida)
      - Crear MovimientoInventario tipo SALIDA (cantidad negativa)
   c. Crear RegistroDespacho
   d. Actualizar Pedido.estado = Despachado
4. Sistema retorna confirmación

**Business Logic**:
- **Despacho reduce stock**: Lote.cantidad -= cantidad_comprometida - FR-071
- **Kardex completo**: Cada lote genera MovimientoInventario tipo SALIDA - FR-071
- **Atomicidad crítica**: Si falla reducción de stock o creación de movimiento, rollback completo
- Validar que Lote.cantidad >= cantidad_comprometida (no debería fallar si compromiso fue correcto, pero validar por seguridad)

**Validations**:
- Pedido no existe → 404
- Pedido no está en En Picking → 409 con mensaje "Pedido no está listo para despacho"
- Transportista vacío → 400
- Stock inconsistente (no debería ocurrir) → 500 con rollback

**Performance**:
- Confirmación de despacho (con 20 líneas, 50 lotes) ≤ 10 seg

### UC-005: Consultar Detalle de Picking/Despacho

**Actor**: Supervisor, Operario

**Flujo**:
1. Usuario solicita detalle de RegistroPicking o RegistroDespacho por pedido_id
2. Sistema retorna:
   - Datos del registro (fecha, operario, observaciones)
   - Datos del pedido asociado
   - Líneas y lotes (para contexto)

**Business Logic**:
- Usada para auditoría y seguimiento
- Mostrar operario que realizó picking/despacho

---

## Endpoints / API

### GET /api/v1/picking/pedidos
**Purpose**: Listar pedidos listos para picking (estado Comprometido)

**Query Params**:
- `page` (default: 0), `size` (default: 20)

**Response 200 OK**:
```json
{
  "pedidos": [
    {
      "pedido_id": "uuid",
      "numero_pedido": "PED-20260403-001",
      "cliente": {
        "cedula": "1234567890",
        "nombre": "Juan Pérez",
        "direccion": "Calle 123 #45-67, Bogotá"
      },
      "ruta_id": "uuid",
      "fecha_compromiso": "2026-04-03T11:00:00Z",
      "total_unidades": 360,
      "lineas": [
        {
          "sku": {
            "sku_id": "uuid",
            "marca": "Pilsen",
            "presentacion": "Six-pack"
          },
          "cantidad": 120,
          "lotes_comprometidos": [
            {
              "codigo_lote": "LOT-2026-001",
              "codigo_lote": "LOT-2025-001",
              "cantidad": 80,
              "ubicacion": "Pasillo A-05" // opcional, fuera de alcance
            },
            {
              "codigo_lote": "LOT-2026-001",
              "codigo_lote": "LOT-2025-020",
              "cantidad": 40,
              "ubicacion": "Pasillo B-12"
            }
          ]
        }
      ]
    }
  ],
  "pagination": {
    "total_elements": 8,
    "total_pages": 1,
    "current_page": 0
  }
}
```

### POST /api/v1/picking/confirmar
**Purpose**: Confirmar picking de un pedido

**Request Body**:
```json
{
  "pedido_id": "uuid",
  "operario_id": "uuid",
  "observaciones": "Picking completo sin novedades" // opcional
}
```

**Response 200 OK**:
```json
{
  "registro_picking_id": "uuid",
  "pedido_id": "uuid",
  "numero_pedido": "PED-20260403-001",
  "estado_anterior": "Comprometido",
  "estado_actual": "En Picking",
  "fecha_picking": "2026-04-03T11:15:00Z",
  "operario": {
    "operario_id": "uuid",
    "nombre": "Carlos Ramírez"
  }
}
```

**Response 409 Conflict** (pedido no está en Comprometido):
```json
{
  "error": "ESTADO_INVALIDO",
  "message": "El pedido no está listo para picking. Estado actual: En Picking"
}
```

**Validations**:
- 400: pedido_id o operario_id vacíos
- 404: Pedido no encontrado
- 409: Pedido no en estado Comprometido

### GET /api/v1/despacho/pedidos
**Purpose**: Listar pedidos listos para despacho (estado En Picking)

**Query Params**:
- `page` (default: 0), `size` (default: 20)

**Response 200 OK**:
```json
{
  "pedidos": [
    {
      "pedido_id": "uuid",
      "numero_pedido": "PED-20260403-001",
      "cliente": {
        "cedula": "1234567890",
        "nombre": "Juan Pérez",
        "direccion": "Calle 123 #45-67, Bogotá"
      },
      "ruta_id": "uuid",
      "fecha_picking": "2026-04-03T11:15:00Z",
      "tiempo_desde_picking_minutos": 3,
      "alerta_tiempo": false, // true si > 5 min
      "total_unidades": 360,
      "operario_picking": "Carlos Ramírez"
    }
  ],
  "pagination": {
    "total_elements": 5,
    "total_pages": 1,
    "current_page": 0
  }
}
```

### POST /api/v1/despacho/confirmar
**Purpose**: Confirmar despacho de un pedido

**Request Body**:
```json
{
  "pedido_id": "uuid",
  "operario_id": "uuid",
  "transportista": "Transportes ABC S.A.S.",
  "placa_vehiculo": "ABC-123", // opcional
  "observaciones": "Despacho completo" // opcional
}
```

**Response 200 OK**:
```json
{
  "registro_despacho_id": "uuid",
  "pedido_id": "uuid",
  "numero_pedido": "PED-20260403-001",
  "estado_anterior": "En Picking",
  "estado_actual": "Despachado",
  "fecha_despacho": "2026-04-03T11:20:00Z",
  "operario": {
    "operario_id": "uuid",
    "nombre": "María González"
  },
  "transportista": "Transportes ABC S.A.S.",
  "movimientos_generados": [
    {
      "movimiento_id": "uuid",
      "codigo_lote": "LOT-2026-001",
      "tipo": "Salida",
      "cantidad": -80
    },
    {
      "movimiento_id": "uuid",
      "codigo_lote": "LOT-2026-001",
      "tipo": "Salida",
      "cantidad": -40
    }
  ]
}
```

**Response 409 Conflict** (pedido no está en En Picking):
```json
{
  "error": "ESTADO_INVALIDO",
  "message": "El pedido no está listo para despacho. Estado actual: Despachado"
}
```

**Validations**:
- 400: pedido_id, operario_id o transportista vacíos
- 404: Pedido no encontrado
- 409: Pedido no en estado En Picking
- 500: Stock inconsistente (Lote.cantidad < cantidad_comprometida)

### GET /api/v1/picking/{pedido_id}/detalle
**Purpose**: Consultar detalle de registro de picking

**Response 200 OK**:
```json
{
  "registro_picking_id": "uuid",
  "pedido": {
    "pedido_id": "uuid",
    "numero_pedido": "PED-20260403-001"
  },
  "fecha_picking": "2026-04-03T11:15:00Z",
  "operario": {
    "operario_id": "uuid",
    "nombre": "Carlos Ramírez"
  },
  "observaciones": "Picking completo sin novedades",
  "duracion_minutos": 4 // desde compromiso hasta picking
}
```

### GET /api/v1/despacho/{pedido_id}/detalle
**Purpose**: Consultar detalle de registro de despacho

**Response 200 OK**:
```json
{
  "registro_despacho_id": "uuid",
  "pedido": {
    "pedido_id": "uuid",
    "numero_pedido": "PED-20260403-001"
  },
  "fecha_despacho": "2026-04-03T11:20:00Z",
  "operario": {
    "operario_id": "uuid",
    "nombre": "María González"
  },
  "transportista": "Transportes ABC S.A.S.",
  "placa_vehiculo": "ABC-123",
  "observaciones": "Despacho completo",
  "movimientos_generados": [
    {
      "movimiento_id": "uuid",
      "lote": {
        "codigo_lote": "LOT-2026-001",
        "codigo_lote": "LOT-2025-001"
      },
      "cantidad": -80
    }
  ]
}
```

---

## Implementation Tasks

### Phase 1: Domain Entities

**T001: Crear entidad RegistroPicking (domain)**
- Path: `domain/entities/RegistroPicking.java`
- Atributos según especificación
- Validación: fecha_picking no puede ser futura

**T002: Crear entidad RegistroDespacho (domain)**
- Path: `domain/entities/RegistroDespacho.java`
- Atributos según especificación
- Validación: transportista no vacío, fecha_despacho no futura

**T003: Extender enum EstadoPedido**
- Path: `domain/entities/Pedido.java`
- Agregar estados: EN_PICKING, DESPACHADO
- Validar transiciones: COMPROMETIDO → EN_PICKING → DESPACHADO

**T004: Extender enum TipoMovimiento**
- Path: `domain/entities/MovimientoInventario.java`
- Agregar tipo: SALIDA

### Phase 2: Repository Interfaces

**T005: Crear RegistroPickingRepository (domain/repositories)**
- Métodos:
  - `UUID save(RegistroPicking registro)`
  - `Optional<RegistroPicking> findByPedidoId(UUID pedidoId)`

**T006: Crear RegistroDespachoRepository (domain/repositories)**
- Métodos:
  - `UUID save(RegistroDespacho registro)`
  - `Optional<RegistroDespacho> findByPedidoId(UUID pedidoId)`

**T007: Extender PedidoRepository**
- Agregar métodos:
  - `List<Pedido> findByEstadoOrderByFechaCompromisoAsc(EstadoPedido estado)` // para picking
  - `List<Pedido> findByEstadoWithPickingOrderByFechaPickingAsc(EstadoPedido estado)` // para despacho

### Phase 3: Use Cases (Application Layer)

**T008: Implementar ConsultarPedidosParaPickingUseCase**
- Path: `application/usecases/ConsultarPedidosParaPickingUseCase.java`
- Input: Pageable
- Output: Page<PedidoPickingDTO> (incluye líneas y lotes comprometidos)
- Lógica:
  1. Consultar Pedidos con estado = COMPROMETIDO
  2. Ordenar por fecha_compromiso ASC (FIFO)
  3. Para cada pedido: JOIN con ProductoPedido, Producto, LoteComprometido, Lote
  4. Incluir información de lotes para facilitar recolección

**T009: Implementar ConfirmarPickingUseCase**
- Path: `application/usecases/ConfirmarPickingUseCase.java`
- Input: ConfirmarPickingCommand (pedido_id, operario_id, observaciones)
- Output: RegistroPickingDTO
- Lógica:
  1. Buscar Pedido (404 si no existe)
  2. Validar estado = COMPROMETIDO (409 si no)
  3. Transacción:
     - Crear RegistroPicking
     - Actualizar Pedido.estado = EN_PICKING
     - NO modificar Lote.cantidad - FR-068
  4. Retornar DTO

**T010: Implementar ConsultarPedidosParaDespachoUseCase**
- Path: `application/usecases/ConsultarPedidosParaDespachoUseCase.java`
- Input: Pageable
- Output: Page<PedidoDespachoDTO> (incluye tiempo_desde_picking, alerta)
- Lógica:
  1. Consultar Pedidos con estado = EN_PICKING
  2. JOIN con RegistroPicking para obtener fecha_picking
  3. Calcular tiempo_desde_picking = now - fecha_picking
  4. Marcar alerta = true si tiempo_desde_picking > 5 min (SC-035)
  5. Ordenar por fecha_picking ASC (FIFO)

**T011: Implementar ConfirmarDespachoUseCase**
- Path: `application/usecases/ConfirmarDespachoUseCase.java`
- Input: ConfirmarDespachoCommand (pedido_id, operario_id, transportista, placa, observaciones)
- Output: RegistroDespachoDTO (incluye movimientos_generados)
- Lógica:
  1. Buscar Pedido (404 si no existe)
  2. Validar estado = EN_PICKING (409 si no)
  3. Buscar LoteComprometidos del pedido
  4. Transacción atómica:
     - Para cada LoteComprometido:
       * Buscar Lote
       * Validar cantidad >= cantidad_comprometida (500 si no)
       * Reducir Lote.cantidad
       * Crear MovimientoInventario tipo SALIDA (cantidad negativa, referencia a pedido_id)
     - Crear RegistroDespacho
     - Actualizar Pedido.estado = DESPACHADO
  5. Retornar DTO con lista de movimientos generados

**T012: Implementar ConsultarDetallePickingUseCase**
- Path: `application/usecases/ConsultarDetallePickingUseCase.java`
- Input: pedido_id
- Output: RegistroPickingDetalleDTO (registro, pedido, operario, duracion)
- Lógica: Buscar RegistroPicking, JOIN con Pedido, Usuario

**T013: Implementar ConsultarDetalleDespachoUseCase**
- Path: `application/usecases/ConsultarDetalleDespachoUseCase.java`
- Input: pedido_id
- Output: RegistroDespachoDetalleDTO (registro, pedido, operario, movimientos)
- Lógica: Buscar RegistroDespacho, JOIN con MovimientoInventario, Lote

### Phase 4: Infrastructure (JPA, DB)

**T014: Crear schema SQL con Flyway**
- Path: `infrastructure/db/migrations/V006__create_picking_despacho.sql`
- Tablas:
  - `registros_picking` (PK: registro_picking_id, FK: pedido_id UNIQUE, operario_id)
  - `registros_despacho` (PK: registro_despacho_id, FK: pedido_id UNIQUE, operario_id)
- Constraints:
  - UNIQUE(pedido_id) en ambas tablas (ONE-TO-ONE con Pedido)
- Índices:
  - `idx_picking_pedido` ON registros_picking(pedido_id)
  - `idx_despacho_pedido` ON registros_despacho(pedido_id)
  - `idx_picking_fecha` ON registros_picking(fecha_picking)
  - `idx_despacho_fecha` ON registros_despacho(fecha_despacho)

**T015: Implementar JPA Entities**
- RegistroPickingEntity.java
- RegistroDespachoEntity.java

**T016: Implementar JPA Repositories**
- JpaRegistroPickingRepository extends JpaRepository
- JpaRegistroDespachoRepository extends JpaRepository

**T017: Implementar Repository Adapters**
- RegistroPickingRepositoryImpl implements RegistroPickingRepository
- RegistroDespachoRepositoryImpl implements RegistroDespachoRepository

### Phase 5: REST Controllers

**T018: Implementar PickingController**
- Path: `infrastructure/web/controllers/PickingController.java`
- GET /api/v1/picking/pedidos → ConsultarPedidosParaPickingUseCase
- POST /api/v1/picking/confirmar → ConfirmarPickingUseCase
- GET /api/v1/picking/{pedido_id}/detalle → ConsultarDetallePickingUseCase
- Validaciones: 400, 404, 409

**T019: Implementar DespachoController**
- Path: `infrastructure/web/controllers/DespachoController.java`
- GET /api/v1/despacho/pedidos → ConsultarPedidosParaDespachoUseCase
- POST /api/v1/despacho/confirmar → ConfirmarDespachoUseCase
- GET /api/v1/despacho/{pedido_id}/detalle → ConsultarDetalleDespachoUseCase
- Validaciones: 400, 404, 409, 500 (stock inconsistente)

### Phase 6: Testing

**T020: Unit tests - Use Cases**
- Test ConfirmarPickingUseCase:
  - Happy path: Picking confirmado, estado cambia a EN_PICKING
  - Edge case: cantidad NO cambia (verificar)
  - Error case: Pedido no en COMPROMETIDO lanza EstadoInvalidoException
- Test ConfirmarDespachoUseCase:
  - Happy path: Despacho reduce stock, crea movimientos, cambia estado a DESPACHADO
  - Edge case: Múltiples lotes comprometidos (3 lotes) → todos reducen stock correctamente
  - Error case: Stock inconsistente (cantidad < cantidad_comprometida) → rollback

**T021: Integration tests - Controllers**
- Test POST /api/v1/picking/confirmar:
  - Confirmar picking retorna 200
  - Verificar en DB: estado = EN_PICKING, Lote.cantidad NO cambió
  - Pedido ya en EN_PICKING retorna 409
- Test POST /api/v1/despacho/confirmar:
  - Confirmar despacho retorna 200 con movimientos
  - Verificar en DB: estado = DESPACHADO, Lote.cantidad reducido, MovimientoInventario creado
  - Pedido ya en DESPACHADO retorna 409

**T022: Integration tests - Transaction Atomicity**
- Test crítico: Simular fallo en creación de MovimientoInventario → rollback completo (stock NO reducido, estado NO cambiado)
- Test: Simular stock inconsistente (mock) → retorna 500, rollback

**T023: Performance tests**
- Test: Confirmar picking ≤ 5 seg
- Test: Confirmar despacho con 20 líneas, 50 lotes ≤ 10 seg

---

## Tests

### Unit Tests
- Use Cases: 12 tests

### Integration Tests
- Controllers: 8 tests
- Transaction atomicity: 3 tests
- Performance: 2 tests

---

## Acceptance Criteria

**FR-067**: Solo pedidos en estado Comprometido pueden pasar a Picking.

**FR-068**: Confirmación de picking NO modifica stock (ya estaba comprometido).

**FR-070**: Solo pedidos en estado En Picking pueden pasar a Despachado.

**FR-071**: Confirmación de despacho reduce stock de lotes y genera MovimientoInventario tipo Salida.

**SC-035**: Picking debe completarse en ≤ 5 minutos desde compromiso (alerta en consulta de despacho).

---

## Notes & Best Practices

1. **Picking NO cambia stock**: Este es un concepto crítico - FR-068. El stock ya se redujo en el Compromiso. Picking es solo tracking operativo.

2. **Despacho SÍ reduce stock**: Aquí es donde finalmente sale la mercancía - FR-071. Crear MovimientoInventario tipo SALIDA para cada lote.

3. **Atomicidad en despacho**: Si falla reducción de stock de algún lote, rollback completo. Pedido vuelve a EN_PICKING para reintento.

4. **FIFO en listas**: Pedidos se muestran en orden de fecha_compromiso (picking) o fecha_picking (despacho) ASC. Más antiguos primero.

5. **Alerta de tiempo**: En consulta de despacho, calcular tiempo_desde_picking y marcar alerta si > 5 min - SC-035. Frontend debe mostrar warning visual.

6. **Validación de stock en despacho**: Aunque no debería fallar (stock ya comprometido), validar cantidad >= cantidad_comprometida por seguridad. Si falla, log error crítico (indica bug en compromiso) y retornar 500.

7. **ONE-TO-ONE constraints**: Un pedido tiene exactamente 1 RegistroPicking y 1 RegistroDespacho. Usar UNIQUE(pedido_id) en tablas para enforcar.

8. **Transportista obligatorio**: El despacho requiere transportista (nombre). Placa_vehiculo es opcional (puede ser transporte sin placa identificable).

9. **Detalle de lotes en picking**: Al listar pedidos para picking, incluir lotes comprometidos con ubicación (si existe). Esto facilita la recolección física.

10. **Testing de rollback crítico**: Es esencial probar que si falla creación de MovimientoInventario durante despacho, toda la transacción hace rollback. Simular fallo de BD en medio de la transacción.
