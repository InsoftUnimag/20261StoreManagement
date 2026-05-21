# Contratos de Integración — Módulo de Gestion de Personas.

**Fecha**: 20/05/2026  
**Actualizado**: Fusionado con especificación de Gestión de Personas  
**Elaborado por**: Equipo del modulo de Gestion de Inventario  
**Para**: Equipo Módulo de Usuarios  
**Versión**: 4.0

---

## 1. Propósito

Este documento establece los contratos que el **módulo de Gestion de Inventario** necesita del **módulo de Gestion de Personas** para funcionar correctamente. 

El módulo de Gestion de Inventario está basado en arquitectura hexagonal y código limpio, y necesita consumir datos de trabajadores y clientes para validar operaciones como crear pedidos, asignar picking, recepcionar productos y confirmar despachos.

---

## 2. Datos que el Módulo de Inventario Necesita

### 2.1 Datos del Cliente

| Campo | Tipo | Requerido | Descripción |
|-------|------|----------|-------------|
| `cedula` | String | Sí | Número de documento CC/NIT del cliente |
| `nombre` | String | Sí | Nombre completo del cliente |
| `telefono` | String | Sí | Número de teléfono de contacto |
| `email` | String | Sí | Correo electrónico |
| `direccion` | String | Sí | Dirección de entrega del cliente |
| `activo` | Boolean | Sí | Indica si el cliente está activo en el sistema |

**Casos de uso en Inventario**:
- Crear pedido: validar cliente existe y está activo
- Mostrar dirección en manifiesto de despacho
- Registrar contacto del cliente en caso de excepciones

### 2.2 Datos de los Trabajadores (Personas)

| Campo | Tipo | Requerido | Descripción |
|-------|------|----------|-------------|
| `id` | UUID | Sí | Identificador único interno |
| `identificador` | String | Sí | ID único de la persona (cedula) |
| `nombre_completo` | String | Sí | Nombre completo de la persona |
| `telefono` | String | Sí | Teléfono de contacto (10 dígitos) |
| `correo` | String | Sí | Correo electrónico válido |
| `rol` | Enum | Sí | Rol único del trabajador en el sistema |
| `estado` | Enum | Sí | Estado actual (Activo/Inactivo) |
| `fecha_creacion` | DateTime | No | Fecha de registro |

**Roles definidos**:

| Rol | Enum | Funcionalidad en Inventario |
|-----|------|----------------------------|
| Supervisor de Inventario | `SUPERVISOR_INVENTARIO` | Supervisa operaciones, asigna tareas, gestiona inventario |
| Operario de Picking | `OPERARIO_PICKING` | Prepara pedidos y confirma picking |
| Operario de Despacho | `OPERARIO_DESPACHO` | Confirma despacho de pedidos |
| Operario de Recepción | `OPERARIO_RECEPCION` | Registra ingresos de productos |
| Asesor Comercial | `ASESOR_COMERCIAL` | Crea y consulta pedidos |

---

## 3. User Stories — Gestión de Personas (Requeridas por Inventario)

### User Story 1 — Registrar Operario de Picking (Priority: P1)

Como **Modulo de gestion de inventario** necesito que **Módulo personas**, me permita registrar operarios de picking para que el sistema identifique al responsable de preparar y confirmar pedidos.

**Why this priority**: Sin operarios registrados, el módulo de gestion de inventario no puede asignar responsables a las tareas.

**Acceptance Scenarios**:

1. **Scenario**: Registro exitoso
   - **Given** el identificador no existe en el sistema
   - **When** se ingresa: identificador, nombre_completo, teléfono, correo
   - **And** se selecciona rol "Operario Picking"
   - **Then** el sistema crea el registro con estado "Activo"
   - **And** el operario aparece disponible para asignar a picking

2. **Scenario**: Identificador duplicado
   - **Given** ya existe persona con ese identificador
   - **When** se intenta registrar otra con mismo identificador
   - **Then** el sistema rechaza: "El identificador {id} ya existe"

---

### User Story 2 — Registrar Operario de Despacho (Priority: P1)

Como **Modulo de gestion de inventario** necesito que **Módulo personas**, me permita registrar operarios de despacho para que el sistema identifique al responsable de confirmar pedidos.

**Why this priority**: Sin operarios registrados, el módulo de gestion de inventario no puede asignar responsables a las tareas.

**Acceptance Scenarios**:

1. **Scenario**: Registro exitoso
   - **Given** el identificador no existe en el sistema
   - **When** se ingresa: identificador, nombre_completo, teléfono, correo
   - **And** se selecciona rol "Operario despacho"
   - **Then** el sistema crea el registro con estado "Activo"
   - **And** el operario aparece disponible para asignar a despacho

2. **Scenario**: Identificador duplicado
   - **Given** ya existe persona con ese identificador
   - **When** se intenta registrar otra con mismo identificador
   - **Then** el sistema rechaza: "El identificador {id} ya existe"

---

### User Story 3 — Registrar Operario de Recepción (Priority: P1)

Como **Módulo de gestion de inventario** necesito que **Módulo Personas**, me permita registrar operarios de recepción para que el sistema identifique al responsable de registrar ingresos de productos.

**Acceptance Scenarios**:

1. **Scenario**: Registro exitoso
   - **Given** el identificador no existe
   - **When** se ingresa datos y selecciona rol "Operario Recepción"
   - **Then** el sistema crea registro con estado "Activo"
   - **And** operario disponible para asignar a recepción

---

### User Story 4 — Registrar Supervisor de Inventario (Priority: P1)

 Como **Módulo de gestion de inventario** necesito que **Módulo Personas**, me permita registar supervisores de inventario para habilitar la gestión del inventario.

**Why this priority**: Solo los supervisores de inventario pueden acceder a gestión de inventario.

**Acceptance Scenarios**:

1. **Scenario**: Registro exitoso
   - **Given** el identificador no existe
   - **When** se ingresa datos y selecciona rol "Supervisor Inventario"
   - **Then** sistema crea registro "Activo"
   - **And** operario tiene permisos para gestionar inventario

---

### User Story 5 — Registrar Asesor Comercial (Priority: P1)

Como **Módulo de gestion de inventario** necesito que **Módulo Personas**, me permita registrar asesores comerciales para que puedan realizar pedidos y consultas.

**Acceptance Scenarios**:

1. **Scenario**: Registro exitoso
   - **Given** el identificador no existe
   - **When** se ingresa datos y selecciona rol "Asesor Comercial"
   - **Then** sistema crea registro "Activo"
   - **And** asesor puede acceder a catálogo y realizar pedidos

---

### Edge Cases

- ¿Teléfono duplicado? → No permitido.
- ¿Correo duplicado? → No permitido.
- ¿Eliminación de persona? → No es permitido.
- ¿Desactivación de persona con tareas activas?  → El sistema rechaza si tiene picking o recepcion pendiente.
- ¿Una persona con múltiples roles? → No permitido, cada persona tiene un único rol.

---

## 4. Requirements — Gestión de Personas

### Functional Requirements

| Código | Requisito |
|--------|-----------|
| FR-060 | El sistema DEBE permitir registrar personas con: identificador, nombre_completo, teléfono, correo, rol |
| FR-061 | El sistema DEBE requerir como obligatorios: identificador, nombre_completo, rol, correo_electronico |
| FR-062 | El sistema DEBE generar identificador único automáticamente si no se proporciona |
| FR-063 | El sistema DEBE validar que el identificador sea único en todo el sistema |
| FR-064 | El sistema DEBE permitir los roles: OPERARIO_PICKING, OPERARIO_DESPACHO, OPERARIO_RECEPCION, SUPERVISOR_INVENTARIO, ASESOR_COMERCIAL |
| FR-065 | El sistema DEBE inicializar el estado como "Activo" al crear |
| FR-066 | El sistema DEBE impedir eliminación de persona con tareas activas |
| FR-067 | El sistema DEBE permitir inactivar persona (cambio de estado a "Inactivo") |
| FR-068 | El sistema DEBE validar formato de teléfono (numérico, 10 dígitos) |
| FR-069 | El sistema DEBE validar formato de correo (contiene @ y dominio) |

### Garantías del Contrato (Invariantes)

- **UNICIDAD**: El identificador debe ser único en todo el sistema. No existen dos personas con el mismo identificador.
- **DISPONIBILIDAD**: Una persona activa con rol válido puede ser asignada a tareas de su rol.
- **CONSISTENCIA**: No se permite eliminar una persona que tenga tareas activas pendientes.
- **ESTADO**: Toda persona nueva se crea con estado "Activo" por defecto.

---

## 5. Endpoints que el Módulo de Usuarios Debe Exponer (Contratos)

### 5.1 Consultar Cliente por CC/NIT

**Endpoint**: `GET /api/v1/clientes/{cedula}`

**Headers requeridos**:
```
Authorization: Bearer <token_jwt>
Content-Type: application/json
```

**Response 200 — Cliente activo**:
```json
{
  "cedula": "1088245693",
  "nombre": "Juan Pérez",
  "telefono": "3001234567",
  "email": "juan.perez@email.com",
  "direccion": "Calle 45 #12-30, Bogotá",
  "activo": true
}
```

**Response 200 — Cliente inactivo**:
```json
{
  "cedula": "1088245693",
  "nombre": "Juan Pérez",
  "telefono": "3001234567",
  "email": "juan.perez@email.com",
  "direccion": "Calle 45 #12-30, Bogotá",
  "activo": false
}
```

**Response 404 — No encontrado**:
```json
{
  "mensaje": "No se encontró cliente con ese documento",
  "codigo": "CLIENTE_NO_ENCONTRADO"
}
```

---

### 5.2 Registrar Persona (Operario/Trabajador)

**Endpoint**: `POST /api/v1/personas`

**Headers requeridos**:
```
Authorization: Bearer <token_jwt>
Content-Type: application/json
```

**Request**:
```json
{
  "identificador": "80123456",
  "nombre_completo": "Carlos Mendoza",
  "telefono": "3001234567",
  "correo": "carlos.mendoza@email.com",
  "rol": "OPERARIO_PICKING"
}
```

**Response 201**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "identificador": "80123456",
  "nombre_completo": "Carlos Mendoza",
  "telefono": "3001234567",
  "correo": "carlos.mendoza@email.com",
  "rol": "OPERARIO_PICKING",
  "estado": "ACTIVO",
  "fecha_creacion": "2026-05-08T10:30:00Z"
}
```

**Response 409 — Duplicado**:
```json
{
  "mensaje": "El identificador {id} ya existe",
  "codigo": "IDENTIFICADOR_DUPLICADO"
}
```

---

### 5.3 Listar Operarios de Picking

**Endpoint**: `GET /api/v1/operarios/picking`

**Headers requeridos**:
```
Authorization: Bearer <token_jwt>
Content-Type: application/json
```

**Response 200**:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "identificador": "80123456",
    "nombre_completo": "Carlos Mendoza",
    "telefono": "3001234567",
    "correo": "carlos.mendoza@email.com",
    "rol": "OPERARIO_PICKING",
    "estado": "ACTIVO"
  }
]
```

**Importante**: 
- Retornar solo personas con rol `OPERARIO_PICKING`
- Filtrar solo personas con `estado = ACTIVO`

---

### 5.4 Listar Operarios de Despacho

**Endpoint**: `GET /api/v1/operarios/despacho`

**Headers requeridos**:
```
Authorization: Bearer <token_jwt>
Content-Type: application/json
```

**Response 200**:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "identificador": "80123456",
    "nombre_completo": "Carlos Mendoza",
    "telefono": "3001234567",
    "correo": "carlos.mendoza@email.com",
    "rol": "OPERARIO_DESPACHO",
    "estado": "ACTIVO"
  }
]
```

**Importante**: 
- Retornar solo personas con rol `OPERARIO_DESPACHO`
- Filtrar solo personas con `estado = ACTIVO`

---

### 5.5 Listar Operarios de Recepción

**Endpoint**: `GET /api/v1/operarios/recepcion`

**Headers requeridos**:
```
Authorization: Bearer <token_jwt>
Content-Type: application/json
```

**Response 200**:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440003",
    "identificador": "80123458",
    "nombre_completo": "Ana Martínez",
    "telefono": "3008765432",
    "correo": "ana.martinez@email.com",
    "rol": "OPERARIO_RECEPCION",
    "estado": "ACTIVO"
  }
]
```

**Importante**:
- Retornar solo personas con rol `OPERARIO_RECEPCION`
- Filtrar solo personas con `estado = ACTIVO`

---

### 5.6 Listar Supervisores de Inventario

**Endpoint**: `GET /api/v1/operarios/supervisores`

**Headers requeridos**:
```
Authorization: Bearer <token_jwt>
Content-Type: application/json
```

**Response 200**:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440010",
    "identificador": "80123460",
    "nombre_completo": "Pedro Ramírez",
    "telefono": "3001112233",
    "correo": "pedro.ramirez@email.com",
    "rol": "SUPERVISOR_INVENTARIO",
    "estado": "ACTIVO"
  }
]
```

---

### 5.7 Listar Asesores Comerciales

**Endpoint**: `GET /api/v1/operarios/asesores`

**Headers requeridos**:
```
Authorization: Bearer <token_jwt>
Content-Type: application/json
```

**Response 200**:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440020",
    "identificador": "80123470",
    "nombre_completo": "Laura Fernández",
    "telefono": "3002223344",
    "correo": "laura.fernandez@email.com",
    "rol": "ASESOR_COMERCIAL",
    "estado": "ACTIVO"
  }
]
```

---

### 5.8 Consultar Persona por ID

**Endpoint**: `GET /api/v1/personas/{id}`

**Headers requeridos**:
```
Authorization: Bearer <token_jwt>
Content-Type: application/json
```

**Response 200**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "identificador": "80123456",
  "nombre_completo": "Carlos Mendoza",
  "telefono": "3001234567",
  "correo": "carlos.mendoza@email.com",
  "rol": "OPERARIO_PICKING_DESPACHO",
  "estado": "ACTIVO",
  "fecha_creacion": "2026-05-08T10:30:00Z"
}
```

**Response 404 — No encontrado**:
```json
{
  "mensaje": "No se encontró persona con ese ID",
  "codigo": "PERSONA_NO_ENCONTRADA"
}
```

---

### 5.9 Inactivar Persona

**Endpoint**: `PATCH /api/v1/personas/{id}/inactivar`

**Headers requeridos**:
```
Authorization: Bearer <token_jwt>
Content-Type: application/json
```

**Response 200**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "estado": "INACTIVO"
}
```

**Response 409 — Tiene tareas activas**:
```json
{
  "mensaje": "No se puede inactivar persona con tareas activas pendientes",
  "codigo": "TIENE_TAREAS_ACTIVAS"
}
```

---
## 6. Endpoints Internos del Módulo de Gestión de Inventario
Los siguientes endpoints son ejecutados por los trabajadores registrados en Logística de Personas dentro del módulo de Inventario. No son contratos hacia afuera; se documentan aquí para trazabilidad de roles.

### 6.1 Supervisor de Inventario

| Endpoint | Metodo | Descripcion |
|-----|------|----------------------------|
| /api/v1/inventario/plantillas | `POST` | Crear plantilla del producto|
| /api/v1/inventario/plantillas/{id} | `PUT` | Modificar plantilla del producto |
| /api/v1/inventario | `GET` | Consultar inventario |
| /api/v1/manifiestos | `POST` | Crear manifiesto |
| /api/v1/manifiestos| `GET` | Listar manifiestos |
| /api/v1/picking/{id}/asignar| `PATCH` |Asignar picking a operario |
| /api/v1/despachos/{id}/asignar | `PATCH` |Asignar despacho a operario |
| /api/v1/inventario/excepciones| `POST` | Reportar excepción de inventario |

### 6.2 Asesor Comercial

| Endpoint | Metodo | Descripcion |
|-----|------|----------------------------|
| /api/v1/productos | `GET` | Consultar productos disponibles|
| /api/v1/productos/{id}/disponibilidad | `GET` | Consultar disponibilidad de producto |
| /api/v1/pedidos | `POST` | Realizar pedido |
| /api/v1/pedidos/{id} | `GET` | Ofrecer detalles del pedido|
| /api/v1/rutas/solicitar| `POST` | Solicitar ruta (integración Logística de Rutas) |

### 6.3 Operario de Recepción

| Endpoint | Metodo | Descripcion |
|-----|------|----------------------------|
| /api/v1/recepciones | `POST` | Registrar ingreso de productos|
| /api/v1/manifiestos | `GET` | Listar manifiestos |

### 6.4 Operario de Picking

| Endpoint | Metodo | Descripcion |
|-----|------|----------------------------|
| /api/v1/picking/asignados| `GET` | Listar pedidos asignados a picking|
| /api/v1/picking/{id}/confirmar| `PATCH` | Confirmar picking de pedido|

### 6.5 Operario de Despacho

| Endpoint | Metodo | Descripcion |
|-----|------|----------------------------|
| /api/v1/despachos/asignados| `GET` | Listar pedidos asignados a despacho|
| /api/v1/despachos/{id}/confirmar| `PATCH` |Confirmar despacho de pedido|


---
## 7. Autenticación

- Todos los endpoints requieren **JWT Bearer Token** en el header `Authorization`
- El token debe ser validado por el módulo de Usuarios
- Formato: `Authorization: Bearer <token_jwt>`


---

## 8. Códigos de Error

| Código HTTP | Código de Error | Significado |
|-------------|-----------------|-------------|
| 200 | — | Operación exitosa |
| 201 | — | Recurso creado exitosamente |
| 401 | `TOKEN_INVALIDO` | Token JWT inválido o expirado |
| 404 | `CLIENTE_NO_ENCONTRADO` | No existe cliente con ese documento |
| 404 | `TRABAJADOR_NO_ENCONTRADO` | No existe persona con ese ID |
| 409 | `IDENTIFICADOR_DUPLICADO` | Ya existe persona con ese identificador |
| 409 | `TIENE_TAREAS_ACTIVAS` | No se puede inactivar/eliminarr con tareas pendientes |
| 503 | `SIN_CONEXION` | Servicio no disponible (timeout) |

---

## 9. Dependencias

| Dependiente | Dependencia | Impacto si no se cumple |
|-------------------|-------------|--------------------------|
| Picking  | Requiere operarios Activos con rol OPERARIO_PICKING| Sin operarios disponibles, no se puede asignar picking |
| Despacho  | Requiere operarios Activos con rol OPERARIO_DESPACHO| Sin operarios disponibles, no se puede confirmar despacho |
| Recepción  |Requiere operarios Activos con rol OPERARIO_RECEPCION | Sin operarios disponibles, no se puede registrar ingresos |
| Inventario| Requiere supervisor Activo con rol SUPERVISOR_INVENTARIO | Sin supervisor, no se puede acceder a gestión de inventario |
| Pedidos | Requiere asesores Activos con rol ASESOR_COMERCIAL | Sin asesores, no se pueden realizar pedidos |

### Efectos en Cascada

- Si se inactiva un operario con tareas pendientes → El módulo afectado recibe excepción: "Persona no disponible"
- Si se elimina una persona → El módulo afectado pierde referencia y debe reasignar tarea

---

## 10. Qué Provee el Módulo de Gestion de Inventario a Gestion de Personas

### 10.1 Notificación de Excepciones (Inventario → Usuarios)

**Endpoint en Inventario**: `POST /api/v1/excepciones`

```json
{
  "tipo": "RECEPCION_DANADA",
  "descripcion": "Producto recibido en mal estado",
  "productoSku": "SKU-12345",
  "clienteCedula": "1088245693",
  "operarioId": "550e8400-e29b-41d4-a716-446655440001",
  "fecha": "2026-05-08T10:30:00Z"
}
```

### 10.2 Notificación de Pedido Creado (Async via RabbitMQ)

**Exchange**: `inventario.exchange`  
**Routing Key**: `pedido.creado`  
**Cola en Usuarios**: `inventario.pedido.creado`

```json
{
  "pedidoId": "uuid-pedido",
  "clienteCedula": "1088245693",
  "asesorId": "550e8400-e29b-41d4-a716-446655440020",
  "fechaCreacion": "2026-05-08T10:30:00Z"
}
```

---

## 11. Diagrama de Integración

```
┌─────────────────────────┐     REST/HTTP     ┌─────────────────────────┐
│   Módulo Inventario     │ ────────────────► │    Módulo Usuarios      │
│   (Consumer)           │ ◄───────────────  │    (Provider)          │
│                         │                   │                         │
│  Necesita:              │                   │  Debe exponer:         │
│  - Consultar cliente    │   GET /clientes   │  GET /api/v1/clientes   │
│  - Listar personas      │   GET /operarios  │  GET /api/v1/personas   │
│  - Registrar pedidos    │   POST /pedidos    │  POST /api/v1/operarios│
│  - Validar roles        │                   │                         │
└─────────────────────────┘                   └─────────────────────────┘
```

---

## 12. Resumen de Endpoints Requeridos a Logística de Personas

| # | Endpoint | Método | Descripción |
|---|----------|--------|-------------|
| 1 | `/api/v1/clientes/{cedula}` | GET | Consultar cliente por CC |
| 2 | `/api/v1/personas` | POST | Registrar nueva persona |
| 3 | `/api/v1/personas/{id}` | GET | Consultar persona por ID |
| 4 | `/api/v1/personas/{id}/inactivar` | PATCH | Inactivar persona |
| 5 | `/api/v1/operarios/picking` | GET | Listar operarios picking |
| 6 | `/api/v1/operarios/despacho` | GET | Listar operarios de despacho |
| 7 | `/api/v1/operarios/recepcion` | GET | Listar operarios de recepción |
| 8 | `/api/v1/operarios/supervisores` | GET | Listar supervisores |
| 9 | `/api/v1/operarios/asesores` | GET | Listar asesores |

---

## 13. Success Criteria

- **SC-026**: 100% de personas registradas tienen identificador único
- **SC-027**: 100% de personas tienen nombre_completo y rol registrados
- **SC-028**: 0% de identificadores duplicados
- **SC-029**: 0% de personas con tareas activas pueden ser eliminadas
- **SC-030**: El sistema debe responder con lista de personas activas por rol en < 2 segundos

---

## 14. Contacto

Para dudas o aclaraciones sobre estos contratos revisa la carpeta docs/casos_de_uso en la rama develop o contactar al equipo de Gestion de Inventario.
