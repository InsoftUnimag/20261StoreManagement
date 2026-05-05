
# FEAT-011: Confirmar Despacho
**Created**: 03/03/2026

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Confirmar salida física del pedido de bodega (Priority: P1)
Como **Operario de Despacho**, necesito confirmar que el pedido fue cargado al vehículo y salió físicamente de la bodega, para actualizar el inventario, cerrar el ciclo del stock en el Módulo 1.

**Why this priority**: Es el último punto de control del inventario físico en bodega. Sin esta confirmación, el inventario lógico permanece en estado "En Picking" indefinidamente, generando inconsistencias entre módulos.

**Independent Test**: Tomar un pedido en estado "En Picking", confirmar despacho, verificar que el pedido cambian a "Despachado", se registra MovimientoInventario tipo "Salida" por pedido.

**Acceptance Scenarios**:

1. **Scenario**: Despacho exitoso de pedido completo
   - **Given** existe un pedido en estado "En Picking" con todos sus productos en estado "Picking"
   - **When** el Operario confirma el despacho del pedido
   - **Then** todos los productos del pedido cambian a estado "Despachado"
   - **And** se registra MovimientoInventario tipo "Salida" por cada pedido
   - **And** el pedido cambia a estado "Despachado"

2. **Scenario**: Despacho de pedido con productos faltantes
   - **Given** pedido en estado "En Picking" hacen falta unidades
   - **When** el Operario reporta una excepción de inventario y confirma el despacho
   - **Then** solo las unidades efectivamente recogidas cambian a "Despachado"
   - **And** se registra MovimientoInventario tipo "Salida" por las cantidades reales
   - **And** el pedido se despacha con indicador "Parcial"

3. **Scenario**: Intento de despachar pedido en estado incorrecto
   - **Given** el pedido NO está en estado "En Picking"
   - **When** el Operario intenta confirmar el despacho
   - **Then** el sistema rechaza: "El pedido {número} no está listo para despacho. Estado actual: {estado}"
   - **And** no se modifica ningún lote ni inventario

4. **Scenario**: Dos operarios intentan despachar el mismo pedido
   - **Given** un Operario ya inició la confirmación de despacho
   - **When** un segundo Operario intenta confirmar el mismo pedido
   - **Then** el sistema rechaza al segundo: "Este pedido ya está siendo procesado por otro operario"
   - **And** solo el primer operario puede completar la operación

5. **Scenario**: Módulo 2 confirma entrega al cliente final
   - **Given** pedido en estado "Despachado"
   - **When** el sistema recibe confirmación de entrega del Módulo 2
   - **Then** el pedido cambia a estado "Entregado"
   - **And** los productos permanecen en estado "Entregado"
   - **And** se registra la fecha y hora de entrega confirmada en el pedido

---

### Edge Cases
- ¿El despacho es reversible? No directamente.
- ¿Qué pasa si se pierde conexión al confirmar? Operación atómica: si no se completa, ningún lote cambia de estado.
- ¿El Módulo 2 puede bloquear el despacho? No. El Módulo 2 es notificado, no aprueba. La confirmación es responsabilidad exclusiva del Operario.

---

## Requirements *(mandatory)*

### Functional Requirements
- **FR-081**: El sistema DEBE permitir confirmar despacho solo de pedidos en estado "En Picking".
- **FR-082**: El sistema DEBE cambiar pedidos de "Picking" a "Despachado" al confirmar.
- **FR-083**: El sistema DEBE registrar MovimientoInventario tipo "Salida" por cada pedido despachado.
- **FR-084**: El sistema DEBE cambiar estado del pedido a "Despachado" al confirmar.
- **FR-085**: El sistema DEBE soportar despacho parcial, registrando cantidades reales vs. solicitadas.
- **FR-086**: El sistema DEBE garantizar operación atómica (sin estados parciales si falla).
- **FR-087**: El sistema DEBE controlar concurrencia: un pedido solo puede ser despachado por un operario a la vez.
- **FR-088**: El sistema DEBE recibir confirmación de entrega del Módulo 2 y actualizar estado del pedido a "Entregado".
- **FR-089**: El sistema DEBE registrar fecha y hora de entrega confirmada en el pedido.

### Key Entities
- **RegistroDespacho**: pedido_ref, operario, fecha_despacho, tipo (Completo | Parcial).
- **MovimientoInventario**: tipo "Salida", lote_ref, cantidad, pedido_ref, fecha, usuario.

---

## Success Criteria *(mandatory)*
- **SC-037**: 100% de pedidos con despacho confirmado cambian "En Picking" → "Despachado".
- **SC-038**: 100% de despachos generan MovimientoInventario tipo "Salida".
- **SC-039**: 0% de pedidos en estado diferente a "En Picking" aceptan confirmación de despacho.
- **SC-041**: Operación atómica garantizada: 0% de estados parciales ante fallo.
- **SC-042**: 100% de confirmaciones de entrega del Módulo 2 actualizan el pedido a "Entregado".
- **SC-043**: 100% de productos del pedido cambian de estado "Picking" → "Entregado" tras confirmación de entrega.