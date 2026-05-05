# Plan de Implementación: Gestión de SKU - Backend

**Date**: 2026-04-03  
**Specs**: 01_crear_plantilla_producto.md · 02_modificar_plantilla_producto.md · 03_consultar_productos.md  
**Module**: Módulo 1 - Inventario  
**Layer**: Backend  
**Priority**: P1 (Bloqueante para todo el módulo)

---

## Summary

Implementación del CRUD de productos (SKU) del catálogo. Este feature es la base de todo el módulo: sin `Producto`, no existen lotes, recepciones, ni pedidos. El backend expone endpoints REST para crear, modificar, eliminar y consultar productos, con registro automático de bitácora para trazabilidad completa.

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
│   ├── model/
│   │   ├── Producto.java        # Entidad de negocio Producto
│   │   └── BitacoraProducto.java
│   ├── repository/
│   │   ├── ProductoRepository.java
│   │   └── BitacoraProductoRepository.java
│   └── exception/
│       ├── ProductoConLotesActivosException.java
│       ├── ProductoDuplicadoException.java
│       └── ProductoNotFoundException.java
├── application/                 # Casos de uso (@Service)
│   └── usecase/
│       ├── ConsultarBitacoraUseCase.java
│       ├── ConsultarCatalogoUseCase.java
│       ├── CrearProductoUseCase.java
│       ├── EliminarProductoUseCase.java
│       └── ModificarProductoUseCase.java
└── infrastructure/
    ├── web/
    │   ├── controller/
    │   │   └── ProductoController.java  # Expone los endpoints de CRUD
    │   └── dto/
    │       ├── BitacoraResponse.java
    │       ├── ProductoInfoDTO.java
    │       ├── ProductoRequest.java     # Request DTO
    │       ├── ProductoResponse.java    # Response DTO
    │       └── ProductoUpdateRequest.java
    └── persistence/             # Entidades JPA + Repositories + Adapters
        ├── entity/
        │   ├── ProductoJpaEntity.java
        │   └── BitacoraProductoJpaEntity.java
        ├── repository/
        │   ├── ProductoJpaRepository.java
        │   └── BitacoraProductoJpaRepository.java
        └── adapter/
            ├── ProductoRepositoryAdapter.java
            └── BitacoraProductoRepositoryAdapter.java
```

---

## Dependencies

**Blocked by**:
- Setup (Phase 1 del plan maestro): Requiere BD PostgreSQL configurada con Flyway

**Blocks**:
- Plan Recepción Backend (requiere productos existentes para crear lotes)
- Plan Consulta Inventario Backend (requiere productos para calcular stock)
- Plan Pedidos Backend (requiere productos en catálogo)

**External Dependencies**:
- Ninguna (no depende de servicios externos)

---

## Entities & Domain

### Producto (SKU)
**Propósito**: Entidad fundamental que define una referencia comercial.

**Atributos**:
- `sku_id`: UUID (PK, autogenerado, inmutable)
- `marca`: String (ej: "Pilsen", "Postobón")
- `presentacion`: String (ej: "Unidad", "Six-pack", "Caja")
- `contenido_ml`: Integer (contenido líquido en mililitros)
- `peso_logistico_kg`: Decimal (peso bruto para cálculos de transporte)
- `creado_el`: DateTime

**Constraints**:
- UNIQUE(marca, presentacion) - FR-005
- peso_logistico_kg > 0
- sku_id es inmutable - FR-003

**Business Rules**:
- No se puede eliminar si tiene lotes activos - FR-011
- Cualquier modificación genera entrada en BitacoraProducto - FR-008

### BitacoraProducto
**Propósito**: Registro de auditoría de cambios en productos.

**Atributos**:
- `id`: Long (PK, autogenerado)
- `sku_id_ref`: UUID (FK -> Producto)
- `campo`: String (nombre del atributo modificado)
- `valor_anterior`: String
- `valor_nuevo`: String
- `descripcion`: String (motivo del cambio)
- `fecha`: DateTime
- `usuario`: String

**Business Rules**:
- Se crea automáticamente al modificar producto
- Cambios en peso_logistico_kg generan alerta - FR-009

---

## Use Cases

### CrearProductoUseCase
**Input**: ProductoRequest (marca, presentacion, contenido_ml, peso_logistico_kg)  
**Output**: ProductoResponse (incluye sku_id autogenerado)  
**Business Logic**:
1. Validar que no exista combinación marca + presentacion
2. Generar sku_id = UUID.randomUUID()
3. Guardar producto con stock inicial = 0
4. Retornar producto creado

**Exceptions**:
- `ProductoDuplicadoException` si ya existe marca + presentacion

### ModificarProductoUseCase
**Input**: UUID skuId, ProductoUpdateRequest  
**Output**: ProductoResponse (con campo alerta si aplica)  
**Business Logic**:
1. Cargar producto por skuId
2. Validar nueva combinación marca + presentacion no genera duplicado
3. Por cada campo modificado:
   - Registrar entrada en BitacoraProducto
   - Si campo es peso_logistico_kg → agregar alerta en respuesta
4. Guardar producto actualizado
5. Retornar producto con historial de cambio

**Exceptions**:
- `ProductoNotFoundException` si skuId no existe (404)
- `ProductoDuplicadoException` si nueva combinación existe (409)
- `ProductoConLotesActivosException` al eliminar producto con lotes (409)

### ConsultarCatalogoUseCase
**Input**: String busqueda (opcional)  
**Output**: List<ProductoResponse>  
**Business Logic**:
1. Buscar productos por marca o presentacion (si busqueda presente)
2. Para cada producto calcular stock_disponible:
   ```sql
   SELECT SUM(l.cantidad) 
   FROM lote l 
   WHERE l.sku_ref = producto.sku_id 
   AND l.estado = 'DISPONIBLE'
   ```
3. Mapear a ProductoResponse con campo disponibilidad
4. NO incluir detalles de lotes (código, fecha vencimiento)

**Performance**: Query optimizada con índice en lote.sku_ref

---

## Endpoints / API

### POST /api/v1/productos
**Purpose**: Crear nuevo producto (SKU)  
**Actor**: Supervisor de Inventario  
**Request Body**:
```json
{
  "marca": "Pilsen",
  "presentacion": "Six-pack",
  "contenido_ml": 330,
  "peso_logistico_kg": 2.5
}
```
**Response**: 201 Created
```json
{
  "sku_id": "550e8400-e29b-41d4-a716-446655440000",
  "marca": "Pilsen",
  "presentacion": "Six-pack",
  "contenido_ml": 330,
  "peso_logistico_kg": 2.5,
  "stock_disponible": 0,
  "disponibilidad": "No disponible",
  "creado_el": "2026-04-03T19:30:00Z"
}
```
**Error Responses**:
- 400 Bad Request: Campos inválidos (validación @Valid)
- 409 Conflict: Producto duplicado (marca + presentacion existe)

### PUT /api/v1/productos/{skuId}
**Purpose**: Modificar producto existente  
**Actor**: Supervisor de Inventario  
**Request Body**:
```json
{
  "marca": "Pilsen",
  "presentacion": "Six-pack",
  "contenido_ml": 330,
  "peso_logistico_kg": 2.8,
  "descripcion": "Actualización de peso por cambio de empaque"
}
```
**Response**: 200 OK
```json
{
  "sku_id": "550e8400-e29b-41d4-a716-446655440000",
  "marca": "Pilsen",
  "presentacion": "Six-pack",
  "peso_logistico_kg": 2.8,
  "alerta": "El peso logístico fue modificado. Notificar al Módulo de Logística."
}
```
**Error Responses**:
- 404 Not Found: Producto no existe
- 409 Conflict: Nueva combinación duplica producto existente

### DELETE /api/v1/productos/{skuId}
**Purpose**: Eliminar producto  
**Actor**: Supervisor de Inventario  
**Response**: 204 No Content  
**Error Responses**:
- 404 Not Found: Producto no existe
- 409 Conflict: Producto tiene lotes activos

### GET /api/v1/productos
**Purpose**: Consultar catálogo con disponibilidad  
**Actor**: Asesor Comercial  
**Query Params**:
- `busqueda` (opcional): Filtro por marca o presentacion
- `page` (default: 0): Número de página
- `size` (default: 20): Tamaños de página
- `sort` (default: marca,asc): Ordenamiento

**Response**: 200 OK
```json
{
  "content": [
    {
      "sku_id": "550e8400-e29b-41d4-a716-446655440000",
      "marca": "Pilsen",
      "presentacion": "Six-pack",
      "contenido_ml": 330,
      "peso_logistico_kg": 2.5,
      "stock_disponible": 150,
      "disponibilidad": "Disponible"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 245,
  "totalPages": 13
}
```

### GET /api/v1/productos/{skuId}/bitacora
**Purpose**: Consultar historial de cambios  
**Actor**: Supervisor de Inventario  
**Response**: 200 OK
```json
[
  {
    "campo": "peso_logistico_kg",
    "valor_anterior": "2.5",
    "valor_nuevo": "2.8",
    "descripcion": "Actualización por cambio de empaque",
    "fecha": "2026-04-03T19:30:00Z",
    "usuario": "supervisor01"
  }
]
```

---

## Implementation Tasks

### Phase 1: Database Schema

**T001** - Crear migración `V2__create_producto_tables.sql`
- Tabla `producto`: sku_id (UUID PK), marca, presentacion, contenido_ml, peso_logistico_kg, creado_el
- Constraint: UNIQUE(marca, presentacion)
- Tabla `bitacora_producto`: id, sku_id_ref (FK), campo, valor_anterior, valor_nuevo, descripcion, fecha, usuario
- Índices: producto.marca, producto.presentacion

**Acceptance**: Migración ejecuta sin errores, tablas creadas correctamente

---

### Phase 2: Domain Layer

**T002** - Crear entidad de dominio `Producto.java` (POJO puro, sin JPA)
- Atributos: sku_id, marca, presentacion, contenido_ml, peso_logistico_kg, creado_el
- Constructor, getters, equals/hashCode

**T003** - Crear entidad de dominio `BitacoraProducto.java`
- Atributos según spec

**T004** - Crear puerto `ProductoRepository.java` (interface)
```java
public interface ProductoRepository {
    Producto save(Producto producto);
    Optional<Producto> findById(UUID skuId);
    List<Producto> findAll(Pageable pageable);
    boolean existsByMarcaAndPresentacion(String marca, String presentacion);
    void deleteById(UUID skuId);
}
```

**T005** - Crear puerto `BitacoraProductoRepository.java`

**T006** - Crear excepciones de dominio
- `ProductoDuplicadoException`
- `ProductoNotFoundException`
- `ProductoConLotesActivosException`

**Acceptance**: Domain layer compila sin dependencias externas (ni Spring, ni JPA)

---

### Phase 3: Application Layer (Use Cases)

**T007** - Implementar `CrearProductoUseCase`
- Validar duplicado (delegar a repository)
- Generar UUID para sku_id
- Guardar producto
- Spec: 01_crear_plantilla_producto.md

**T008** - Implementar `ModificarProductoUseCase`
- Cargar producto existente
- Comparar cambios campo por campo
- Registrar BitacoraProducto por cada cambio
- Generar alerta si peso_logistico_kg cambió
- Spec: 02_modificar_plantilla_producto.md

**T009** - Implementar `EliminarProductoUseCase`
- Validar que no tenga lotes activos (query a tabla lote)
- Eliminar producto
- Spec: 02_modificar_plantilla_producto.md

**T010** - Implementar `ConsultarCatalogoUseCase`
- Buscar productos (con filtro opcional)
- Calcular stock_disponible por producto (query a tabla lote)
- Mapear a ProductoResponse
- Spec: 03_consultar_productos.md

**T011** - Implementar `ConsultarBitacoraUseCase`
- Retornar historial de cambios por sku_id
- Spec: 02_modificar_plantilla_producto.md

**Acceptance**: Use Cases pasan tests unitarios con Mockito (sin Spring)

---

### Phase 4: Infrastructure Layer (Persistence)

**T012** - Crear entidad JPA `ProductoJpaEntity.java`
- Anotaciones: @Entity, @Table, @Id, @Column
- Mapeo a tabla `producto`

**T013** - Crear entidad JPA `BitacoraProductoJpaEntity.java`
- Relación @ManyToOne a ProductoJpaEntity

**T014** - Crear `ProductoJpaRepository` (Spring Data)
```java
public interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, UUID> {
    boolean existsByMarcaAndPresentacion(String marca, String presentacion);
    List<ProductoJpaEntity> findByMarcaContainingIgnoreCaseOrPresentacionContainingIgnoreCase(
        String marca, String presentacion
    );
}
```

**T015** - Crear `ProductoRepositoryAdapter` (implementa port del domain)
- Convierte entre ProductoJpaEntity <-> Producto (domain)
- Delega operaciones a ProductoJpaRepository

**T016** - Crear `BitacoraProductoRepositoryAdapter`

**Acceptance**: Repositorios funcionan con BD real (test con @DataJpaTest)

---

### Phase 5: Infrastructure Layer (Web)

**T017** - Crear DTOs
- `ProductoRequest.java`: @NotBlank, @Positive validations
- `ProductoUpdateRequest.java`
- `ProductoResponse.java`: incluye stock_disponible, disponibilidad
- `BitacoraResponse.java`

**T018** - Implementar `ProductoController`
- `POST /api/v1/productos` → delega a CrearProductoUseCase
- `PUT /api/v1/productos/{skuId}` → delega a ModificarProductoUseCase
- `DELETE /api/v1/productos/{skuId}` → delega a EliminarProductoUseCase
- `GET /api/v1/productos` → delega a ConsultarCatalogoUseCase
- `GET /api/v1/productos/{skuId}/bitacora` → delega a ConsultarBitacoraUseCase

**T019** - Configurar `GlobalExceptionHandler` para ProductoDuplicadoException, etc.
- Mapear excepciones a códigos HTTP correctos (409, 404)

**Acceptance**: Endpoints responden correctamente (test con @SpringBootTest)

---

### Phase 6: Polish

**T020** - Implementar paginación en `GET /api/v1/productos`
- Parámetros: page, size, sort
- Response: objeto Page con metadata

**T021** - Agregar logging con SLF4J
- Log en cada operación (crear, modificar, eliminar, consultar)
- Nivel INFO para operaciones exitosas, ERROR para excepciones

**T022** - Optimizar query de stock_disponible
- Añadir índice en lote.sku_ref si no existe
- Verificar performance con 500+ productos

**Acceptance**: Carga de catálogo ≤ 3 seg (SC-025)

---

## Tests

### Unit Tests (sin Spring)

**CrearProductoUseCaseTest.java**:
- `crearProducto_exitoso()`: Verifica generación de UUID y guardado
- `crearProducto_duplicado_lanzaExcepcion()`: Verifica ProductoDuplicadoException

**ModificarProductoUseCaseTest.java**:
- `modificarProducto_exitoso()`: Verifica actualización y bitácora
- `modificarProducto_pesoLogistico_generaAlerta()`: Verifica alerta
- `modificarProducto_noExiste_lanzaExcepcion()`: Verifica 404

**Cobertura esperada**: 80%

### Integration Tests (con Spring)

**ProductoControllerTest.java**:
- `POST /api/v1/productos` con datos válidos → 201 Created
- `POST /api/v1/productos` con duplicado → 409 Conflict
- `POST /api/v1/productos` con campos inválidos → 400 Bad Request
- `PUT /api/v1/productos/{skuId}` exitoso → 200 OK con bitácora
- `DELETE /api/v1/productos/{skuId}` con lotes activos → 409 Conflict
- `GET /api/v1/productos` → 200 OK con lista paginada
- `GET /api/v1/productos?busqueda=Pilsen` → 200 OK filtrado

**Cobertura esperada**: 70%

---

## Acceptance Criteria

**From Specs**:
- ✅ SC-001: 100% de productos creados tienen SKU único generado por sistema
- ✅ SC-002: 100% de productos tienen peso_logistico_kg registrado
- ✅ SC-003: 0% de productos duplicados (marca + presentacion)
- ✅ SC-004: SKU es inmutable (no cambia en modificaciones)
- ✅ SC-005: 100% de modificaciones registran bitácora
- ✅ SC-006: Alerta emitida al modificar peso logístico
- ✅ SC-023: Asesor ve solo disponibilidad, no detalles de lotes
- ✅ SC-024: Stock_disponible es en tiempo real
- ✅ SC-025: Carga de catálogo (500 productos) ≤ 3 seg

**Technical Acceptance**:
- ✅ Domain layer sin dependencias externas
- ✅ Use Cases testeados con Mockito (sin Spring)
- ✅ Controllers testeados con @SpringBootTest
- ✅ Migraciones Flyway ejecutan correctamente
- ✅ Paginación funciona en GET /api/v1/productos

---

## Notes & Best Practices

- El `sku_id` NUNCA debe ser editable en DTOs de request
- Usar `@Transactional` en Use Cases que modifican bitácora
- El campo `alerta` en PUT response es informativo, no bloquea operación
- Peso logístico debe validarse > 0 en el DTO con `@Positive`
- Tests de integración usar testcontainers o H2 en memoria

---

**Maintainer**: Backend Team  
**Last Updated**: 2026-04-03  
**Status**: Ready for Implementation
