# Feature Specification: Asignar Picking
**Created**: 08/05/2026
---
## User Scenarios & Testing *(mandatory)*

### User Story 1 - Supervisor asigna pedidos comprometidos a Operario de Picking (Priority: P1)
Como **Supervisor de Inventario**, necesito asignar pedidos en estado "Comprometido" a un Operario de Picking específico, considerando la ruta y la fecha de salida, para distribuir la carga operativa de manera controlada y garantizar que cada pedido tenga un responsable único de preparación.

**Why this priority**: La asignación centralizada por el Supervisor es el punto de control que garantiza una distribución equitativa del trabajo, evita conflictos de concurrencia entre operarios y asegura que los pedidos con mayor urgencia (por fecha de salida) y productos con vencimiento próximo (FEFO) sean priorizados correctamente.

**Independent Test**: Con pedidos en estado "Comprometido" activos, verificar que el Supervisor de Inventario puede ver el listado completo ordenado por prioridad FEFO y fecha de salida, asignar un pedido a un Operario de Picking disponible, y verificar que el Operario solo ve los pedidos asignados a él.

**Acceptance Scenarios**:

1. **Scenario**: Listado de pedidos comprometidos disponibles para asignación
   - **Given** existen pedidos en estado "Comprometido" sin asignar
   - **When** Supervisor de Inventario accede al módulo de asignación de picking
   - **Then** sistema muestra todos los pedidos comprometidos pendientes de asignación
   - **And** los pedidos se ordenan por fecha de salida ASC y prioridad FEFO de los lotes comprometidos
   - **And** por cada pedido se muestra: número, cliente, ruta, fecha de salida, cantidad de líneas, productos y lotes asignados

2. **Scenario**: Asignación exitosa de pedido a Operario de Picking
   - **Given** Supervisor de Inventario selecciona un pedido comprometido sin asignar
   - **And** selecciona un Operario de Picking disponible
   - **When** Supervisor de Inventario confirma la asignación
   - **Then** el pedido seleccionado queda asignado al Operario de Picking elegido
   - **And** el sistema registra: pedido_ref, operario_picking_ref, supervisor_ref, fecha_asignacion
   - **And** el pedido aparece en la vista del Operario de Picking asignado
   - **And** el pedido desaparece de la lista de pedidos sin asignar

3. **Scenario**: Intento de asignar pedido ya asignado
   - **Given** un pedido comprometido ya fue asignado a un Operario de Picking
   - **When** Supervisor de Inventario intenta asignarlo a otro Operario
   - **Then** sistema informa: "El pedido {número} ya está asignado a {operario}. ¿Desea reasignarlo?"
   - **And** si el Supervisor confirma, se reasigna el pedido al nuevo Operario

4. **Scenario**: Intento de asignar pedido en estado incorrecto
   - **Given** un pedido NO está en estado "Comprometido"
   - **When** Supervisor de Inventario intenta asignarlo a un Operario de Picking
   - **Then** sistema rechaza: "El pedido {número} no está disponible para picking. Estado actual: {estado}"

5. **Scenario**: Filtro por ruta y fecha de salida
   - **Given** Supervisor de Inventario accede al módulo de asignación de picking
   - **When** aplica filtros por ruta y/o fecha de salida
   - **Then** sistema muestra solo los pedidos comprometidos que coinciden con los filtros aplicados
   - **And** mantiene el ordenamiento por prioridad FEFO dentro de los resultados filtrados

6. **Scenario**: Sin pedidos comprometidos para asignar
   - **Given** Supervisor de Inventario accede al módulo de asignación de picking
   - **When** no existen pedidos en estado "Comprometido" sin asignar
   - **Then** sistema muestra: "No hay pedidos comprometidos pendientes de asignación"

7. **Scenario**: Visualización de carga por Operario de Picking
   - **Given** Supervisor de Inventario accede al módulo de asignación de picking
   - **When** el sistema carga la vista
   - **Then** se muestra un resumen de pedidos asignados por cada Operario de Picking activo
   - **And** el Supervisor puede evaluar la distribución de carga antes de asignar nuevos pedidos

---
### Edge Cases
- ¿El Operario de Picking puede auto-asignarse pedidos? No. Solo el Supervisor de Inventario tiene permisos para asignar pedidos.

- ¿Se puede reasignar un pedido que ya está "En Picking"? No. Una vez que el Operario inició la confirmación de picking, el pedido no puede reasignarse. Solo puede revertirse por el Supervisor.

- ¿Se priorizan pedidos con lotes de vencimiento más próximo? Sí. La vista del Supervisor ordena por prioridad FEFO para asegurar que los productos con fecha de vencimiento más cercana sean despachados primero.

---
## Requirements *(mandatory)*
### Functional Requirements
- **FR-095**: El sistema DEBE permitir al Supervisor de Inventario visualizar todos los pedidos en estado "Comprometido" pendientes de asignación.
- **FR-096**: El sistema DEBE ordenar los pedidos por fecha de salida ASC y prioridad FEFO de los lotes comprometidos.
- **FR-097**: El sistema DEBE permitir al Supervisor de Inventario asignar un pedido a un Operario de Picking específico.
- **FR-098**: El sistema DEBE registrar cada asignación con: pedido_ref, operario_picking_ref, supervisor_ref, fecha_asignacion.
- **FR-099**: El sistema DEBE impedir la asignación de pedidos que no estén en estado "Comprometido".
- **FR-100**: El sistema DEBE permitir reasignar un pedido comprometido aún no procesado.
- **FR-101**: El sistema DEBE permitir filtrar pedidos por ruta y fecha de salida.
- **FR-102**: El sistema DEBE mostrar al Supervisor un resumen de carga de pedidos por Operario de Picking.
- **FR-103**: El sistema DEBE restringir la funcionalidad de asignación exclusivamente al rol Supervisor de Inventario.

### Key Entities
- **AsignacionPicking**: pedido_ref, operario_picking_ref, supervisor_ref, fecha_asignacion, estado (Asignado | Reasignado).

---
## Success Criteria *(mandatory)*
- **SC-044**: 100% de pedidos comprometidos son visibles en el módulo de asignación del Supervisor.
- **SC-045**: 100% de asignaciones de picking registran pedido_ref, operario_ref, supervisor_ref y fecha.
- **SC-046**: 0% de pedidos en estado diferente a "Comprometido" son asignables a un Operario de Picking.
- **SC-047**: 100% de pedidos asignados aparecen exclusivamente en la vista del Operario de Picking designado.
- **SC-048**: Ordenamiento FEFO aplicado al 100% de las vistas del Supervisor de Inventario.
- **SC-049**: Tiempo de asignación (1 pedido a 1 operario) <= 3 segundos.
