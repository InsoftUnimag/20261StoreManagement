# Feature Specification: Consultar Disponibilidad
**Created**: 13/03/2026
---
## User Scenarios & Testing *(mandatory)*
 
### User Story 1 -  Sistema verifica disponibilidad para los productos de un pedido (Priority: P1)

Como **Asesor Comercial**, deseo que el sistema verifique que hay stock disponible para un pedido, es de vital importancia conocer la disponibilidad que tenemos de cada producto y lote, esto con el fin de asignarle producto a cada pedido.
 
**Why this priority**: Un pedido necesita obligatoriamente stock disponible para poder despacharse, el proposito de este paso es que en todo momento podamos saber si el pedido esta completo o hace falta producto al pedido para poder despacharlo sin reportar problemas.
 
**Independent Test**: Consultar en la vista de stock global del sistema el total de unidades disponibles del producto solicitado y validar si dicha cantidad en existencia es mayor o igual a lo pedido.
 
**Acceptance Scenarios**:
 
1. **Scenario**: producto con disponibilidad positiva
   - **Given** hay un pedido que requiere un prodcuto
   - **When** se comprueba en el stock global que existan unidades disponibles suficientes
   - **Then** el sistema permite realizar el pedido
 
2. **Scenario**: producto sin disponibilidad
   - **Given** no existen unidades disponibles en el stock global para un producto
   - **When** el pedido requiere el prodcto que no tiene stock
   - **Then** el sistema arroja una alerta de que no hay stock disponible del producto

3. **Scenario**: la cantidad de producto que requiere el pedido es mayor al stock
   - **Given** existen existencias de un producto en el sistema
   - **When** el pedido requiere ua cantidad A y el stock global disponible es menor a B
   - **And**  cantidad global disponible < cantidad A (cantidad del pedido)
   - **Then** el sistema arroja una alerta de: "falta stock para el producto en el momento"
---
### Edge Cases
- ¿podría fallar la consulta al tomar como disponible stock vencido? absolutamente NO, el agrupador de stock global se nutre y resta automáticamente en background de los vencimientos para que la vista comercial siempre sea 100% apta para la venta.
---
## Requirements *(mandatory)*
### Functional Requirements

- **FR-067**: El sistema DEBE permitir buscar e informar si hay ese producto en stock para que se pueda realizar el pedido.

- **FR-068**: El sistema DEBE hacer el proceso automáticamente antes de confirmar la creación de un pedido.


---
## Success Criteria *(mandatory)*
- **SC-025**: El sistema debe realizar la consulta 100% de las veces antes de que se intente confirmar un pedido nuevo.
