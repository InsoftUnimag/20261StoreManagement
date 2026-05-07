# Plan de Implementación: Gestión de Pedidos - Frontend

**Date**: 2026-04-03  
**Specs**: 07_consultar_datos_cliente.md · 08_realizar_pedido.md · 09_consultar_detalle_pedido.md · 10_consultar_lista_pedidos.md  
**Module**: Módulo 1 - Inventario  
**Layer**: Frontend  
**Priority**: P1

---

## Summary

Interfaces para consultar clientes, crear pedidos, y consultar pedidos (lista y detalle). Flujo completo: Buscar cliente → Crear pedido → Ver detalle con estados (Esperando Ruta → Comprometido → En Picking → Despachado).

---

## Dependencies

**Blocked by**:
- Plan Pedidos Backend (endpoints REST)
- Plan Gestión SKU Frontend (selector de productos)

---

## Pages & Components

### 1. BuscarClientePage
- Input para cédula del cliente
- Botón "Buscar" → GET /api/v1/clientes/{cedula}
- Card con datos del cliente (nombre, teléfono, dirección, email)
- Botón "Realizar Pedido" → navigate a RealizarPedidoPage con cliente_cc

### 2. RealizarPedidoPage
- Formulario multi-step wizard:
  1. **Datos del cliente**: Pre-llenado (read-only)
  2. **Líneas del pedido**:
     - Autocomplete para buscar productos (por marca)
     - Input cantidad (validar > 0)
     - Botón "Agregar Línea"
     - Tabla de líneas agregadas con botón eliminar
  3. **Revisión**: Resumen del pedido con total unidades
  4. **Confirmación**: POST /api/v1/pedidos
- Validación: Stock disponible antes de submit (GET /api/v1/inventario/stock)
- Success: Navigate a DetallePedidoPage con toast
- Error 409 (stock insuficiente): Mostrar modal con detalle de SKUs sin stock

### 3. ListaPedidosPage
- Filtros: Estado, Cliente (cédula), Número pedido, Rango fechas
- Tabla: Número, Cliente, Fecha, Estado (badge coloreado), Total unidades, Acciones
- Paginación (20 pedidos por página)
- Click en fila → navigate a DetallePedidoPage

### 4. DetallePedidoPage
- Card principal: Número pedido, Estado (badge), Fechas (creación, compromiso)
- Sección Cliente: Nombre, Teléfono, Dirección
- Sección Líneas:
  - Tabla: Producto, Cantidad Solicitada, Cantidad Confirmada
  - Si estado >= Comprometido: Expandir línea para ver lotes comprometidos (código, vencimiento, cantidad)
- Timeline de estados: Esperando Ruta → Comprometido → En Picking → Despachado
- Botón "Volver a Lista"

---

## Services

**ClienteService**:
- `buscarCliente(cedula)` → GET /api/v1/clientes/{cedula}

**PedidoService**:
- `crearPedido(data)` → POST /api/v1/pedidos
- `getPedidos(filtros, page, size)` → GET /api/v1/pedidos
- `getDetallePedido(id)` → GET /api/v1/pedidos/{id}

---

## Key Components

**ClienteCard**: Card con datos del cliente
**PedidoWizard**: Wizard multi-step (Stepper component)
**LineaPedidoForm**: Form para agregar líneas al pedido
**EstadoBadge**: Badge coloreado según estado pedido
**PedidoTimeline**: Timeline de estados del pedido
**LotesComprometidosTable**: Tabla expandible con lotes por línea

---

## Implementation Tasks

**T001-T003**: Crear services (ClienteService, PedidoService)
**T004-T009**: Crear components (ClienteCard, PedidoWizard, LineaPedidoForm, EstadoBadge, PedidoTimeline, LotesComprometidosTable)
**T010-T013**: Implementar pages (BuscarClientePage, RealizarPedidoPage, ListaPedidosPage, DetallePedidoPage)
**T014-T016**: Configurar routing, validaciones complejas, styling, testing

---

## Tests
- Services: 6 tests
- Components: 8 tests
- Pages: 6 tests

---

## Acceptance Criteria

**FR-052**: Solo clientes activos pueden realizar pedidos.
**FR-053**: Asesor busca cliente por cédula antes de crear pedido.
**FR-054**: Sistema valida stock disponible antes de confirmar pedido.
**FR-055**: Si stock insuficiente, rechazo inmediato con mensaje detallado.
**FR-056**: Pedido creado queda en estado Esperando Ruta.
**SC-029**: Confirmación de pedido ≤ 5 segundos (loading spinner mientras procesa).
