# Plan de Implementación: Consulta de Inventario - Backend

**Date**: 2026-04-03  
**Specs**: 05_consultar_stock_disponible.md · 06_consultar_movimientos.md  
**Module**: Módulo 1 - Inventario  
**Layer**: Backend  
**Priority**: P2 (Consulta, no bloqueante pero importante)

---

## Summary

Implementación de endpoints de consulta para visualizar stock disponible (por SKU, con detalle de lotes FEFO) y movimientos de inventario (kardex). Son operaciones de solo lectura que proveen visibilidad del inventario en tiempo real para operarios y supervisores.

---

## Dependencies

**Blocked by**:
- Plan Gestión SKU Backend (requiere productos)
- Plan Recepción Backend (requiere lotes y movimientos)

**Blocks**:
- Plan Frontend Consulta Inventario (consume estos endpoints)

**External Dependencies**:
- Ninguna (solo lectura de BD local)

---

## Entities & Domain

Este plan NO crea nuevas entidades, solo consulta las existentes:
- **Producto**: Para información del SKU
- **Lote**: Para stock por lote con fechas de vencimiento
- **MovimientoInventario**: Para kardex (historial de movimientos)

---

## Use Cases

### UC-001: Consultar Stock Disponible por SKU (Spec 05)

**Actor**: Operario, Supervisor, Sistema (para pedidos)

**Flujo Principal**:
1. Usuario solicita stock de un sku_id específico
2. Sistema consulta:
   - Información del Producto (marca, presentacion, contenido)
   - Todos los Lotes con stock_actual > 0, ordenados por fecha_vencimiento ASC (FEFO)
   - Stock total: SUM(stock_actual)
3. Retorna:
   - Datos del producto
   - Stock total disponible
   - Detalle por lote (codigo, vencimiento, stock)

**Business Logic**:
- Solo incluir lotes con stock_actual > 0 (ignorar lotes agotados)
- Ordenar lotes por fecha_vencimiento ASC (FEFO) - FR-061
- Si producto no tiene lotes → stock_total = 0 (no error)

**Performance**:
- Consulta de stock de 1 SKU ≤ 500 ms
- Consulta de catálogo (500 productos) ≤ 3 seg - SC-025

### UC-002: Consultar Stock de Múltiples SKUs (Spec 05)

**Actor**: Sistema (para validar disponibilidad en pedidos)

**Flujo**:
1. Sistema envía lista de sku_ids
2. Sistema consulta stock_total para cada sku_id
3. Retorna mapa: {sku_id: stock_total}

**Business Logic**:
- Batch query eficiente (IN clause o JOIN)
- Si algún sku_id no existe → ignorar (no retornar en mapa)

### UC-003: Consultar Movimientos de Inventario (Kardex) (Spec 06)

**Actor**: Supervisor, Auditor

**Flujo**:
1. Usuario solicita kardex con filtros:
   - sku_id (opcional)
   - lote_id (opcional)
   - tipo_movimiento (opcional)
   - fecha_desde, fecha_hasta (opcional)
   - page, size (paginación)
2. Sistema consulta MovimientoInventario con filtros aplicados
3. Retorna lista paginada con:
   - Datos del movimiento
   - Información del lote y producto asociado
   - Operario que realizó el movimiento (si aplica)

**Business Logic**:
- Ordenar por fecha_movimiento DESC (más recientes primero)
- Incluir información contextual: producto, lote, pedido (si aplica), excepción (si aplica)

**Performance**:
- Consulta de kardex de 1 lote ≤ 1 seg
- Consulta de kardex general (1 mes) con paginación ≤ 2 seg

### UC-004: Consultar Detalle de Lote (Spec 05)

**Actor**: Operario, Supervisor

**Flujo**:
1. Usuario solicita detalle de un lote_id específico
2. Sistema retorna:
   - Información del lote (codigo, fechas, stock)
   - Información del producto asociado
   - Datos de recepción (fecha, operario)
   - Kardex del lote (últimos movimientos)

**Business Logic**:
- Incluir últimos 10 movimientos del lote para contexto
- Mostrar cantidad_inicial y stock_actual para calcular rotación

---

## Endpoints / API

### GET /api/v1/inventario/stock/{sku_id}
**Purpose**: Consultar stock disponible de un SKU con detalle de lotes

**Path Param**:
- `sku_id`: UUID del producto

**Response 200 OK**:
```json
{
  "sku": {
    "sku_id": "uuid",
    "marca": "Pilsen",
    "presentacion": "Six-pack",
    "contenido_ml": 1980,
    "peso_logistico_kg": 2.5
  },
  "stock_total": 480,
  "lotes": [
    {
      "lote_id": "uuid",
      "codigo_lote": "LOT-2025-001",
      "fecha_vencimiento": "2026-06-15",
      "fecha_fabricacion": "2025-12-01",
      "stock_actual": 240,
      "dias_hasta_vencimiento": 73
    },
    {
      "lote_id": "uuid",
      "codigo_lote": "LOT-2025-015",
      "fecha_vencimiento": "2026-08-20",
      "stock_actual": 240,
      "dias_hasta_vencimiento": 139
    }
  ],
  "proximo_vencimiento": {
    "lote_id": "uuid",
    "fecha_vencimiento": "2026-06-15",
    "dias_restantes": 73
  }
}
```

**Response 404 Not Found**:
```json
{
  "error": "SKU_NOT_FOUND",
  "message": "El SKU especificado no existe en el catálogo"
}
```

### GET /api/v1/inventario/stock
**Purpose**: Consultar stock de múltiples SKUs (batch query)

**Query Params**:
- `sku_ids`: Comma-separated UUIDs (ej: "uuid1,uuid2,uuid3")
- `include_zero_stock`: Boolean (default: false) - incluir SKUs con stock = 0

**Response 200 OK**:
```json
{
  "stocks": [
    {
      "sku_id": "uuid1",
      "marca": "Pilsen",
      "presentacion": "Unidad",
      "stock_total": 1200
    },
    {
      "sku_id": "uuid2",
      "marca": "Águila",
      "presentacion": "Six-pack",
      "stock_total": 0
    }
  ]
}
```

### GET /api/v1/inventario/lotes/{lote_id}
**Purpose**: Consultar detalle completo de un lote específico

**Response 200 OK**:
```json
{
  "lote": {
    "lote_id": "uuid",
    "codigo_lote": "LOT-2025-001",
    "fecha_vencimiento": "2026-06-15",
    "fecha_fabricacion": "2025-12-01",
    "cantidad_inicial": 300,
    "stock_actual": 240,
    "creado_el": "2026-03-15T09:30:00Z"
  },
  "producto": {
    "sku_id": "uuid",
    "marca": "Pilsen",
    "presentacion": "Six-pack"
  },
  "recepcion": {
    "recepcion_id": "uuid",
    "fecha_recepcion": "2026-03-15T09:30:00Z",
    "operario_nombre": "Juan Pérez"
  },
  "movimientos_recientes": [
    {
      "movimiento_id": "uuid",
      "tipo_movimiento": "Entrada",
      "cantidad": 300,
      "fecha": "2026-03-15T09:30:00Z"
    },
    {
      "movimiento_id": "uuid",
      "tipo_movimiento": "Compromiso",
      "cantidad": -60,
      "fecha": "2026-03-20T14:00:00Z",
      "pedido_id": "uuid"
    }
  ]
}
```

**Response 404 Not Found**:
```json
{
  "error": "LOTE_NOT_FOUND",
  "message": "El lote especificado no existe"
}
```

### GET /api/v1/inventario/movimientos
**Purpose**: Consultar kardex (movimientos de inventario) con filtros

**Query Params**:
- `sku_id` (opcional): UUID del producto
- `lote_id` (opcional): UUID del lote
- `tipo_movimiento` (opcional): Enum (Entrada, Compromiso, Picking, Salida, Baja Avería, Baja Vencimiento, Faltante)
- `fecha_desde` (opcional): ISO Date (ej: "2026-03-01")
- `fecha_hasta` (opcional): ISO Date (ej: "2026-03-31")
- `page` (default: 0): Número de página
- `size` (default: 20): Tamaño de página

**Response 200 OK**:
```json
{
  "movimientos": [
    {
      "movimiento_id": "uuid",
      "tipo_movimiento": "Entrada",
      "cantidad": 300,
      "fecha_movimiento": "2026-03-15T09:30:00Z",
      "lote": {
        "lote_id": "uuid",
        "codigo_lote": "LOT-2025-001"
      },
      "producto": {
        "sku_id": "uuid",
        "marca": "Pilsen",
        "presentacion": "Six-pack"
      },
      "operario_nombre": "Juan Pérez",
      "observaciones": "Recepción contra manifiesto MAN-2025-042"
    },
    {
      "movimiento_id": "uuid",
      "tipo_movimiento": "Compromiso",
      "cantidad": -60,
      "fecha_movimiento": "2026-03-20T14:00:00Z",
      "lote": {
        "lote_id": "uuid",
        "codigo_lote": "LOT-2025-001"
      },
      "producto": {
        "sku_id": "uuid",
        "marca": "Pilsen",
        "presentacion": "Six-pack"
      },
      "pedido_id": "uuid",
      "observaciones": "Compromiso FEFO para pedido PED-001"
    }
  ],
  "pagination": {
    "total_elements": 487,
    "total_pages": 25,
    "current_page": 0,
    "page_size": 20
  }
}
```

### GET /api/v1/inventario/resumen
**Purpose**: Dashboard con resumen general de inventario

**Response 200 OK**:
```json
{
  "total_skus_activos": 45,
  "total_lotes_con_stock": 128,
  "stock_total_unidades": 15420,
  "alertas": {
    "proximos_vencer_30_dias": 8,
    "stock_bajo": 3,
    "excepciones_abiertas": 5
  },
  "movimientos_hoy": {
    "entradas": 240,
    "salidas": 180,
    "compromisos": 120
  }
}
```

---

## Implementation Tasks

### Phase 1: Use Cases (Application Layer)

**T001: Implementar ConsultarStockPorSkuUseCase**
- Path: `application/usecases/ConsultarStockPorSkuUseCase.java`
- Input: sku_id
- Output: StockDisponibleDTO (sku info, stock_total, lista de lotes con FEFO)
- Lógica:
  1. Buscar Producto por sku_id (404 si no existe)
  2. Buscar Lotes con stock_actual > 0, ordenados por fecha_vencimiento ASC
  3. Calcular stock_total = SUM(stock_actual)
  4. Calcular dias_hasta_vencimiento para cada lote
  5. Identificar proximo_vencimiento (primer lote en lista FEFO)

**T002: Implementar ConsultarStockMultipleSkusUseCase**
- Path: `application/usecases/ConsultarStockMultipleSkusUseCase.java`
- Input: List<UUID> skuIds, boolean includeZeroStock
- Output: List<StockResumenDTO> (sku_id, marca, presentacion, stock_total)
- Lógica:
  1. Query eficiente con JOIN: Producto + SUM(Lote.stock_actual) GROUP BY sku_id
  2. Si includeZeroStock = false: filtrar solo stock_total > 0
  3. Retornar lista ordenada por marca, presentacion

**T003: Implementar ConsultarDetalleLoteUseCase**
- Path: `application/usecases/ConsultarDetalleLoteUseCase.java`
- Input: lote_id
- Output: LoteDetalleDTO (lote, producto, recepcion, movimientos_recientes)
- Lógica:
  1. Buscar Lote (404 si no existe)
  2. JOIN con Producto, Recepcion
  3. Buscar MovimientoInventario por lote_id (últimos 10, ordenados por fecha DESC)

**T004: Implementar ConsultarMovimientosInventarioUseCase**
- Path: `application/usecases/ConsultarMovimientosInventarioUseCase.java`
- Input: FiltrosKardexDTO (sku_id, lote_id, tipo, fecha_desde, fecha_hasta, pageable)
- Output: Page<MovimientoInventarioDTO>
- Lógica:
  1. Construir query dinámica con filtros (usar Specification pattern)
  2. JOIN con Lote, Producto, Pedido (nullable), ExcepcionInventario (nullable), Usuario (nullable)
  3. Ordenar por fecha_movimiento DESC
  4. Aplicar paginación

**T005: Implementar ConsultarResumenInventarioUseCase**
- Path: `application/usecases/ConsultarResumenInventarioUseCase.java`
- Input: Ninguno
- Output: ResumenInventarioDTO (totales, alertas, movimientos_hoy)
- Lógica:
  1. COUNT(DISTINCT Producto) con lotes activos
  2. COUNT(Lote) WHERE stock_actual > 0
  3. SUM(Lote.stock_actual)
  4. Alertas:
     - Lotes con fecha_vencimiento <= hoy + 30 dias
     - SKUs con stock_total < umbral (ej: 50 unidades)
     - ExcepcionInventario con estado = ABIERTA
  5. Movimientos_hoy: COUNT MovimientoInventario WHERE fecha >= hoy 00:00

### Phase 2: Query Optimization

**T006: Crear query FEFO optimizada en LoteRepository**
- Método:
  ```java
  @Query("SELECT l FROM LoteEntity l WHERE l.skuId = :skuId AND l.stockActual > 0 ORDER BY l.fechaVencimiento ASC, l.creadoEl ASC")
  List<LoteEntity> findAvailableBySkuOrderByFEFO(@Param("skuId") UUID skuId);
  ```
- Usar índice existente: `idx_lotes_sku_vencimiento`

**T007: Crear query batch stock en LoteRepository**
- Método:
  ```java
  @Query("SELECT l.skuId as skuId, SUM(l.stockActual) as stockTotal " +
         "FROM LoteEntity l WHERE l.skuId IN :skuIds AND l.stockActual > 0 " +
         "GROUP BY l.skuId")
  List<StockProjection> findStockBySkuIds(@Param("skuIds") List<UUID> skuIds);
  ```
- Interface projection: `StockProjection` (skuId, stockTotal)

**T008: Crear Specification para filtros dinámicos de kardex**
- Path: `infrastructure/persistence/specifications/MovimientoInventarioSpecification.java`
- Métodos estáticos:
  - `bySkuId(UUID skuId)`
  - `byLoteId(UUID loteId)`
  - `byTipoMovimiento(TipoMovimiento tipo)`
  - `byFechaRange(LocalDate desde, LocalDate hasta)`
- Composición con `Specification.where(spec1).and(spec2)...`

### Phase 3: REST Controllers

**T009: Implementar InventarioConsultaController**
- Path: `infrastructure/web/controllers/InventarioConsultaController.java`
- GET /api/v1/inventario/stock/{sku_id} → ConsultarStockPorSkuUseCase
- GET /api/v1/inventario/stock (batch) → ConsultarStockMultipleSkusUseCase
- GET /api/v1/inventario/lotes/{lote_id} → ConsultarDetalleLoteUseCase
- GET /api/v1/inventario/movimientos → ConsultarMovimientosInventarioUseCase
- GET /api/v1/inventario/resumen → ConsultarResumenInventarioUseCase
- Validaciones:
  - 400: Parámetros inválidos (sku_ids vacío, dates mal formateadas)
  - 404: SKU o Lote no encontrado
- Cache headers: `Cache-Control: max-age=30` para consultas de stock (30 seg)

### Phase 4: DTOs & Mappers

**T010: Crear DTOs de respuesta**
- StockDisponibleDTO (sku, stock_total, lotes[], proximo_vencimiento)
- LoteStockDTO (lote_id, codigo_lote, fecha_vencimiento, stock_actual, dias_hasta_vencimiento)
- StockResumenDTO (sku_id, marca, presentacion, stock_total)
- LoteDetalleDTO (lote, producto, recepcion, movimientos_recientes[])
- MovimientoInventarioDTO (movimiento, lote, producto, operario, pedido, excepcion)
- ResumenInventarioDTO (totales, alertas, movimientos_hoy)

**T011: Implementar Mappers**
- LoteMapper: Entity → LoteStockDTO (calcular dias_hasta_vencimiento)
- MovimientoMapper: Entity → MovimientoInventarioDTO (joins con entidades relacionadas)
- ProductoMapper: reusar del plan SKU

### Phase 5: Testing

**T012: Unit tests - Use Cases**
- Test ConsultarStockPorSkuUseCase:
  - Happy path: SKU con 2 lotes retorna lista ordenada FEFO
  - Edge case: SKU sin lotes retorna stock_total = 0
  - Error case: SKU inexistente lanza NotFoundException
- Test ConsultarStockMultipleSkusUseCase:
  - Happy path: Batch de 10 SKUs retorna stock de todos
  - Edge case: includeZeroStock=false filtra SKUs sin stock
- Test ConsultarMovimientosInventarioUseCase:
  - Happy path: Filtros combinados (sku + tipo + fecha) retornan resultados correctos
  - Edge case: Sin filtros retorna todos los movimientos paginados

**T013: Integration tests - Controllers**
- Test GET /api/v1/inventario/stock/{sku_id}:
  - Retorna 200 con lotes ordenados por fecha_vencimiento ASC
  - Calcula correctamente dias_hasta_vencimiento
  - Retorna 404 si SKU no existe
- Test GET /api/v1/inventario/stock (batch):
  - Acepta múltiples sku_ids separados por comas
  - Retorna solo SKUs con stock si includeZeroStock=false
- Test GET /api/v1/inventario/movimientos:
  - Filtros funcionan correctamente
  - Paginación retorna total_pages correcto
  - Ordenamiento DESC por fecha

**T014: Performance tests**
- Test: Consulta de stock de 1 SKU completa en ≤ 500 ms
- Test: Consulta batch de 50 SKUs completa en ≤ 2 seg
- Test: Kardex de 1 mes (1000 registros) con paginación ≤ 2 seg

---

## Tests

### Unit Tests
- Use Cases: 9 tests

### Integration Tests
- Controllers: 8 tests
- Performance: 3 tests

---

## Acceptance Criteria

**FR-034**: Operarios pueden consultar stock disponible de cualquier SKU con detalle de lotes.

**FR-035**: Sistema muestra lotes ordenados por fecha de vencimiento (FEFO) para facilitar rotación.

**FR-043**: Supervisores pueden consultar kardex (movimientos de inventario) con filtros por SKU, lote, tipo y fecha.

**FR-044**: Kardex incluye información contextual: producto, operario, pedido (si aplica), excepción (si aplica).

**SC-025**: Consulta de catálogo (500 productos con stock) completa en ≤ 3 segundos.

---

## Notes & Best Practices

1. **FEFO en consultas**: Siempre ordenar lotes por fecha_vencimiento ASC. Este orden es crítico para operarios que necesitan saber qué lote despachar primero.

2. **Cálculo de dias_hasta_vencimiento**: Hacer en el mapper, no en BD. Usar `ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento)`.

3. **Caché de consultas**: Stock disponible puede cachearse 30 segundos (header `Cache-Control: max-age=30`). No cachear kardex (debe ser tiempo real).

4. **Batch queries eficientes**: Para consultar stock de múltiples SKUs, usar JOIN + GROUP BY en una sola query, NO hacer N+1 queries.

5. **Índices críticos**:
   - `idx_lotes_sku_vencimiento` ON lotes(sku_id, fecha_vencimiento) // FEFO
   - `idx_lotes_stock` ON lotes(sku_id, stock_actual) WHERE stock_actual > 0
   - `idx_movimientos_lote` ON movimientos_inventario(lote_id)
   - `idx_movimientos_fecha` ON movimientos_inventario(fecha_movimiento)

6. **Specification pattern para filtros dinámicos**: Kardex tiene muchas combinaciones de filtros. Usar Spring Data JPA Specification para construir queries dinámicas sin SQL nativo.

7. **Paginación obligatoria en kardex**: No retornar todos los movimientos sin paginación. Default: page=0, size=20. Máximo: size=100.

8. **Alertas en resumen**: El dashboard de resumen debe calcular:
   - Lotes próximos a vencer (30 días)
   - SKUs con stock bajo (umbral configurable, ej: 50 unidades)
   - Excepciones abiertas pendientes de investigación

9. **Performance monitoring**: Estas son las queries MÁS FRECUENTES del sistema. Monitorear tiempos de respuesta y optimizar índices si es necesario.

10. **Read-only operations**: Este plan NO modifica datos. Todas las operaciones son consultas (SELECT). No requiere @Transactional(readOnly = true) mejora performance.
