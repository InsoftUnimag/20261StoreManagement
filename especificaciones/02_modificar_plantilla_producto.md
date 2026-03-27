
# Feature Specification: Modificar Plantilla de Producto
**Created**: 03/03/2026
---
## User Scenarios & Testing *(mandatory)*
 
### User Story 1 - Actualizar atributos de producto existente (Priority: P2)
Como **Supervisor de Inventario**, necesito modificar los atributos de un
producto ya registrado sin perder el historial del SKU ni los lotes asociados.
 
**Why this priority**: La Plantilla de Producto es el fundamento operativo del sistema. Los atributos logísticos (como el peso_logístico_kg) son variables críticas para la optimización de carga en el Módulo 2. Esta funcionalidad permite la adaptabilidad del catálogo sin perder la trazabilidad de inventario.
 
**Independent Test**: Modificar peso_logístico_kg de producto con lotes activos, verificar SKU sin cambios, lotes no afectados, cambio en bitácora.
 
**Acceptance Scenarios**:
 
1. **Scenario**: Modificación exitosa de atributo
   - **Given** existe un producto con su SKU asignado
   - **When** el Supervisor de Inventario modifica el peso_logístico_kg y confirma
   - **Then** el sistema actualiza el atributo
   - **And** el SKU permanece sin cambios
   - **And** los lotes existentes no son afectados
   - **And** el cambio queda en bitácora (campo, valor ant., valor nuevo, descripcion, fecha)
 
2. **Scenario**: Modificación que generaría duplicado
   - **Given** existen Producto A y Producto B en el catálogo
   - **When** Supervisor de Inventario cambia marca del B a la marca del A
   - **Then** sistema detecta conflicto y rechaza la modificación
 
3. **Scenario**: Intento de eliminar producto con lotes activos
   - **Given** el producto tiene lotes Disponible o Comprometido
   - **When** Supervisor de Inventario intenta eliminar el producto
   - **Then** El sistema deniega la acción
   - **And** Notifica que el registro posee dependencias operativas activas
 
---
### Edge Cases
- **Inmutabilidad de Identidad**: El SKU ID no es editable bajo ninguna circunstancia tras su creación.

- **Carga Crítica**: Si se modifica el peso_logístico_kg, el sistema debe emitir una alerta indicando que los cálculos de capacidad de flota en el Módulo 2 para rutas no despachadas podrían variar.
---

## Requirements *(mandatory)*
### Functional Requirements

- **FR-007**: El sistema DEBE permitir modificar: marca, presentacion, contenido_ml y peso_logístico_kg.
- **FR-008**: El sistema DEBE mantener el SKU invariable ante cualquier modificación.
- **FR-009**: El sistema DEBE registrar bitácora: campo, valor_anterior, valor_nuevo, descripcion, fecha_modificacion.
- **FR-010**: El sistema DEBE validar que la modificación no genere combinación marca+presentación duplicada.
- **FR-011**: El sistema DEBE impedir eliminación de producto con lotes activos o historial.
 
### Key Entities
- **BitácoraProducto**: SKU_id, campo, valor_anterior, valor_nuevo, descripcion, fecha_modificacion.
 
---
## Success Criteria *(mandatory)*
- **SC-004**: Integridad referencial mantenida: el SKU ID no cambia tras la edición.
- **SC-005**: 100% de modificaciones en bitácora (SKU_id, campo, valor_anterior, valor_nuevo, descripcion, fecha_modificacion).
- **SC-006**: 0% de productos con lotes activos pueden ser eliminados.