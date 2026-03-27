
# Feature Specification: Registrar Ingreso de Productos
**Created**: 03/03/2026
---
## User Scenarios & Testing *(mandatory)*
 
### User Story 1 - Recepción de mercancía desde planta (Priority: P1)
Como **Operario de Recepción**, necesito registrar la mercancía que llega
desde planta validando contra el manifiesto, para actualizar el inventario
disponible y habilitar la atención de pedidos.
 
**Why this priority**: Este proceso constituye el punto de entrada del flujo operativo. Su ejecución es prioritaria para garantizar la visibilidad de existencias y permitir pedidos.
 
**Independent Test**: Recibir lote de SKU existente, validar contra manifiesto, verificar que stock "Disponible" se actualiza y MovimientoInventario se registra.
 
**Acceptance Scenarios**:
 
1. **Scenario**: Recepción exitosa coincidente con manifiesto
   - **Given** existe manifiesto activo, SKU en catálogo, lote no existe para ese SKU
   - **When** Operario de recepción registra: código lote, cantidad, fecha vencimiento
   - **Then** sistema valida coincidencia con manifiesto
   - **And** crea Lote con estado "Disponible"
   - **And** stock del SKU incrementa en cantidad recibida
   - **And** registra MovimientoInventario tipo "Entrada"
   - **And** confirma con número de recepción generado
 
2. **Scenario**: Múltiples lotes del mismo SKU en una recepción
 - **Given** el manifiesto incluye dos o más lotes distintos del mismo SKU
 - **When**  el Operario de recepción registra cada lote individualmente
 - **Then** el sistema crea un Lote independiente por cada código
 - **And**  el stock total del SKU equivale a la suma de todos los lotes recibidos
 - **And** los lotes quedan ordenados por fecha de vencimiento ascendente (FEFO)
 - **And** registra un MovimientoInventario de tipo "Entrada" por cada lote


3. **Scenario**: Lote con vencimiento dentro del umbral crítico
   - **Given** el lote entrante tiene una fecha de vencimiento dentro del umbral crítico parametrizado (N días)
   - **When** el Operario de recepción confirma el ingreso
   - **Then** el sistema registra el lote con estado "Disponible" y asigna el flag de urgencia FEFO
   - **And** genera la alerta: "Producto crítico por vencimiento próximo"
   - **And** registra un MovimientoInventario de tipo "Entrada"
   - **And** el Supervisor de Inventario recibe la notificación

 
4. **Scenario**: Discrepancia con manifiesto
   - **Given** existe manifiesto activo para el SKU
   - **When** Operario de recepción registra cantidad diferente a la del manifiesto
   - **Then** sistema detecta discrepancia y extiende a "Reportar excepción"
   - **And** Operario de recepción completa reporte tipo "Diferencia de Inventario"
   - **And** lote se crea con la cantidad física real
   - **And** Supervisor de Inventario recibe notificación
   - **And** registra MovimientoInventario tipo "Entrada"
 
5. **Scenario**: Fecha de vencimiento ya vencida
   - **Given** el Operario de recepción está registrando una recepción
   - **When** ingresa una fecha de vencimiento menor o igual a la fecha actual
   - **Then** el sistema rechaza el ingreso con el mensaje: "La fecha de vencimiento debe ser posterior a la fecha actual"
   - **And** no crea lote ni MovimientoInventario
   - **and** El operario de recepción reporta una excepción
   - **and** Supervisor de Inventario recibe notificación
 
6. **Scenario**: Código de lote duplicado para el mismo SKU
   - **Given** ya existe ese código de lote para el SKU
   - **When** Operario de recepción intenta registrarlo
   - **Then** sistema rechaza: "El lote {código} ya existe para {SKU}"
   - **And** stock existente no es modificado
 
7. **Scenario**: Cantidad cero o negativa
   - **When** Operario ingresa cantidad menor o igual a cero
   - **Then** sistema rechaza: "La cantidad debe ser mayor a cero"
 
8. **Scenario**: Interrupción de conexión
   - **When** se pierde conexión antes de confirmar
   - **Then** sistema no persiste ningún dato parcial (operación atómica)
   - **And** inventario permanece sin cambios
 
---
### Edge Cases
- SKU no existe en catálogo → redirigir a Supervisor de Inventario para crear plantilla.
- Manifiesto no existe o cerrado → requerir manifiesto activo.
- Registro parcial → No permitido. Operación atómica.
 
---
## Requirements *(mandatory)*
### Functional Requirements
- **FR-012**: El sistema DEBE registrar recepción con SKU, código_lote, cantidad, fecha_expedicion, costo_COP, estado_lote, flag_urgencia_fefo y fecha_vencimiento.
- **FR-013**: El sistema DEBE requerir los campos (SKU, código_lote, cantidad, fecha_expedicion, costo_COP, estado_lote, flag_urgencia_fefo y fecha_vencimiento) como obligatorios.
- **FR-014**: El sistema DEBE validar fecha vencimiento sea posterior a la fecha actual.
- **FR-015**: El sistema DEBE crear Lote con estado "Disponible" al confirmar la recepción.
- **FR-016**: El sistema DEBE incrementar el stock del SKU en la cantidad recibida (include: Actualizar stock disponible).
- **FR-017**: El sistema DEBE registrar MovimientoInventario tipo "Entrada" por cada lote confirmado.
- **FR-018**: El sistema DEBE impedir el registro de un código de lote duplicado para el mismo SKU.
- **FR-019**: El sistema DEBE rechazar cantidades menores o iguales a cero.
- **FR-020**: El sistema DEBE detectar discrepancia y extender a "Reportar excepción" (extend).
- **FR-021**: El sistema DEBE garantizar que la operación de recepción sea atómica: sin datos parciales en caso de fallo.
- **FR-022**: El sistema DEBE generar alerta "Producto crítico" si lote vence en N días.
- **FR-023**: El sistema DEBE asignar el flag de urgencia FEFO a los lotes que superen el umbral crítico de vencimiento.
- **FR-024**: El sistema DEBE asignar un número de recepción único por cada evento de recepción confirmado.
 

### Key Entities
- **Recepción**: número, fecha, operario, manifiesto_ref.
- **Lote**: código_lote, SKU_ref, fecha_vencimiento, cantidad, estado_lote, flag_urgencia_fefo, fecha_expedicion, costo_COP. 
- **MovimientoInventario**: tipo, cantidad, lote_ref, recepción_ref, fecha.
- **Manifiesto**: documento de planta con SKUs, lotes y cantidades esperadas.
 
---
## Success Criteria *(mandatory)*
- **SC-007**: 100% de lotes tienen código y fecha vencimiento antes de confirmar.
- **SC-008**: 100% de fechas vencidas son rechazadas.
- **SC-009**: Stock disponible es igual al conteo físico tras cada recepción exitosa.
- **SC-010**: 100% de discrepancias generan excepción visible para Supervisor de Inventario.
- **SC-011**: 0% de lotes duplicados (mismo código + mismo SKU).