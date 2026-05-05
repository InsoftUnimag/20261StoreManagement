
# Feature Specification: Consultar Productos (Catálogo)
**Created**: 03/03/2026
---
## User Scenarios & Testing *(mandatory)*
 
### User Story 1 - Ver catálogo de productos disponibles (Priority: P2)
Como **Asesor Comercial**, necesito consultar el catálogo de productos con su disponibilidad actual para tomar decisiones de compra antes de realizar un pedido.
 
**Why this priority**: Esta funcionalidad garantiza la transparencia en la oferta comercial. Al proporcionar visibilidad en tiempo real sobre los productos disponibles, se facilita una toma de decisiones informada, optimizando el tiempo del Asesor y asegurando que la selección de su cliente coincida siempre con las existencias físicas del inventario.
 
**Independent Test**: Consultar el catálogo como Asesor Comercial, verificar que los productos con stock mayor a cero muestran "Disponible", que los de stock cero muestran "No disponible" y que ningún detalle interno de lotes es visible.
 
**Acceptance Scenarios**:
 
1. **Scenario**: Consulta del catálogo con productos disponibles
   - **Given** existen productos con stock disponible mayor a cero
   - **When** el Asesor comercial accede a la consulta de productos
   - **Then** el sistema muestra el nombre, contenido (ml), la presentación, costo ($ COP) y la disponibilidad del producto
   - **And** los productos con stock mayor a cero muestran "Disponible"
   - **And** los productos con stock igual a cero muestran "No disponible"
   - **And** el el Asesor comercial no ve detalles de lotes (código, fecha vencimiento, fecha expedicion)
 
2. **Scenario**: Búsqueda por nombre o marca
   - **When** el Asesor comercial escribe nombre o marca en buscador
   - **Then** sistema filtra productos que coinciden
   - **And** mantiene indicador de disponibilidad actualizado
 
3. **Scenario**: Catálogo sin productos disponibles
   - **Given** todos los productos tienen stock = 0
   - **When** el Asesor comercial consulta catálogo
   - **Then** sistema muestra todos como "No disponible"
 
---
### Edge Cases
- ¿Stock mostrado incluye Comprometido? No. Solo lotes "Disponible" (reales).
- ¿Consulta en tiempo real? Sí, sin caché desactualizada.
 
---
## Requirements *(mandatory)*
### Functional Requirements
- **FR-048**: El sistema DEBE mostrar catálogo con nombre, presentación, contenido (ml), costo ($ COP) y disponibilidad (Disponible  / No disponible).
- **FR-049**: El sistema DEBE calcular la disponibilidad basándose únicamente en lotes en estado DISPONIBLE, excluyendo COMPROMETIDO.
- **FR-050**: El sistema DEBE permitir buscar por nombre o marca.
- **FR-051**: El sistema DEBE ocultar al Asesor Comercial detalles internos de lotes (código, fecha vencimiento, fecha expedicion).
 
---
## Success Criteria *(mandatory)*
- **SC-023**: El stock mostrado al Asesor Comercial es igual a la suma exacta de los lotes en estado DISPONIBLE
- **SC-024**: 0% de detalles internos de lotes visibles para el Asesor Comercial.
- **SC-025**: Tiempo de carga del catálogo <= 3 segundos para 500 productos.
