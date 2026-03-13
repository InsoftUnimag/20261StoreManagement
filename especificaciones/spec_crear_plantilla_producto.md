
# Feature Specification: Crear Plantilla de Producto
**Created**: 03/03/2026
---
## User Scenarios & Testing *(mandatory)*
 
### User Story 1 - Parametrizar nuevo producto en el catálogo (Priority: P1)
Como **Supervisor de Inventario**, necesito crear la plantilla de un nuevo
producto para que el sistema genere su SKU y quede habilitado para recibir lotes.
 
**Why this priority**: La plantilla de producto constituye el dato maestro esencial. Su implementación es prioritaria para habilitar la trazabilidad operativa entre los módulos de recepciones, lotes y despachos.
 
**Independent Test**: Crear un producto nuevo, verificar que el sistema genera SKU único, y confirmar que aparece en catálogo con stock inicial en cero.
 
**Acceptance Scenarios**:
 
1. **Scenario**: Creación exitosa con datos completos
   - **Given** el Supervisor de Inventario accede al formulario de nueva plantilla
   - **And** el producto no existe en el catálogo
   - **When** el Supervisor de Inventario ingresa marca_cerveza, presentacion, contenido_ml y peso_logístico_kg
   - **And** confirma la creación
   - **Then** el sistema genera automáticamente un SKU único
   - **And** el producto queda en catálogo con stock inicial en cero
   - **And** el producto está disponible para registrar recepciones
 
2. **Scenario**: Producto duplicado (misma marca + presentación)
   - **Given** ya existe producto con esa combinación marca + presentación
   - **When** el Supervisor de Inventario intenta crear el mismo producto
   - **Then** el sistema rechaza la creación
   - **And** muestra el producto existente con su SKU
   - **And** sugiere editar el existente
 
3. **Scenario**: Datos obligatorios incompletos
   - **Given** el Supervisor de Inventario está creando una nueva plantilla
   - **When** intenta confirmar sin campos obligatorios
   - **Then** el sistema muestra los campos faltantes
   - **And** no guarda ningún dato ni genera SKU hasta que estén completos
 
---
### Edge Cases
 - ¿Peso logístico en cero o negativo? → El sistema muestra el error: "El peso logístico debe ser mayor a cero".

 - ¿Mismo nombre, diferente presentación? → Son productos distintos. La unicidad aplica sobre la combinación marca + presentación.

 
---
## Requirements *(mandatory)*
### Functional Requirements
- **FR-001**: El sistema DEBE permitir crear plantilla de producto.
- **FR-002**: El sistema DEBE requerir obligatorios: marca, presentacion, contenido_ml y peso_logístico_kg.
- **FR-003**: El sistema DEBE generar SKU único automáticamente al confirmar.
- **FR-004**: El sistema DEBE validar peso logístico > 0.
- **FR-005**: El sistema DEBE impedir duplicados por combinación marca + presentación.
- **FR-006**: El sistema DEBE inicializar stock en cero al crear el producto.
 
### Key Entities
- **Producto**: marca, presentacion, contenido_ml, peso_logístico_kg, SKU_id (autogenerado).
- **SKU**: código único e inmutable. Identificador del Producto en el sistema.
 
---
## Success Criteria *(mandatory)*
- **SC-001**: 100% de productos creados tienen SKU único generado por sistema.
- **SC-002**: 100% de productos tienen peso_logístico_kg registrado.
- **SC-003**: 0% de productos duplicados (misma marca + presentación).
