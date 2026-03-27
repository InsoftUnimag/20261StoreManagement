# Implementation Plan: Gestión de Plantilla de Producto (SKU)

**Date**: 2026-03-27
**Specs**: [`01_crear_plantilla_producto.md`](../especificaciones/01_crear_plantilla_producto.md) · [`02_modificar_plantilla_producto.md`](../especificaciones/02_modificar_plantilla_producto.md) · [`03_consultar_productos.md`](../especificaciones/03_consultar_productos.md)

---

## Summary

Implementación del CRUD de productos (SKU) del catálogo. Este feature es la base de todo el módulo: sin `Producto`, no existen lotes, recepciones, ni pedidos. El backend expone tres endpoints REST que el frontend React consume. Se registra bitácora automática en cada modificación to garantizar trazabilidad completa.

---

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.x, Spring Data JPA, Spring Validation, PostgreSQL Driver
**Storage**: PostgreSQL — tablas `producto`, `bitacora_producto`
**Testing**: JUnit 5, Mockito, Spring Boot Test (`@SpringBootTest`)
**Target Platform**: REST API (Spring Boot) + SPA (React + Vite)
**Performance Goals**: Carga del catálogo ≤ 3 seg para 500 productos (SC-025)
**Constraints**:
- El `sku_id` es autogenerado e inmutable una vez creado (FR-003, FR-008)
- Unicidad sobre combinación `marca + presentacion` (FR-005, FR-010)
- No se puede eliminar un `Producto` con lotes activos (FR-011)

---

## Project Structure

### Arquitectura: Clean Architecture

```text
backend/src/main/java/com/distribuidora/modulo1/
│
├── domain/                          # Capa más interna — sin dependencias externas
│   ├── model/
│   │   ├── Producto.java            # Entidad pura (sin @Entity JPA)
│   │   └── BitacoraProducto.java
│   ├── port/                        # Interfaces (puertos de salida)
│   │   ├── ProductoRepository.java
│   │   └── BitacoraProductoRepository.java
│   └── exception/
│       ├── ProductoDuplicadoException.java
│       └── ProductoConLotesActivosException.java
│
├── application/                     # Casos de uso — solo depende de domain
│   └── usecase/
│       ├── CrearProductoUseCase.java
│       ├── ModificarProductoUseCase.java
│       └── ConsultarCatalogoUseCase.java
│
└── infrastructure/                  # Capa externa — contiene toda la tecnología
    ├── persistence/                 # Adaptadores de repositorio (JPA)
    │   ├── ProductoJpaEntity.java
    │   ├── ProductoJpaRepository.java   (Spring Data)
    │   └── ProductoRepositoryAdapter.java  (implementa el port)
    ├── web/                         # Controllers REST + DTOs
    │   ├── ProductoController.java
    │   ├── dto/
    │   │   ├── ProductoRequest.java
    │   │   ├── ProductoUpdateRequest.java
    │   │   ├── ProductoResponse.java
    │   │   └── BitacoraResponse.java
    │   └── exception/
    │       └── GlobalExceptionHandler.java
    └── config/

backend/src/main/resources/db/migration/
└── V2__create_producto_tables.sql

backend/src/test/java/com/distribuidora/modulo1/
├── application/usecase/             # Tests unitarios (sin Spring — Mockito puro)
│   ├── CrearProductoUseCaseTest.java
│   └── ModificarProductoUseCaseTest.java
└── infrastructure/web/              # Tests de integración (@SpringBootTest)
    └── ProductoControllerTest.java

prototipo/src/
├── pages/
│   ├── CatalogoPage.jsx             # Asesor Comercial — Spec 03
│   ├── CrearProductoPage.jsx        # Supervisor — Spec 01
│   └── EditarProductoPage.jsx       # Supervisor — Spec 02
├── components/
│   ├── ProductoCard.jsx
│   └── BitacoraTable.jsx
└── services/
    └── productoService.js           # fetch wrapper para /api/productos
```

**Structure Decision**: Clean Architecture. El `domain` contiene la entidad `Producto` como POJO puro (sin anotaciones JPA). Los `use cases` en application implementan una interfaz por caso de uso y reciben los repositories como dependencias por inyección. La capa `infrastructure/persistence` adapta Spring Data JPA al port del domain. Los tests unitarios de casos de uso corren sin Spring (solo Mockito).

---

## Phase 1: Setup de tablas (Migración BD)

**Purpose**: Definir el schema en PostgreSQL antes de escribir código Java.

- [ ] T001 Crear `V2__create_producto_tables.sql` con:
  - Tabla `producto`: `sku_id` (UUID autogenerado), `marca`, `presentacion`, `contenido_ml`, `peso_logistico_kg`, constraint UNIQUE(`marca`, `presentacion`)
  - Tabla `bitacora_producto`: `id`, `sku_id_ref`, `campo`, `valor_anterior`, `valor_nuevo`, `descripcion`, `fecha_modificacion`

---

## Phase 2: Foundational — Modelos y Repositorios

**Purpose**: Crear las clases base que todos los User Stories de este feature necesitan.

- [ ] T002 Crear entidad `Producto.java` con anotaciones JPA (`@Entity`, `@Table`, `@Column`)
- [ ] T003 Crear entidad `BitacoraProducto.java` con relación `@ManyToOne` a `Producto`
- [ ] T004 Crear `ProductoRepository.java` extendiendo `JpaRepository<Producto, UUID>`
  - Añadir método: `existsByMarcaAndPresentacion(String marca, String presentacion)`
  - Añadir método: `findByMarcaContainingIgnoreCaseOrPresentacionContainingIgnoreCase()`
- [ ] T005 Crear `BitacoraProductoRepository.java` extendiendo `JpaRepository`
- [ ] T006 Definir DTOs:
  - `ProductoRequest.java`: marca, presentacion, contenido_ml, peso_logistico_kg (con `@NotBlank`, `@Positive`)
  - `ProductoUpdateRequest.java`: mismo campos + descripcion del cambio
  - `ProductoResponse.java`: todos los campos + sku_id + stock_disponible (calculado)
  - `BitacoraResponse.java`: campo, valor_anterior, valor_nuevo, descripcion, fecha

**Checkpoint**: El proyecto compila con las nuevas entidades y se puede ejecutar la migración.

---

## Phase 3: User Story 1 — Crear Plantilla de Producto (Spec 01, P1)

**Goal**: El Supervisor de Inventario puede registrar un nuevo producto y el sistema genera su SKU único automáticamente.

**Independent Test**: `POST /api/productos` con datos válidos → responde 201 con SKU autogenerado, stock = 0.

- [ ] T007 [US1-Spec01] Implementar `ProductoService.crearProducto(ProductoRequest)`:
  - Validar que no exista combinación `marca + presentacion`
  - Guardar entidad con `sku_id` = `UUID.randomUUID()`
  - Retornar `ProductoResponse` con stock = 0
- [ ] T008 [US1-Spec01] Implementar `POST /api/productos` en `ProductoController`
  - Respuesta: `201 Created` con body `ProductoResponse`
  - Error duplicado: `409 Conflict` con mensaje y SKU existente
  - Error campos vacíos: `400 Bad Request` por validación `@Valid`
- [ ] T009 [US1-Spec01] Test unitario `ProductoServiceTest.crearProducto_exitoso()`
- [ ] T010 [US1-Spec01] Test unitario `ProductoServiceTest.crearProducto_duplicado_lanzaExcepcion()`
- [ ] T011 [US1-Spec01] Test de integración `ProductoControllerTest` para `POST /api/productos`
- [ ] T012 [US1-Spec01] Frontend: `CrearProductoPage.jsx` con formulario y llamada a `productoService.crear()`

**Checkpoint**: El Supervisor puede crear productos. `SC-001`, `SC-002`, `SC-003` verificados.

---

## Phase 4: User Story 1 — Modificar Plantilla de Producto (Spec 02, P2)

**Goal**: El Supervisor puede editar atributos de un producto sin cambiar el SKU, con registro de bitácora por cada campo modificado.

**Independent Test**: `PUT /api/productos/{skuId}` modificando `peso_logistico_kg` → SKU sin cambios, bitácora registrada, alerta emitida.

- [ ] T013 [US1-Spec02] Implementar `ProductoService.modificarProducto(UUID skuId, ProductoUpdateRequest)`:
  - Cargar producto por `skuId` o lanzar `404 Not Found`
  - Validar que la nueva combinación `marca + presentacion` no genere duplicado
  - Por cada campo modificado → registrar `BitacoraProducto` (campo, anterior, nuevo, descripcion, fecha)
  - Si se modifica `peso_logistico_kg` → incluir alerta en la respuesta (campo `alerta` en el DTO)
  - Guardar producto actualizado
- [ ] T014 [US1-Spec02] Implementar `PUT /api/productos/{skuId}` en `ProductoController`
- [ ] T015 [US1-Spec02] Implementar `DELETE /api/productos/{skuId}` en `ProductoController`
  - Verificar que el producto no tiene lotes activos (estado DISPONIBLE o COMPROMETIDO)
  - Si tiene lotes activos: `409 Conflict` con mensaje "posee dependencias operativas activas"
- [ ] T016 [US1-Spec02] Implementar `GET /api/productos/{skuId}/bitacora` para consultar historial
- [ ] T017 [US1-Spec02] Tests unitarios para `modificarProducto` (exitoso, duplicado, con alerta peso)
- [ ] T018 [US1-Spec02] Tests de integración para `PUT` y `DELETE`
- [ ] T019 [US1-Spec02] Frontend: `EditarProductoPage.jsx` con formulario pre-cargado y sección de bitácora

**Checkpoint**: `SC-004`, `SC-005`, `SC-006` verificados. El SKU es inmutable. La bitácora registra el 100% de los cambios.

---

## Phase 5: User Story 1 — Consultar Catálogo (Spec 03, P2)

**Goal**: El Asesor Comercial puede ver el catálogo con disponibilidad en tiempo real, sin ver detalles de lotes.

**Independent Test**: `GET /api/productos` → lista con `disponibilidad` calculada solo sobre lotes DISPONIBLE, sin fechas de vencimiento visibles.

- [ ] T020 [US1-Spec03] Implementar `ProductoService.consultarCatalogo(String busqueda)`:
  - Query que calcula `stock_disponible` como `SUM` de lotes en estado `DISPONIBLE`
  - Retornar `ProductoResponse` con campo `disponibilidad` = "Disponible" / "No disponible"
  - NO incluir detalles de lotes (código, fecha vencimiento)
- [ ] T021 [US1-Spec03] Implementar `GET /api/productos?busqueda={texto}` en `ProductoController`
- [ ] T022 [US1-Spec03] Tests de integración para `GET /api/productos` (con y sin filtro de búsqueda)
- [ ] T023 [US1-Spec03] Frontend: `CatalogoPage.jsx` con búsqueda en tiempo real y badge de disponibilidad

**Checkpoint**: `SC-023`, `SC-024`, `SC-025` verificados. El Asesor ve solo disponibilidad, nunca detalles de lotes.

---

## Phase 6: Polish de este Feature

- [ ] T024 Paginación en `GET /api/productos` (page, size, sort)
- [ ] T025 Logs con `Slf4j` en todas las operaciones del `ProductoService`
- [ ] T026 Revisar tiempo de respuesta del catálogo con 500+ productos en BD (SC-025 ≤ 3 seg)

---

## Dependencies & Execution Order

- **T001** (migración BD) debe completarse antes que T002-T006
- **T002–T006** (foundational) deben completarse antes que cualquier User Story
- **T007–T012** (Spec 01 — Crear) deben completarse antes que T013-T019 (Spec 02 — Modificar), ya que `modificar` depende de que `crear` funcione
- **T020–T023** (Spec 03 — Catálogo) puede desarrollarse en paralelo con Spec 02, ya que solo hace lectura

## Notes

- El `sku_id` NUNCA debe ser expuesto como editable en ningún DTO de request
- Los tests de integración deben usar una BD H2 en memoria o testcontainers/PostgreSQL de test
- El campo `alerta` en la respuesta de modificar peso logístico es informativo, no bloquea la operación
