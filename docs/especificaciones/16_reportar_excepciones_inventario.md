
# Feature Specification: Reportar Excepciones de Inventario
**Created**: 03/03/2026
---
## User Scenarios & Testing *(mandatory)*
 
### User Story 1 -  Supervisor reporta anomalía detectada en bodega (Priority: P1)

Como **Supervisor de Inventario**, necesito reportar excepciones sobre lotes existentes en bodega (averías por daño físico o vencimiento detectado durante inspección), para descontar ese stock del inventario disponible y evitar despachar producto no apto.
 
**Why this priority**: Este proceso garantiza la concordancia entre el inventario lógico y el físico. Su correcta ejecución previene la asignación de pedidos sobre productos no aptos, asegurando la efectividad del despacho en el Módulo 2 y minimizando los rechazos en la liquidación logística
 
**Independent Test**: Ejecutar el reporte de avería sobre una fracción de un lote en estado "Disponible", verificar que el stock se descuente en la cantidad reportada y que se genere un registro de MovimientoInventario negativo asociado a la excepción.
 
**Acceptance Scenarios**:
 
1. **Scenario**: Avería sobre una fracción de un lote
   - **Given** existe un lote con estado "Disponible" y con stock mayor a cero.
   - **When** Supervisor de inventario registra: lote, cantidad afectada, tipo "Avería", descripción
   - **Then** stock del lote se descuenta en cantidad afectada
   - **And** se registra MovimientoInventario negativo tipo "Avería"
 
2. **Scenario**: Avería total del lote
   - **Given** existe un lote con cantidad mayor a cero y la cantidad afectada es igual al total del lote
   - **When** el Supervisor de inventario reporta avería sobre la totalidad del lote
   - **Then** la cantidad del lote llega a cero, considerándose "Agotado"
   - **And** el stock global del SKU se actualiza y el lote queda inhabilitado para cualquier asignación futura
 
3. **Scenario**: Lote vencido detectado manualmente
   - **Given** el Supervisor de inventario detecta un lote con fecha de vencimiento superada y aún con cantidad mayor a cero
   - **When** registra una excepción de tipo "VENCIMIENTO" sobre ese lote
   - **Then** la cantidad del lote se fuerza a cero (Agotado)
   - **And** el stock global del SKU se descuenta por la cantidad completa restante del lote
   - **And** se registra un MovimientoInventario negativo de tipo "BAJA_VENCIMIENTO"
   - **And** el lote queda bloqueado para cualquier asignación futura al no tener unidades


4. **Scenario**: Detección automática de lotes vencidos
   - **Given** el proceso automático diario detecta lotes con fecha_vencimiento menor o igual a la fecha actual y cantidad > 0
   - **Then** el sistema extrae su cantidad a cero mediante excepción "Vencimiento", sacándolo de circulación automáticamente
   - **And** genera una alerta consolidada para el Supervisor de Inventario
   - **And** registra los MovimientosInventario de baja correspondientes
 

### User Story 2 -  Operario de Recepción reporta discrepancia con el manifiesto (Priority: P1)

Como **Operario de Recepción**, necesito reportar una excepción cuando la cantidad física recibida no coincide con el manifiesto de fábrica, para que el inventario registrado refleje la realidad y el Supervisor de Inventario pueda gestionar la diferencia.
 
**Why this priority**: La precisión en el ingreso es el pilar de la integridad del inventario. Reportar las discrepancias en esta etapa asegura que la planeación de despachos en el Módulo 2 trabaje sobre existencias reales, evitando reasignaciones.

**Independent Test**: Registrar una recepción donde la cantidad física difiere del manifiesto, activar el reporte de excepción, verificar que el lote se crea con la cantidad real y que la excepción queda vinculada a la Recepción con notificación al Supervisor de Inventario.
 
**Acceptance Scenarios**:
 
1. **Scenario**: Discrepancia entre cantidad física y manifiesto durante recepción
   - **Given** el Operario está registrando una recepción
   - **And** la cantidad física recibida difiere de la indicada en el manifiesto
   - **When** el Operario selecciona "Reportar excepción"
   - **And** completa el reporte con tipo "Diferencia de Inventario" cantidad real recibida y cantidad esperada en manifiesto
   - **Then** el lote se crea con la cantidad física real
   - **And** la excepción queda registrada y vinculada a la Recepción
   - **And** el Supervisor de Inventario recibe notificación
 
2. **Scenario**: Mercancía recibida en estado de daño físico visible
   - **Given** el Operario de Recepción detecta unidades dañadas al momento de recibir el lote
   - **When** el Operario registra el reporte con tipo "Avería", indicando la cantidad dañada y una descripción del daño
   - **Then** el lote se crea con la cantidad total recibida
   - **And** la cantidad averiada se descuenta inmediatamente del stock disponible
   - **And** se registra un MovimientoInventario negativo tipo "Avería"
   - **And** el Supervisor de Inventario recibe notificación
 

### User Story 3 - Operario de picking o despacho reporta avería o vencimiento (Priority: P1)

Como **Operario de Picking o Despacho**, necesito reportar una excepción cuando al ejecutar el picking encuentro que un producto está físicamente dañado o ha vencido, para que el inventario del sistema se corrija y el pedido afectado sea gestionado correctamente.

**Why this priority**: Este reporte es el último filtro de calidad antes de que el producto salga. Garantizar que solo el inventario apto llegue al cliente, optimiza el flujo del Módulo 2 al evitar despachos fallidos.

**Independent Test**: Tomar un pedido en listos para despacho, simular que el Operario detecta unidades averiadas o vencidas al hacer el picking, activar el reporte de excepción, verificar que el stock del lote se ajusta, se registra un MovimientoInventario negativo tipo "BAJA_AVERIA" y el Supervisor de Inventario recibe la alerta.

**Acceptance Scenarios**:

1. **Scenario**: Unidades averiadas detectadas durante el picking
 - **Given** el Operario de picking o despacho está ejecutando el picking de un pedido que esta listo para despacho.
 - **And**  detecta unidades físicamente dañadas en un lote.
 - **When** el Operario reporta la excepción con tipo "Avería", lote afectado, cantidad dañada y descripción del daño
 - **Then** el stock del lote se descuenta en la cantidad averiada
 - **And** se registra un MovimientoInventario negativo tipo "BAJA_AVERIA"
 - **And** el sistema busca automáticamente existencias en otros lotes y le indica al operario de qué lote específico tomar las unidades faltantes para completar el pedido.
 - **And** si la avería es total, la cantidad del lote llega a cero y queda inhabilitado para nuevos pedidos.
 - **And** el Supervisor de Inventario recibe notificación
 - **And** el Operario continúa el proceso de picking con las unidades reasignadas y las no afectadas.


2. **Scenario**: Lote vencido detectado durante el picking
 - **Given** el Operario de picking o despacho está ejecutando el picking de un pedido que esta listo para despacho.
 - **And** detecta que un lote comprometido tiene la fecha de vencimiento superada
 - **When** el Operario reporta la excepción con tipo "Vencimiento" sobre ese lote
 - **Then** el sistema fuerza la cantidad del lote completo a cero y lo bloquea para cualquier transacción futura.
 -**and** el sistema retira automáticamente este lote de todos los pedidos comprometidos que lo tenían asignado en el sistema.
 - **And** el stock del SKU se descuenta por la cantidad del lote
 - **And** se registra un MovimientoInventario negativo por la totalidad del stock vencido tipo "BAJA_VENCIMIENTO"
 - **And** el Supervisor de Inventario recibe notificación

---
### Edge Cases
- ¿La cantidad reportada como excepción puede superar el stock disponible del lote? No. El sistema valida: cantidad_excepción <= stock_actual_lote.

- ¿Una excepción puede revertirse? No directamente. Requiere una nueva recepción del producto repuesto o una corrección autorizada con trazabilidad completa.
 
---
## Requirements *(mandatory)*
### Functional Requirements
- **FR-025**: El sistema DEBE permitir reportar una excepción indicando: tipo (Avería / Vencimiento / Diferencia de Inventario), cantidad afectada y descripción.

- **FR-026**: El sistema DEBE validar que cantidad_excepción <= stock_disponible del lote al momento del reporte.

- **FR-027**: El sistema DEBE descontar el stock del SKU de forma inmediata al confirmar la excepción.

- **FR-028**: El sistema DEBE registrar un MovimientoInventario negativo por cada excepción confirmada.

- **FR-029**: El sistema DEBE garantizar que la cantidad del lote pase a 0 (Agotado) cuando la excepción abarca la totalidad de sus unidades.

- **FR-030**: El sistema DEBE garantizar que la cantidad pase a 0 (Agotado) al reportar o detectar vencimiento total.

- **FR-031**: El sistema DEBE ignorar lotes cuya cantidad sea 0 para nuevas asignaciones FEFO.

- **FR-032**: El sistema DEBE ejecutar un proceso automático diario de detección y baja de lotes vencidos.

- **FR-033**: El sistema DEBE notificar al Supervisor de Inventario ante cada excepción registrada.

- **FR-034**: El sistema DEBE permitir reportar excepciones por Operarios de Recepción, Picking, Despacho y por Supervisor de Inventario.

 
### Key Entities
- **ExcepciónInventario**: tipo (Avería | Vencimiento | Diferencia de Inventario), lote_ref, cantidad_afectada, descripción, operario, fecha, origen_ref (Recepción | Picking | Inspección).
 
---
## Success Criteria *(mandatory)*
- **SC-013**: El stock del lote refleja el descuento de forma inmediata tras confirmar la excepción.
- **SC-014**: 100% de lotes vencidos detectados y llevados a cantidad cero por proceso diario.
- **SC-015**: 0% de lotes sin cantidad asignables a pedidos.
- **SC-016**: El 100% de las excepciones registradas tienen un MovimientoInventario negativo asociado y están vinculadas a su origen (Recepción, Picking o Inspección).
 
