# FEAT-008: Realizar Pedido
**Created**: 03/03/2026 

> ⚠️ **Nota de flujo**: El pedido se crea en estado "Esperando Ruta" **sin comprometer lotes**. Los lotes se comprometen únicamente cuando el logística de transporte (Módulo 2) envía la señal de asignación de ruta.

> ⚠️ **Nota de flujo**: El pedido requiere saber datos del cliente (Cedula/id, dirección de entrega) para poder realizar el pedido y enviarse a la logística de transporte.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Crear pedido de productos (Priority: P1)
Como **Asesor Comercial**, necesito realizar un pedido para que el sistema registre mi solicitud y la ponga en cola para asignación de ruta por logística de trasnporte.

**Why this priority**: La creación del pedido es el punto de activación de toda la operación logística. Priorizar esta funcionalidad asegura que la solicitud quede registrada de forma inmediata y veraz, permitiendo a logística de transporte planificar la ruta antes de comprometer el inventario.

**Independent Test**: Crear pedido con stock suficiente, verificar que el pedido queda en "Esperando Ruta" SIN comprometer lotes, y que el stock disponible no cambia en este momento.

**Acceptance Scenarios**:

1. **Scenario**: Pedido exitoso — Paso 1: Registro del pedido
   - **Given** Asesor Comercial asocia el cliente al pedido
   - **And** stock disponible cubre las cantidades solicitadas
   - **When** Asesor Comercial selecciona productos con cantidades y confirma
   - **Then** sistema ejecuta automáticamente "Consultar disponibilidad"
   - **And** verifica que stock cubre cantidades solicitadas
   - **And** genera número de pedido único
   - **And** pedido queda en estado "Esperando Ruta"
   - **And** los lotes NO son comprometidos en este momento
   - **And** Asesor Comercial recibe confirmación con número y detalle del pedido

2. **Scenario**: Pedido exitoso — Paso 2: Compromiso de inventario activado (ruta asignada)
   - **Given** pedido en estado "Esperando Ruta"
   - **And** ya asignó ruta y fecha de despacho
   - **When** logística de transporte envia la señal de asignación de ruta
   - **Then** sistema re-verifica disponibilidad de stock en ese momento
   - **And** reserva lotes siguiendo FEFO → estado "Comprometido"
   - **And** pedido cambia a estado "Comprometido"
   - **And** registra movimiento de inventario tipo "Compromiso" por cada lote

3. **Scenario**: Stock insuficiente al crear pedido
   - **When** sistema ejecuta consulta de disponibilidad y detecta insuficiencia
   - **Then** informa al Asesor Comercial: "Stock insuficiente para: (producto). Disponible: (X)"
   - **And** no genera pedido

4. **Scenario**: Stock insuficiente al momento de comprometer
   - **Given** pedido en "Esperando Ruta"
   - **When** sistema recibe la asignación de ruta y fecha de despacho
   - **And** sistema re-verifica disponibilidad de stock en ese momento
   - **And** el stock ya no cubre la cantidad solicitada
   - **Then** el sistema compromete automáticamente las unidades disponibles para asegurar el despacho de lo existente
   - **And** notifica al Asesor Comercial la cantidad que efectivamente será enviada.
   - **And** el pedido cambia el estado a comprometido con las cantidades ajustadas para no detener la operación de transporte.

5. **Scenario**: Múltiples productos en un pedido, disponibilidad parcial al crear
   - **Given** Producto A tiene stock, Producto B no tiene
   - **Then** sistema informa disponibilidad individual
   - **And** permite confirmar pedido solo con productos disponibles

6. **Scenario**: Pedido no tiene usuario asociado
   - **When** se intenta hacer un pedido sin un cliente asociado
   - **Then** sistema no permite crear el pedido
   - **And** muestra mensaje: "Cliente no asociado"

7. **Scenario**: Asignación FEFO al comprometer lotes
   - **Given** un producto tiene múltiples lotes disponibles
   - **When** sistema compromete inventario por la asignación de ruta y fecha de despacho
   - **Then** selecciona primero lotes con fecha vencimiento más próxima (FEFO)
   - **And** registra movimiento de inventario tipo "Compromiso" vinculado al pedido

---

### Edge Cases
- ¿Stock cambia entre creación del pedido y asignación de ruta y fecha de despacho? → Sistema re-verifica en el momento de comprometer. Si no hay stock, notifica al Asesor Comercial.

- ¿Qué pasa si un lote reservado por FEFO llega a su fecha de vencimiento antes de ser despachado físicamente? → El proceso de Picking disparará una excepción. El sistema debe anular ese compromiso, marcar el lote como "Vencido" y buscar un lote sustituto vigente para no detener el despacho.

---

## Requirements *(mandatory)*

### Functional Requirements
- **FR-052**: El sistema DEBE requerir que el Asesor Comercial ingrese el CC/NIT del Cliente y verificar que este se encuentre registrado y activo.
- **FR-053**: El sistema DEBE ejecutar "Consultar disponibilidad" al confirmar el pedido
- **FR-054**: El sistema DEBE verificar stock disponible por SKU en momento exacto de confirmación del pedido.
- **FR-055**: El sistema DEBE rechazar pedido si stock es insuficiente al momento de creación.
- **FR-056**: El sistema DEBE generar número de pedido único al confirmar.
- **FR-057**: El sistema DEBE dejar pedido en estado "Esperando Ruta" al crearlo, sin comprometer productos.
- **FR-058**: El sistema DEBE recibir la ruta y fecha_recogida del Módulo 2 para activar el compromiso de inventario del pedido.
- **FR-059**: El sistema DEBE re-verificar disponibilidad de stock al momento de comprometer.
- **FR-060**: El sistema DEBE comprometer los productos del pedido al recibir la ruta y fecha_recogida del Módulo 2.
- **FR-061**: El sistema DEBE seleccionar lotes por FEFO al comprometer inventario.
- **FR-062**: El sistema DEBE registrar MovimientoInventario tipo "Compromiso" al reservar productos.
- **FR-063**: El sistema DEBE cambiar estado del pedido a "Comprometido" tras comprometer productos exitosamente.
- **FR-064**: El sistema DEBE informar al Asesor Comercial la disponibilidad real cuando sea insuficiente.

### Key Entities
- **Pedido**: número, cliente_ref, fecha, estado, productos_pedido[].
  Estados: Esperando Ruta | Comprometido | En Picking | Despachado | Entregado.
- **ProductosPedido**: pedido_ref, SKU_ref, cantidad_solicitada, cantidad_confirmada, lotes_comprometidos[].

---

## Success Criteria *(mandatory)*
- **SC-025**: 0% de productos comprometidos al crear el pedido (antes de recibir la ruta y fecha_recogida del Módulo 2).
- **SC-026**: 100% de pedidos con ruta y fecha_recogida del Módulo 2 tienen productos en estado "Comprometido".
- **SC-027**: 0% de pedidos aceptados con stock insuficiente.
- **SC-028**: 100% de lotes asignados siguen criterio FEFO.
- **SC-029**: Tiempo de confirmación del pedido <= 5 seg.
- **SC-030**: Tiempo de compromiso de inventario <= 5 seg.
