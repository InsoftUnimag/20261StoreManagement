# Plan Maestro Backend - Módulo 1: Gestión de Inventario y Abastecimiento

**Date**: 2026-04-03  
**Module**: Módulo 1 - Inventario  
**Layer**: Backend (API REST + Business Logic)  
**Team**: Backend Team

---

## Summary

Plan maestro de coordinación para la implementación del backend del Módulo 1. Define las fases de desarrollo, dependencias críticas y referencias a planes detallados por feature. El backend expone una API REST con Java 21 + Spring Boot siguiendo Clean Architecture y persiste datos en PostgreSQL.

---

## Technical Context

### Stack Tecnológico

**Language/Version**: Java 21  
**Framework**: Spring Boot 3.x
- Spring Web MVC (REST API)
- Spring Data JPA (Persistencia)
- Spring Validation (Validación de DTOs)
- Spring Boot Test (Testing)

**Database**: PostgreSQL 15+  
**Migration Tool**: Flyway  
**Testing**: JUnit 5, Mockito, Spring Boot Test (`@SpringBootTest`)  
**Messaging**: RabbitMQ (para integración inter-módulos)  
**External Services**: 
- Módulo de Usuarios (HTTP REST - read-only)
- Módulo 2 - Logística (consumidor de mensajes)
- Módulo 3 - Financiero (productor de mensajes)

### Arquitectura

**Clean Architecture** con tres capas concéntricas:

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

### Project Structure

```
backend/
├── src/main/java/com/distribuidora/inventario/
│   ├── domain/
│   │   ├── model/              # Entidades puras (sin @Entity JPA)
│   │   ├── repository/         # Interfaces (puertos de salida)
│   │   └── exception/          # Excepciones del dominio
│   ├── application/
│   │   └── usecase/            # Casos de uso (lógica de aplicación)
│   └── infrastructure/
│       ├── persistence/        # Entidades JPA + implementaciones
│       ├── web/                # Controllers REST + DTOs
│       ├── messaging/          # Productores/Consumidores de cola
│       ├── external/           # Adaptadores a servicios externos
│       └── config/             # Beans de Spring
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/           # Scripts SQL (Flyway)
└── src/test/java/
    ├── application/usecase/    # Tests unitarios
    └── infrastructure/web/     # Tests de integración
```

### Performance Goals

- Confirmación de pedido: ≤ 5 seg (SC-029)
- Carga de catálogo (500 productos): ≤ 3 seg (SC-025)
- Consulta al Módulo de Usuarios: ≤ 2 seg (SC-021)
- Confirmación de picking: ≤ 5 min (SC-035)

### Constraints

- Operación de recepción debe ser **atómica** (FR-021)
- Stock comprometido NO cuenta como disponible
- Los lotes NO se comprometen al crear pedido (solo cuando Módulo 2 asigna ruta)
- SKU es inmutable una vez creado (FR-003)

---

## Implementation Phases

### Phase 1: Setup — Infraestructura Base
**Purpose**: Configurar el proyecto para que el equipo pueda desarrollar.

**Feature Plans**: N/A (setup general)

**Key Deliverables**:
- Proyecto Spring Boot con dependencias configuradas
- Base de datos PostgreSQL con Flyway
- Handler global de excepciones (`@RestControllerAdvice`)
- CORS configurado para frontend React
- Schema base de BD con todas las tablas
- Cliente RabbitMQ configurado

**Acceptance Criteria**:
- ✅ Proyecto compila sin errores
- ✅ BD se levanta con schema completo
- ✅ Migraciones Flyway ejecutan correctamente
- ✅ Handler de errores responde JSON estándar
- ✅ Tests de infraestructura pasan

**Blocking**: Esta fase es **bloqueante** para todas las demás.

---

### Phase 2: Catálogo de Productos (Specs 01-03)
**Purpose**: Implementar CRUD de productos (SKU) — base de todo el módulo.

**Feature Plan**: [`plan_gestion_sku_backend.md`](plan_gestion_sku_backend.md)

**Specs Covered**: 
- 01_crear_plantilla_producto.md
- 02_modificar_plantilla_producto.md
- 03_consultar_productos.md

**Key Deliverables**:
- Entidades: `Producto`, `BitacoraProducto`
- Use Cases: `CrearProductoUseCase`, `ModificarProductoUseCase`, `ConsultarCatalogoUseCase`
- Endpoints REST: 
  - `POST /api/productos`
  - `PUT /api/productos/{skuId}`
  - `DELETE /api/productos/{skuId}`
  - `GET /api/productos`
  - `GET /api/productos/{skuId}/bitacora`

**Acceptance Criteria**:
- ✅ Supervisor puede crear/editar SKUs
- ✅ Bitácora registra el 100% de cambios (SC-004, SC-005)
- ✅ SKU es inmutable (SC-001)
- ✅ 0% de productos duplicados (SC-003)

**Dependencies**: Phase 1 (Setup)

**Blocking**: Esta fase es **bloqueante** para Phase 3.

---

### Phase 3: Recepción de Mercancía y Excepciones (Specs 04, 16)
**Purpose**: Punto de entrada del inventario físico.

**Feature Plan**: [`plan_recepcion_excepciones_backend.md`](plan_recepcion_excepciones_backend.md)

**Specs Covered**: 
- 04_registrar_ingreso_productos.md
- 16_reportar_excepciones_inventario.md

**Key Deliverables**:
- Entidades: `Recepcion`, `Lote`, `MovimientoInventario`, `ExcepcionInventario`, `Manifiesto`
- Use Cases: `RegistrarIngresoUseCase`, `ReportarExcepcionUseCase`, `DetectarLotesVencidosUseCase`
- Endpoints REST:
  - `POST /api/recepciones`
  - `POST /api/excepciones`
  - `GET /api/manifiestos/pendientes`
- Job automático: `@Scheduled` para detectar lotes vencidos

**Dependencies**: Phase 2

**Blocking**: Esta fase es **bloqueante** para Phase 4.

---

### Phase 4: Consulta de Inventario (Specs 05, 06)
**Purpose**: Visibilidad del stock en tiempo real.

**Feature Plan**: [`plan_consulta_inventario_backend.md`](plan_consulta_inventario_backend.md)

**Specs Covered**: 
- 05_consultar_inventario.md
- 06_consultar_disponibilidad.md

**Key Deliverables**:
- Use Cases: `ConsultarInventarioUseCase`, `ConsultarDisponibilidadUseCase`
- Endpoints REST:
  - `GET /api/inventario/{skuId}`
  - `GET /api/disponibilidad?skuId={id}&cantidad={n}`

**Dependencies**: Phase 3

**Blocking**: Esta fase es **bloqueante** para Phase 5.

---

### Phase 5: Gestión de Pedidos (Specs 07-10)
**Purpose**: Núcleo comercial del módulo.

**Feature Plan**: [`plan_pedidos_backend.md`](plan_pedidos_backend.md)

**Specs Covered**: 
- 07_consultar_datos_cliente.md
- 08_realizar_pedido.md
- 09_consultar_detalle_pedido.md
- 10_listar_pedidos_comprometidos.md

**Key Deliverables**:
- Entidades: `Pedido`, `ProductoPedido`, `LoteComprometido`
- Use Cases: `ConsultarClienteUseCase`, `RealizarPedidoUseCase`, `ComprometerInventarioUseCase`, `ConsultarDetallePedidoUseCase`
- Endpoints REST:
  - `GET /api/clientes/{cc}`
  - `POST /api/pedidos`
  - `GET /api/pedidos/{numero}`
  - `GET /api/pedidos/comprometidos`

**Dependencies**: Phase 4

**Blocking**: Esta fase es **bloqueante** para Phase 6.

---

### Phase 6: Despacho y Picking (Specs 11-14)
**Purpose**: Flujo de salida física del inventario.

**Feature Plan**: [`plan_picking_despacho_backend.md`](plan_picking_despacho_backend.md)

**Specs Covered**: 
- 11_confirmar_picking.md
- 12_listar_manifiesto.md
- 13_solicitar_ruta.md
- 14_confirmar_despacho.md

**Key Deliverables**:
- Entidades: `RegistroPicking`, `RegistroDespacho`
- Use Cases: `ConfirmarPickingUseCase`, `ListarManifiestoUseCase`, `ConfirmarDespachoUseCase`
- Endpoints REST:
  - `POST /api/picking/confirmar`
  - `GET /api/manifiestos/{vehiculo}`
  - `POST /api/despachos/confirmar`

**Dependencies**: Phase 5

**Blocking**: Esta fase es **bloqueante** para Phase 7.

---

### Phase 7: Integración Inter-Módulos (Spec 15 + Mensajería)
**Purpose**: Comunicación asíncrona con Módulo 2 y Módulo 3.

**Feature Plan**: [`plan_integracion_backend.md`](plan_integracion_backend.md)

**Specs Covered**: 
- 15_ofrecer_datos_pedido.md
- Contratos de mensajería

**Key Deliverables**:
- Consumidor: `RutaAsignadaConsumer` (escucha cola Módulo 2)
- Productor: `PedidoCreadoProducer` (publica a cola Módulo 3)
- Contratos de mensajes documentados

**Dependencies**: Phase 5 y Phase 6

---

### Phase 8: Polish & Cross-Cutting
**Purpose**: Mejoras de calidad y producción.

**Key Deliverables**:
- Paginación en todos los listados
- Logging estructurado (SLF4J)
- Documentación OpenAPI/Swagger
- Métricas de performance
- Variables de entorno externalizadas

**Dependencies**: Todas las fases anteriores

---

## Dependencies Flow

```
Phase 1 (Setup)
    └── Phase 2 (Catálogo)
            └── Phase 3 (Recepción)
                    └── Phase 4 (Consulta)
                            └── Phase 5 (Pedidos)
                                    └── Phase 6 (Picking)
                                            └── Phase 7 (Integración)
                                                    └── Phase 8 (Polish)
```

---

## Feature Plans Reference

| Feature | Plan Backend | Specs | Priority |
|---------|--------------|-------|----------|
| **Catálogo SKU** | [plan_gestion_sku_backend.md](plan_gestion_sku_backend.md) | 01-03 | P1 |
| **Recepción/Excepciones** | [plan_recepcion_excepciones_backend.md](plan_recepcion_excepciones_backend.md) | 04, 16 | P1 |
| **Consulta Inventario** | [plan_consulta_inventario_backend.md](plan_consulta_inventario_backend.md) | 05-06 | P1 |
| **Pedidos** | [plan_pedidos_backend.md](plan_pedidos_backend.md) | 07-10 | P1 |
| **Picking/Despacho** | [plan_picking_despacho_backend.md](plan_picking_despacho_backend.md) | 11-14 | P1 |
| **Integración** | [plan_integracion_backend.md](plan_integracion_backend.md) | 15 | P2 |

---

## External Integrations

| Sistema | Tipo | Propósito | Tecnología |
|---------|------|-----------|------------|
| **Módulo de Usuarios** | HTTP REST | Consultar clientes | RestTemplate |
| **Módulo 2 - Logística** | Message Consumer | Recibir ruta asignada | RabbitMQ |
| **Módulo 3 - Financiero** | Message Producer | Enviar datos pedido | RabbitMQ |

---

## Testing Strategy

### Unitarios (sin Spring)
- Tests de Use Cases con Mockito
- Cobertura mínima: 80%

### Integración (con Spring)
- Tests de Controllers con `@SpringBootTest`
- Cobertura mínima: 70%

---

**Maintainer**: Backend Team Lead  
**Last Updated**: 2026-04-03  
**Status**: Active
