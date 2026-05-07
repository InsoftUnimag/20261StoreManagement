
# Feature Specification: Consultar Inventario
**Created**: 03/03/2026
---
## User Scenarios & Testing *(mandatory)*
 
### User Story 1 - Consultar stock por producto con FEFO (Priority: P2)
Como **Supervisor de Inventario**, necesito consultar el inventario disponible por producto y lote ordenado por FEFO para tomar decisiones de reabastecimiento y gestion de vencimientos.
 
**Why this priority**:Esta visibilidad permite una gestión proactiva de las existencias. Al priorizar los lotes con vencimiento más próximo, se asegura la frescura del inventario entregado y se optimiza el espacio en bodega.

**Independent Test**: Consultar SKU con múltiples lotes, verificar que el total es igual a la suma de lotes Disponibles, ordenados fecha de vencimiento ASC y que los lotes dentro del umbral crítico estan destacados visualmente.
 
**Acceptance Scenarios**:
 
1. **Scenario**: Consulta de SKU con múltiples lotes activos
   - **Given** existen múltiples lotes activos para un SKU
   - **When** Supervisor de Inventario consulta inventario del producto
   - **Then** el sistema muestra el stock total, calculado como la suma de los lotes en estado Disponible
   - **And** lista lotes ordenados por fecha vencimiento ASC (FEFO)
   - **And** muestra por lote: código, fecha vencimiento, cantidad, estado
   - **And** destaca visualmente los lotes con fecha de vencimiento dentro del umbral crítico
 
2. **Scenario**: SKU sin stock disponible
   - **Given** SKU tiene todos sus lotes en Vencido, Avería o Despachado
   - **When** Supervisor de Inventario consulta el inventario del producto
   - **Then** el sistema muestra stock total como cero
   - **And** permite ver historial de MovimientosInventario del SKU
 
3. **Scenario**: Consulta con filtros
   - **When** Supervisor de Inventario aplica filtros por estado o rango de fechas
   - **Then** el sistema filtra y mantiene ordenamiento FEFO
 
---
### Edge Cases
- ¿Muestra stock Comprometido separado? Sí, diferenciado del Disponible.
- ¿Es en tiempo real? Sí, refleja último MovimientoInventario.
 
---
## Requirements *(mandatory)*
### Functional Requirements
- **FR-035**: El sistema DEBE calcular el stock total del SKU como la suma de los lotes en estado DISPONIBLE.
- **FR-036**: El sistema DEBE ordenar lotes por fecha_vencimiento ASC (FEFO).
- **FR-037**: El sistema DEBE resaltar visualmente los lotes con fecha de vencimiento dentro del umbral crítico.
- **FR-038**: El sistema DEBE diferenciar stock "Disponible" vs "Comprometido".
- **FR-039**: El sistema DEBE mostrar el stock en tiempo real basándose en el último MovimientoInventario.
- **FR-040**: El sistema DEBERIA permitir filtrar por estado de lote y rango de fechas.
- **FR-041**: El sistema DEBERIA mostrar el historial de MovimientosInventario por auditoría.
 
---
## Success Criteria *(mandatory)*
- **SC-017**: El stock total del SKU debe ser igual a la suma exacta de los lotes en estado Disponible.
- **SC-018**: El sistema debe garantizar que el primer lote de la lista sea siempre el de vencimiento más próximo, sin excepciones.
- **SC-019**: El 100% de los lotes con fecha de vencimiento dentro del umbral crítico están destacados visualmente.
