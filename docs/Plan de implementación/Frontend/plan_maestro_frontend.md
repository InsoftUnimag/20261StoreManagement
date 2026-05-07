# Plan Maestro Frontend - Módulo 1: Gestión de Inventario y Abastecimiento

**Date**: 2026-04-03  
**Module**: Módulo 1 - Inventario  
**Layer**: Frontend (SPA React)  
**Team**: Frontend Team

---

## Summary

Plan maestro de coordinación para la implementación del frontend del Módulo 1. Define las fases de desarrollo, dependencias con backend y referencias a planes detallados por feature. El frontend es una SPA construida con React 19 + Vite que consume la API REST del backend.

---

## Technical Context

### Stack Tecnológico

**Language/Version**: JavaScript (ES2023)  
**Framework**: React 19  
**Build Tool**: Vite 8  
**State Management**: React Context API + Hooks (useState, useEffect, useReducer)  
**HTTP Client**: Axios  
**Routing**: React Router v6  
**UI Library**: (Por definir - TailwindCSS, Material-UI, o custom)  
**Form Handling**: React Hook Form + Yup (validación)  
**Testing**: Jest + React Testing Library  

### Architecture

**Component-Based Architecture** con separación de concerns:

```
prototipo/src/
├── pages/              # Páginas (rutas principales)
│   ├── CatalogoPage.jsx
│   ├── CrearProductoPage.jsx
│   └── ...
├── components/         # Componentes reutilizables
│   ├── ProductoCard.jsx
│   ├── BitacoraTable.jsx
│   └── ...
├── services/          # Clientes HTTP (API calls)
│   ├── productoService.js
│   ├── pedidoService.js
│   └── ...
├── hooks/             # Custom hooks
│   ├── useAuth.js
│   ├── useInventario.js
│   └── ...
├── context/           # Context providers
│   ├── AuthContext.jsx
│   └── NotificationContext.jsx
├── utils/             # Helpers
│   ├── formatters.js
│   └── validators.js
└── App.jsx            # Entry point
```

### Performance Goals

- Tiempo de carga inicial: ≤ 2 seg
- Renderizado de catálogo (500 items): ≤ 1 seg
- Interacción UI (clicks): ≤ 100ms
- Actualización en tiempo real: ≤ 3 seg

### Constraints

- La app debe funcionar en Chrome, Firefox, Edge (últimas 2 versiones)
- Debe ser responsive (desktop 1920px, tablet 768px, mobile 375px)
- Accesibilidad mínima WCAG 2.1 Level A
- Todos los formularios requieren validación client-side antes de submit

---

## Implementation Phases

### Phase 1: Setup — Infraestructura Base
**Purpose**: Configurar el proyecto frontend para que el equipo pueda desarrollar.

**Feature Plans**: N/A (setup general)

**Key Deliverables**:
- Proyecto Vite + React configurado
- React Router configurado con rutas principales
- Axios configurado (base URL, interceptors)
- Layout base (header, sidebar, footer)
- Sistema de notificaciones global (toast/alert)
- Manejo de errores global (error boundary)

**Acceptance Criteria**:
- ✅ Proyecto compila y corre en `http://localhost:5173`
- ✅ Rutas principales navegables
- ✅ Llamadas HTTP al backend funcionan
- ✅ Errores se muestran al usuario

**Dependencies**: Backend Phase 1 (requiere API REST disponible)

**Blocking**: Esta fase es **bloqueante** para todas las demás.

---

### Phase 2: Módulo de Catálogo (Specs 01-03)
**Purpose**: Interfaz para gestionar productos (SKU).

**Feature Plan**: [`plan_gestion_sku_frontend.md`](plan_gestion_sku_frontend.md)

**Specs Covered**: 
- 01_crear_plantilla_producto.md
- 02_modificar_plantilla_producto.md
- 03_consultar_productos.md

**Key Deliverables**:
- **Páginas**:
  - `CatalogoPage.jsx` (listar productos con búsqueda)
  - `CrearProductoPage.jsx` (formulario crear SKU)
  - `EditarProductoPage.jsx` (formulario editar + bitácora)
- **Componentes**:
  - `ProductoCard.jsx` (tarjeta de producto)
  - `BitacoraTable.jsx` (historial de cambios)
  - `FormularioProducto.jsx` (form reutilizable)
- **Services**:
  - `productoService.js` (CRUD de productos)

**Acceptance Criteria**:
- ✅ Supervisor puede crear/editar productos desde UI
- ✅ Catálogo muestra disponibilidad en tiempo real
- ✅ Bitácora visible en página de edición
- ✅ Validaciones de formulario funcionan

**Dependencies**: Backend Phase 2

**Blocking**: NO bloqueante (se puede desarrollar en paralelo con otras fases si backend está listo)

---

### Phase 3: Módulo de Recepción (Specs 04, 16)
**Purpose**: Interfaz para recibir mercancía y reportar excepciones.

**Feature Plan**: [`plan_recepcion_excepciones_frontend.md`](plan_recepcion_excepciones_frontend.md)

**Specs Covered**: 
- 04_registrar_ingreso_productos.md
- 16_reportar_excepciones_inventario.md

**Key Deliverables**:
- **Páginas**:
  - `RecepcionPage.jsx` (registrar ingreso de lotes)
  - `ExcepcionesPage.jsx` (reportar averías/vencimientos)
- **Componentes**:
  - `FormularioRecepcion.jsx`
  - `ListaManifiestosPendientes.jsx`
  - `AlertaVencimiento.jsx` (indicador de urgencia FEFO)
- **Services**:
  - `recepcionService.js`
  - `excepcionService.js`

**Acceptance Criteria**:
- ✅ Operario puede registrar lotes desde UI
- ✅ Sistema muestra manifiestos pendientes
- ✅ Alertas de vencimiento crítico visibles
- ✅ Excepciones se reportan correctamente

**Dependencies**: Backend Phase 3

---

### Phase 4: Módulo de Inventario (Specs 05, 06)
**Purpose**: Visualización del stock en tiempo real.

**Feature Plan**: [`plan_consulta_inventario_frontend.md`](plan_consulta_inventario_frontend.md)

**Specs Covered**: 
- 05_consultar_inventario.md
- 06_consultar_disponibilidad.md

**Key Deliverables**:
- **Páginas**:
  - `InventarioPage.jsx` (consulta de inventario por SKU)
- **Componentes**:
  - `TablaLotesFEFO.jsx` (lotes ordenados por vencimiento)
  - `IndicadorDisponibilidad.jsx` (badge verde/rojo)
  - `GraficoStock.jsx` (opcional - visualización gráfica)
- **Services**:
  - `inventarioService.js`

**Acceptance Criteria**:
- ✅ Inventario muestra lotes ordenados por FEFO
- ✅ Lotes críticos resaltados visualmente (rojo/amarillo)
- ✅ Búsqueda por SKU funciona

**Dependencies**: Backend Phase 4

---

### Phase 5: Módulo de Pedidos (Specs 07-10)
**Purpose**: Interfaz para crear y consultar pedidos.

**Feature Plan**: [`plan_pedidos_frontend.md`](plan_pedidos_frontend.md)

**Specs Covered**: 
- 07_consultar_datos_cliente.md
- 08_realizar_pedido.md
- 09_consultar_detalle_pedido.md
- 10_listar_pedidos_comprometidos.md

**Key Deliverables**:
- **Páginas**:
  - `RealizarPedidoPage.jsx` (crear pedido con carrito)
  - `DetallePedidoPage.jsx` (ver detalle de pedido)
  - `ConsultarClientePage.jsx` (búsqueda de cliente)
- **Componentes**:
  - `SelectorProductos.jsx` (búsqueda + agregar al carrito)
  - `CarritoCompras.jsx` (resumen de productos seleccionados)
  - `ResumenPedido.jsx` (confirmación antes de enviar)
  - `BadgeEstadoPedido.jsx` (indicador de estado)
- **Services**:
  - `pedidoService.js`
  - `clienteService.js`

**Acceptance Criteria**:
- ✅ Asesor puede crear pedidos desde UI
- ✅ Cliente se valida antes de crear pedido
- ✅ Carrito muestra disponibilidad en tiempo real
- ✅ Estado del pedido visible ("Esperando Ruta", "Comprometido", etc.)

**Dependencies**: Backend Phase 5

---

### Phase 6: Módulo de Despacho (Specs 11-14)
**Purpose**: Interfaz para picking y despacho.

**Feature Plan**: [`plan_picking_despacho_frontend.md`](plan_picking_despacho_frontend.md)

**Specs Covered**: 
- 11_confirmar_picking.md
- 12_listar_manifiesto.md
- 13_solicitar_ruta.md
- 14_confirmar_despacho.md

**Key Deliverables**:
- **Páginas**:
  - `PickingPage.jsx` (confirmar picking de pedidos)
  - `DespachoPage.jsx` (confirmar despacho)
  - `ManifiestoPage.jsx` (ver manifiesto de carga)
- **Componentes**:
  - `ListaPedidosComprometidos.jsx`
  - `FormularioPicking.jsx` (registrar cantidades reales)
  - `ManifiestoVehiculo.jsx` (resumen de carga)
- **Services**:
  - `pickingService.js`
  - `despachoService.js`

**Acceptance Criteria**:
- ✅ Operario de Picking puede confirmar picking
- ✅ Faltantes se reportan correctamente
- ✅ Conductor puede ver manifiesto de carga
- ✅ Despacho se confirma con un clic

**Dependencies**: Backend Phase 6

---

### Phase 7: Notificaciones en Tiempo Real
**Purpose**: Sistema de notificaciones para eventos asíncronos.

**Key Deliverables**:
- Notificaciones de stock insuficiente al comprometer pedido
- Alertas de lotes vencidos
- Confirmación de asignación de ruta
- (Opcional) WebSocket para updates en tiempo real

**Dependencies**: Backend Phase 7

---

### Phase 8: Polish & UX
**Purpose**: Mejoras de experiencia de usuario.

**Key Deliverables**:
- Paginación en todas las tablas
- Filtros avanzados (por fecha, estado, etc.)
- Búsqueda con autocompletado
- Optimización de performance (lazy loading, memoization)
- Accesibilidad (navegación por teclado, ARIA labels)
- Responsive design refinado

**Dependencies**: Todas las fases anteriores

---

## Dependencies Flow

```
Phase 1 (Setup)
    ├── Phase 2 (Catálogo) ────┐
    ├── Phase 3 (Recepción) ───┤
    ├── Phase 4 (Inventario) ──┤─── Pueden desarrollarse en paralelo
    ├── Phase 5 (Pedidos) ─────┤    si backend está listo
    └── Phase 6 (Despacho) ────┘
            └── Phase 7 (Notificaciones)
                    └── Phase 8 (Polish)
```

**Nota importante**: Las fases 2-6 pueden desarrollarse en paralelo **SI** el backend correspondiente ya está disponible. Si no, deben seguir el orden del backend.

---

## Feature Plans Reference

| Feature | Plan Frontend | Specs | Backend Dependency |
|---------|---------------|-------|--------------------|
| **Catálogo SKU** | [plan_gestion_sku_frontend.md](plan_gestion_sku_frontend.md) | 01-03 | Backend Phase 2 |
| **Recepción/Excepciones** | [plan_recepcion_excepciones_frontend.md](plan_recepcion_excepciones_frontend.md) | 04, 16 | Backend Phase 3 |
| **Consulta Inventario** | [plan_consulta_inventario_frontend.md](plan_consulta_inventario_frontend.md) | 05-06 | Backend Phase 4 |
| **Pedidos** | [plan_pedidos_frontend.md](plan_pedidos_frontend.md) | 07-10 | Backend Phase 5 |
| **Picking/Despacho** | [plan_picking_despacho_frontend.md](plan_picking_despacho_frontend.md) | 11-14 | Backend Phase 6 |

---

## API Contract with Backend

El frontend consume los siguientes endpoints del backend:

### Productos
- `GET /api/productos` - Listar productos
- `POST /api/productos` - Crear producto
- `PUT /api/productos/{skuId}` - Actualizar producto
- `DELETE /api/productos/{skuId}` - Eliminar producto
- `GET /api/productos/{skuId}/bitacora` - Historial de cambios

### Recepciones
- `GET /api/manifiestos/pendientes` - Listar manifiestos pendientes
- `POST /api/recepciones` - Registrar ingreso de lotes
- `POST /api/excepciones` - Reportar excepción

### Inventario
- `GET /api/inventario/{skuId}` - Consultar inventario
- `GET /api/disponibilidad?skuId={id}&cantidad={n}` - Verificar disponibilidad

### Pedidos
- `GET /api/clientes/{cc}` - Consultar cliente
- `POST /api/pedidos` - Crear pedido
- `GET /api/pedidos/{numero}` - Consultar detalle
- `GET /api/pedidos/comprometidos` - Listar pedidos en picking

### Despacho
- `POST /api/picking/confirmar` - Confirmar picking
- `GET /api/manifiestos/{vehiculo}` - Ver manifiesto de carga
- `POST /api/despachos/confirmar` - Confirmar despacho

---

## Testing Strategy

### Unitarios (componentes)
- Tests de componentes con React Testing Library
- Tests de hooks custom
- Tests de servicios (mocks de Axios)
- Cobertura mínima: 70%

### Integración
- Tests E2E con Cypress (opcional)
- Tests de flujos completos (crear producto → ver en catálogo)

---

## Design System

### Color Palette (Por definir)
- Primary: #...
- Secondary: #...
- Success: #28a745 (verde)
- Warning: #ffc107 (amarillo)
- Danger: #dc3545 (rojo)
- Info: #17a2b8 (azul)

### Typography
- Fuente: (Por definir - Inter, Roboto, etc.)
- Tamaños: 12px, 14px, 16px, 20px, 24px

### States
- **Disponible**: Badge verde
- **Comprometido**: Badge azul
- **En Picking**: Badge amarillo
- **Despachado**: Badge gris
- **Avería**: Badge rojo
- **Vencido**: Badge rojo oscuro

---

**Maintainer**: Frontend Team Lead  
**Last Updated**: 2026-04-03  
**Status**: Active
