# Actores del Módulo 1 – Gestión de Inventario

El Módulo 1 es responsable del control del inventario dentro del Centro de Distribución, desde la recepción de mercancía hasta su preparación para despacho. Este módulo no gestiona transporte ni facturación; interactúa con otros módulos del sistema para completar el flujo operativo.

---

## Actores Humanos

### 1. Jefe de Bodega

**Rol:** Administrativo y de control del inventario.

**Responsabilidades:**

- Crear SKU.
- Modificar SKU.
- Consultar inventario global.
- Reportar avería.
- Registrar baja definitiva de productos vencidos.

---

### 2. Operario de Recepción

**Rol:** Operativo – Gestión de entrada de mercancía.

**Responsabilidades:**

- Registrar recepción de mercancía.
- Verificar cantidad recibida contra el manifiesto de fábrica.
- Seleccionar SKU existente o crear uno nuevo si no existe.
- Ingresar lote.
- Ingresar fecha de vencimiento.
- Reportar avería detectada durante la recepción.

---

### 3. Cliente

**Rol:** Actor externo que realiza pedidos al Centro de Distribución.

**Responsabilidades:**

- Registrarse en el sistema.
- Consultar productos disponibles.
- Realizar pedido.
- Consultar estado de su pedido.

---

### 4. Operario de Picking

**Rol:** Operativo – Gestión de salida física.

**Responsabilidades:**

- Consultar órdenes de picking pendientes.
- Confirmar picking.
- Reportar avería detectada durante la manipulación.

---

## Actores de Sistema

### 5. Módulo 2 – Logística

**Tipo:** Sistema externo.

**Interacciones con el Módulo 1:**

- Consultar pedidos listos para despacho.
- Confirmar despacho.

El estado de entrega final es gestionado exclusivamente por el Módulo 2.

---

### 6. Módulo 3 – Financiero

**Tipo:** Sistema externo.

**Interacciones con el Módulo 1:**

- Consultar detalle de pedido despachado para facturación.

El Módulo 3 no modifica inventario ni estados de pedido.