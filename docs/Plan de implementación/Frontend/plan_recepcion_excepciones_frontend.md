# Plan de Implementación: Recepción y Excepciones - Frontend

**Date**: 2026-04-03  
**Specs**: 04_registrar_recepcion.md · 16_registrar_excepcion.md  
**Module**: Módulo 1 - Inventario  
**Layer**: Frontend  
**Priority**: P1


---
## Summary

Interfaces para registrar recepción de mercancía contra manifiestos y registrar excepciones de inventario (averías, vencimientos, faltantes, diferencias). Consume endpoints de recepción y excepciones del backend.

---

## Dependencies

**Blocked by**:
- Plan Gestión SKU Frontend (requiere selector de productos)
- Plan Recepción Backend (endpoints REST)

---

## Pages & Components

### 1. ManifiestosPendientesPage
- Lista de manifiestos con estado Pendiente/Parcial
- Tabla: Número, Proveedor, Fecha Emisión, Progreso (barra)
- Botón "Registrar Recepción" → navigate a RegistrarRecepcionPage con manifiesto_id

### 2. RegistrarRecepcionPage
- Formulario multi-step:
  1. Seleccionar manifiesto (opcional, puede ser sin manifiesto)
  2. Si manifiesto seleccionado: Mostrar líneas esperadas
  3. Para cada SKU recibido: ingresar codigo_lote, fecha_vencimiento, cantidad_recibida
  4. Botón "Agregar Línea" para recepción sin manifiesto
  5. Confirmación y submit
- POST /api/v1/recepciones
- Success: Navigate a lista de recepciones con toast + alerta si hubo diferencias
- Error: Mostrar errores de validación

### 3. ExcepcionesPage
- Lista de excepciones con filtros (tipo, estado, fecha)
- Tabla: Fecha, Tipo, SKU, Lote, Cantidad, Operario, Estado
- Botón "Registrar Excepción"
- Click en fila → modal con detalle completo

### 4. RegistrarExcepcionPage
- Formulario:
  - Tipo: select (Avería, Vencimiento, Faltante)
  - Producto: autocomplete (buscar por marca)
  - Lote: select (cargar lotes del producto con stock > 0)
  - Cantidad: number input
  - Descripción: textarea (required)
  - Evidencia: file upload (imagen, opcional)
- POST /api/v1/excepciones
- Success: Navigate a lista con toast

---

## Services

**RecepcionService**:
- `getManifiestosPendientes()`
- `getDetallesManifiesto(id)`
- `registrarRecepcion(data)`

**ExcepcionService**:
- `getExcepciones(filtros, page)`
- `getDetalleExcepcion(id)`
- `registrarExcepcion(data)`
- `cambiarEstadoExcepcion(id, nuevoEstado)`

---

## Key Components

**RecepcionForm**: Formulario multi-línea para registrar recepción
**ExcepcionForm**: Formulario para registrar excepción
**ManifiestoCard**: Card con información y progreso de manifiesto
**ExcepcionBadge**: Badge coloreado según tipo/estado

---

## Implementation Tasks

**T001-T003**: Crear services (RecepcionService, ExcepcionService)
**T004-T007**: Crear components (RecepcionForm, ExcepcionForm, ManifiestoCard, ExcepcionBadge)
**T008-T011**: Implementar pages (ManifiestosPendientesPage, RegistrarRecepcionPage, ExcepcionesPage, RegistrarExcepcionPage)
**T012-T014**: Configurar routing, styling, testing

---

## Tests
- Services: 6 tests
- Components: 6 tests
- Pages: 4 tests

---

## Acceptance Criteria

**FR-019**: Operarios pueden seleccionar manifiesto pendiente para registrar recepción.
**FR-020**: Sistema detecta automáticamente diferencias y muestra alerta.
**FR-021**: Recepción es atómica (success o rollback completo).
**FR-094**: Operarios pueden registrar excepciones tipo Avería con descripción obligatoria.
**FR-095**: Operarios pueden registrar excepciones tipo Vencimiento.
