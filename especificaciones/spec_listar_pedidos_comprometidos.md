
# Feature Specification: Listar Pedidos listos para despacho
**Created**: 03/03/2026
---
## User Scenarios & Testing *(mandatory)*
 
### User Story 1 - Ver pedidos listos para preparar (Priority: P1)
Como **Operario de Picking**, necesito ver los pedidos que estan comprometidos para organizar mi trabajo de picking diario.
 
**Why this priority**: Esta funcionalidad es el eje de planificación operativa en la bodega. Proporcionar una visibilidad clara de los pedidos pendientes permite al operario priorizar sus tareas, optimizar sus recorridos de picking y asegurar que el despacho cumpla con los tiempos de entrega prometidos, manteniendo un ritmo de trabajo fluido y profesional.
 
**Independent Test**: Con pedidos que estan comprometidos activos, verificar que **Operario de Picking** ve listado completo con detalle de productos y lotes asignados.
 
**Acceptance Scenarios**:
 
1. **Scenario**: Listado de pedidos pendientes
   - **Given** existen pedidos en estado comprometido
   - **When** Operario de Picking accede al listado
   - **Then** sistema muestra todos los pedidos comprometidos
   - **And** por cada pedido: número, cliente, fecha, productos, lotes asignados
   - **And** pedidos ordenados por fecha de creación ASC (FIFO)
 
2. **Scenario**: Detalle de un pedido comprometidos
   - **When** Operario de Picking selecciona un pedido que esta comprometido
   - **Then** sistema muestra líneas: SKU, nombre, cantidad, lotes asignados y dirección de entrega del cliente
 
3. **Scenario**: Sin pedidos pendientes
   - **When** Operario de Picking accede y no hay pedidos comprometidos
   - **Then** sistema muestra: "No hay pedidos pendientes de preparación"
 
---
### Edge Cases
- ¿Ve pedidos de días anteriores no procesados? Sí, si siguen comprometidos.

- ¿Puede filtrar? Sí, por cliente, fecha.
 
---
## Requirements *(mandatory)*
### Functional Requirements
- **FR-063**: El sistema DEBE mostrar todos los pedidos que estan en estado comprometidos.
- **FR-064**: El sistema DEBE mostrar por pedido: número, cliente, fecha, productos y lotes.
- **FR-065**: El sistema DEBE ordenar pedidos por fecha de creación ASC por defecto.
- **FR-066**: El sistema DEBE permitir filtrar por cliente, fecha.
 
---
## Success Criteria *(mandatory)*
- **SC-030**: Listado muestra 100% de pedidos en estdo comprometidos sin omisiones.
- **SC-031**: Tiempo de carga <= 3 segundos para hasta 200 pedidos activos.
