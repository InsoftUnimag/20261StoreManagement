# Actores del Módulo 1 – Gestión de Inventario y Abastecimiento

El Módulo 1 es responsable del control total del ciclo de vida del producto: desde la catalogación y recepción de mercancía, hasta la gestión de pedidos, picking y despacho final. Interactúa con módulos externos para la validación de usuarios y la liquidación financiera.

---

## Actores Humanos

### 1. Supervisor de Inventario
**Rol:** Administrativo y de control maestro de existencias.
**Responsabilidades:**
- **Maestros de Producto:** Crear y modificar SKUs (marca, presentación, peso logístico).
- **Control de Calidad:** Gestionar y reportar excepciones de inventario (averías o vencimientos) detectadas en bodega y recibir notificaciones de las reportadas por otros operarios.
- **Auditoría:** Consultar inventario global por lotes (FEFO) y bitácoras de movimientos.

### 2. Operario de Recepción
**Rol:** Operativo – Gestión de entrada de mercancía.
**Responsabilidades:**
- **Recepción:** Consultar los manifiestos de fábrica y registrar el ingreso de productos validando contra los mismos.
- **Trazabilidad:** Ingresar datos de lote (cantidad, fecha de vencimiento y expedición) bajo criterios FEFO.
- **Incidencias:** Reportar discrepancias de cantidad o estado físico en la entrega mediante el flujo de excepciones.

### 3. Asesor Comercial
**Rol:** Gestión de ventas y atención al cliente.
**Responsabilidades:**
- **Venta:** Consultar catálogo y disponibilidad de stock real.
- **Pedidos:** Realizar pedidos (acción que dispara automáticamente el envío de datos al Módulo 2 para asignación de ruta).
- **Seguimiento:** Recibir confirmaciones de número de pedido y notificaciones de cambios en la disponibilidad para informar al cliente.

### 4. Operario de Picking
**Rol:** Operativo – Gestión de alistamiento físico.
**Responsabilidades:**
- **Preparación:** Consultar pedidos comprometidos y realizar el picking siguiendo criterios FEFO.
- **Confirmación:** Registrar la recolección física para pasar el pedido a estado "En Picking".
- **Novedades:** Reportar averías o faltantes detectados durante la preparación.

### 5. Operario de Despacho
**Rol:** Operativo – Responsable del control de salida física.
**Responsabilidades:**
- **Validación de Carga:** Confirmar que los pedidos "En Picking" han sido cargados correctamente en el vehículo asignado.
- **Confirmación de Salida:** Registrar la salida física del pedido de la bodega (cambio de estado a "Despachado") para cerrar el ciclo del stock en el Módulo 1.
- **Gestión de Despachos Parciales:** Reportar excepciones si el pedido sale incompleto por novedades de último momento.

---

## Actores de Sistema

### 6. Módulo de Usuarios (Externo)
**Tipo:** Servicio externo de identidad.
**Interacción:** Provee la información maestra y el estado de los clientes (Activo/Inactivo) para permitir o denegar la creación de pedidos.

### 7. Módulo 2 – Logística (Asignación)
**Tipo:** Integración de procesos.
**Interacción:** Provee la asignación de ruta y fecha de recogida para disparar el compromiso automático de lotes FEFO en el Módulo 1.

### 8. Módulo 3 – Financiero (Liquidación)
**Tipo:** Sistema externo de liquidación.
**Interacción:** Recibe de forma asíncrona los datos de pedidos creados y despachados para la liquidación comercial y de fletes.
