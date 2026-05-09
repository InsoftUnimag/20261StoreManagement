
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
### User Story 2 - Supervisor asigna pedidos a operarios (Priority: P1)
Como **Supervisor de Inventario**, necesito asignar pedidos comprometidos a operarios de picking y despacho para balancear la carga de trabajo y controlar el flujo operativo.

**Why this priority**: La asignacion temprana evita cuellos de botella y permite que cada operario trabaje sobre pedidos claros y rastreables.

**Independent Test**: Con pedidos en estado comprometido, el Supervisor lista operarios por rol, asigna un pedido y valida que la asignacion quede registrada y retornada en la respuesta.

**Acceptance Scenarios**:

1. **Scenario**: Asignar operario de picking a pedido comprometido
   - **Given** existe un pedido en estado "Comprometido"
   - **And** el Supervisor obtiene la lista de operarios de picking
   - **When** el Supervisor asigna operario de picking al pedido
   - **Then** el sistema guarda la asignacion de picking
   - **And** retorna el id de operario asignado

2. **Scenario**: Asignar operario de despacho despues del picking
   - **Given** existe un pedido en estado "En Picking"
   - **And** el Supervisor obtiene la lista de operarios de despacho
   - **When** el Supervisor asigna operario de despacho al pedido
   - **Then** el sistema guarda la asignacion de despacho
   - **And** retorna el id de operario asignado

3. **Scenario**: Intento de asignar pedido en estado incorrecto
   - **Given** el pedido NO esta en estado "Comprometido"
   - **When** el Supervisor intenta asignar operarios
   - **Then** el sistema rechaza la asignacion por estado invalido

4. **Scenario**: Intento de asignar picking y despacho al mismo tiempo
   - **Given** existe un pedido en estado "Comprometido"
   - **When** el Supervisor intenta asignar operario de picking y despacho en la misma operacion
   - **Then** el sistema rechaza la asignacion simultanea

5. **Scenario**: Consultar operarios por rol
   - **When** el Supervisor solicita operarios de picking
   - **Then** el sistema consulta el modulo de usuarios y retorna operarios con rol OPERARIO_PICKING
   - **And** lo mismo aplica para operarios de despacho (OPERARIO_DESPACHO)
 
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
- **FR-074**: El sistema DEBE permitir al Supervisor de Inventario asignar operario de picking a pedidos en estado "Comprometido".
- **FR-075**: El sistema DEBE consultar el modulo de usuarios para listar operarios por rol (OPERARIO_PICKING, OPERARIO_DESPACHO) mediante endpoints separados.
- **FR-076**: El sistema DEBE retornar en la respuesta los ids de operario asignados al pedido.
- **FR-077**: El sistema DEBE permitir asignar operario de despacho solo cuando el pedido este en estado "En Picking".
- **FR-078**: El sistema NO DEBE permitir asignar operario de picking y despacho en la misma operacion.
 
---
## Success Criteria *(mandatory)*
- **SC-030**: Listado muestra 100% de pedidos en estdo comprometidos sin omisiones.
- **SC-031**: Tiempo de carga <= 3 segundos para hasta 200 pedidos activos.
