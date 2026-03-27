
# Feature Specification: Confirmar Picking
**Created**: 03/03/2026
---
## User Scenarios & Testing *(mandatory)*
 
### User Story 1 - Confirmar preparación física del pedido (Priority: P1)
Como **Operario de Picking**, necesito confirmar que recogí físicamente los productos de bodega. Si detecto "Averias", debo reportarlo como excepción.
 
**Why this priority**: Esta validación asegura la integridad entre el inventario lógico y el físico. Al confirmar el picking en tiempo real, el sistema garantiza que solo productos aptos y disponibles salgan.

**Independent Test**: Tomar pedido que estan en estado comprometido, confirmar "En picking" completo, verificar que los productos del pedido pasan a "Picking". Luego probar "Averias" activando Reportar Excepción.
 
**Acceptance Scenarios**:
 
1. **Scenario**: Picking completo sin novedades
   - **Given** Operario de Picking selecciona pedido en estado "comprometidos"
   - **And** todos los productos del pedido están físicamente en bodega
   - **When** Operario de Picking confirma "En Picking" el pedido
   - **Then** Productos del pedido cambian de "Comprometido" a "Picking"
   - **And** se registra MovimientoInventario tipo "Picking" por cada pedido.
 
2. **Scenario**: Intento de picking de pedido ya procesado
   - **Given** pedido de Picking ya tiene estado "En Picking"
   - **When** Operario de Picking intenta confirmar picking
   - **Then** sistema rechaza: "Este pedido ya fue procesado. Estado: {estado}"

 3. **Scenario**: Picking parcial — faltante parcial aceptado
   - **Given** el Operario detecta que la cantidad física en el estante es menor a la solicitada por el sistema
   - **When** el Operario reporta la cantidad faltante
   - **Then** el sistema busca automáticamente si el SKU está disponible en la bodega
   - **And** si encuentra stock, asigna las unidades faltantes  e indica al operario de Picking el lote donde debe buscar la unidades faltantes
   - **And** si no hay más stock disponible, se toman solo las disponibles
   - **And** el sistema genera un registro de excepción tipo "Faltante" para pedido.
   - **And** el pedido avanza a la siguiente etapa con las unidades efectivamente recolectadas
   - **And** notifica al Supervisor sobre el quiebre de stock

---
### Edge Cases
- ¿Dos Operarios de Picking toman el mismo pedido simultáneamente? Concurrencia:
  un lote solo puede ser tomado por un Operario de Picking a la vez.

- ¿Picking reversible? Solo por Supervisor de Inventario, con bitácora completa.
 
---
## Requirements *(mandatory)*
### Functional Requirements
- **FR-067**: El sistema DEBE permitir confirmar picking de pedido "Comprometido".
- **FR-068**: El sistema DEBE cambiar producto de "Comprometido" a "Picking" al confirmar.
- **FR-069**: El sistema DEBE registrar MovimientoInventario tipo "Picking" por cada pedido.
- **FR-070**: El sistema DEBE impedir confirmar picking de pedido ya procesado.
- **FR-071**: El sistema DEBE actualizar estado del pedido a "En Picking" al confirmar exitosamente.
- **FR-072**: El sistema DEBE permitir confirmar picking con solo las unidades recolectadas
- **FR-071**: El sistema DEBE registrar MovimientoInventario tipo "Faltante_Picking" 
 
### Key Entities
- **RegistroPicking**: pedido_ref, lote_ref, cantidad_esperada, cantidad_real, operario, fecha.
 
---
## Success Criteria *(mandatory)*
- **SC-032**: 100% de pedidos que en picking se confirman cambian "Comprometido" → "En Picking".
- **SC-033**: 100% de faltantes en picking generan ExcepciónInventario tipo "Faltante".
- **SC-034**: 0% de pedidos ya procesados aceptan nueva confirmación de picking.
- **SC-035**: Tiempo de confirmación de picking (1 pedido, <= 10 líneas) <= 5 minutos.
