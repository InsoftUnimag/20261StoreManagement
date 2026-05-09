# Feature Specification: Asignar Despacho
**Created**: 08/05/2026
---
## User Scenarios & Testing *(mandatory)*

### User Story 1 - Supervisor asigna pedidos con picking finalizado a Operario de Despacho (Priority: P1)
Como **Supervisor de Inventario**, necesito asignar pedidos en estado "Pickup" a un Operario de Despacho específico, para garantizar que cada pedido tenga un responsable único de carga y salida física de bodega, manteniendo la trazabilidad completa del flujo operativo.

**Why this priority**: La asignación centralizada del despacho cierra el ciclo de control del inventario físico. Sin esta asignación, no hay trazabilidad sobre quién es responsable de cargar el pedido al vehículo, generando riesgos de pérdidas, duplicaciones y falta de rendición de cuentas.

**Independent Test**: Con pedidos en estado "Pickup" activos, verificar que el Supervisor de Inventario puede ver el listado completo ordenado por prioridad FEFO y fecha de salida, asignar un pedido a un Operario de Despacho disponible, y verificar que el Operario solo ve los pedidos asignados a él.

**Acceptance Scenarios**:

1. **Scenario**: Listado de pedidos con picking finalizado disponibles para asignación
   - **Given** existen pedidos en estado "Pickup" sin asignar a despacho
   - **When** Supervisor de Inventario accede al módulo de asignación de despacho
   - **Then** sistema muestra todos los pedidos con picking finalizado pendientes de asignación
   - **And** los pedidos se ordenan por fecha de salida ASC y prioridad FEFO de los lotes
   - **And** por cada pedido se muestra: número, cliente, ruta, fecha de salida, operario de picking que lo preparó, cantidad de líneas

2. **Scenario**: Asignación exitosa de pedido a Operario de Despacho
   - **Given** Supervisor de Inventario selecciona un pedido con picking finalizado sin asignar
   - **And** selecciona un Operario de Despacho disponible
   - **When** Supervisor de Inventario confirma la asignación
   - **Then** el pedido seleccionado queda asignado al Operario de Despacho elegido
   - **And** el sistema registra: pedido_ref, operario_despacho_ref, supervisor_ref, fecha_asignacion
   - **And** el pedido aparece en la vista del Operario de Despacho asignado
   - **And** el pedido desaparece de la lista de pedidos sin asignar para despacho

3. **Scenario**: Intento de asignar pedido ya asignado a despacho
   - **Given** un pedido con picking finalizado ya fue asignado a un Operario de Despacho
   - **When** Supervisor de Inventario intenta asignarlo a otro Operario
   - **Then** sistema informa: "El pedido {número} ya está asignado a {operario}. ¿Desea reasignarlo?"
   - **And** si el Supervisor confirma, se reasigna el pedido al nuevo Operario

4. **Scenario**: Intento de asignar pedido en estado incorrecto
   - **Given** un pedido NO está en estado "Pickup"
   - **When** Supervisor de Inventario intenta asignarlo a un Operario de Despacho
   - **Then** sistema rechaza: "El pedido {número} no está listo para despacho. Estado actual: {estado}"

5. **Scenario**: Filtro por ruta y fecha de salida
   - **Given** Supervisor de Inventario accede al módulo de asignación de despacho
   - **When** aplica filtros por ruta y/o fecha de salida
   - **Then** sistema muestra solo los pedidos con picking finalizado que coinciden con los filtros
   - **And** mantiene el ordenamiento por prioridad FEFO dentro de los resultados filtrados

6. **Scenario**: Sin pedidos con picking finalizado para asignar
   - **Given** Supervisor de Inventario accede al módulo de asignación de despacho
   - **When** no existen pedidos en estado "Pickup" sin asignar
   - **Then** sistema muestra: "No hay pedidos con picking finalizado pendientes de asignación"

7. **Scenario**: Visualización de carga por Operario de Despacho
   - **Given** Supervisor de Inventario accede al módulo de asignación de despacho
   - **When** el sistema carga la vista
   - **Then** se muestra un resumen de pedidos asignados por cada Operario de Despacho activo
   - **And** el Supervisor puede evaluar la distribución de carga antes de asignar nuevos pedidos

---
### Edge Cases
- ¿El Operario de Despacho puede auto-asignarse pedidos? No. Solo el Supervisor de Inventario tiene permisos para asignar pedidos a despacho.

- ¿Se puede reasignar un pedido cuyo despacho ya inició? No. Una vez que el Operario inició la confirmación de despacho, el pedido no puede reasignarse.

- ¿Se priorizan pedidos con lotes de vencimiento más próximo? Sí. La vista del Supervisor ordena por prioridad FEFO para asegurar que los productos con fecha de vencimiento más cercana salgan primero.

---
## Requirements *(mandatory)*
### Functional Requirements
- **FR-104**: El sistema DEBE permitir al Supervisor de Inventario visualizar todos los pedidos en estado "Pickup" pendientes de asignación a despacho.
- **FR-105**: El sistema DEBE ordenar los pedidos por fecha de salida ASC y prioridad FEFO de los lotes.
- **FR-106**: El sistema DEBE permitir al Supervisor de Inventario asignar un pedido a un Operario de Despacho específico.
- **FR-107**: El sistema DEBE registrar cada asignación con: pedido_ref, operario_despacho_ref, supervisor_ref, fecha_asignacion.
- **FR-108**: El sistema DEBE impedir la asignación de pedidos que no estén en estado "Pickup".
- **FR-109**: El sistema DEBE permitir reasignar un pedido aún no despachado.
- **FR-110**: El sistema DEBE permitir filtrar pedidos por ruta y fecha de salida.
- **FR-111**: El sistema DEBE mostrar al Supervisor un resumen de carga de pedidos por Operario de Despacho.
- **FR-112**: El sistema DEBE restringir la funcionalidad de asignación de despacho exclusivamente al rol Supervisor de Inventario.

### Key Entities
- **AsignacionDespacho**: pedido_ref, operario_despacho_ref, supervisor_ref, fecha_asignacion, estado (Asignado | Reasignado).

---
## Success Criteria *(mandatory)*
- **SC-050**: 100% de pedidos con picking finalizado son visibles en el módulo de asignación de despacho del Supervisor.
- **SC-051**: 100% de asignaciones de despacho registran pedido_ref, operario_ref, supervisor_ref y fecha.
- **SC-052**: 0% de pedidos en estado diferente a "Pickup" son asignables a un Operario de Despacho.
- **SC-053**: 100% de pedidos asignados aparecen exclusivamente en la vista del Operario de Despacho designado.
- **SC-054**: Ordenamiento FEFO aplicado al 100% de las vistas de asignación de despacho del Supervisor.
- **SC-055**: Tiempo de asignación (1 pedido a 1 operario) <= 3 segundos.
