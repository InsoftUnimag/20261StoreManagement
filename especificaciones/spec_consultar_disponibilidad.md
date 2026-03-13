# Feature Specification: Consultar Disponibilidad
**Created**: 13/03/2026
---
## User Scenarios & Testing *(mandatory)*
 
### User Story 1 -  Sistema verifica disponibilidad para los productos de un pedido (Priority: P1)

Como **Asesor Comercial**, deseo que el sistema verifique que hay stock disponible para un pedido, es de vital importancia conocer la disponibilidad que tenemos de cada producto y lote, esto con el fin de asignarle producto a cada pedido.
 
**Why this priority**: Un pedido necesita obligatoriamente stock disponible para poder despacharse, el proposito de este paso es que en todo momento podamos saber si el pedido esta completo o hace falta producto al pedido para poder despacharlo sin reportar problemas.
 
**Independent Test**: Buscar en stock todos los lotes disponibles del producto solicitado, realizar la verificación hasta que se le asigne un producto al pedido..
 
**Acceptance Scenarios**:
 
1. **Scenario**: producto con disponibilidad positiva
   - **Given** hay un pedido que requiere un prodcuto
   - **When** se comprueba en el sistema que existan lotes de ese producto
   - **Then** el sistema permite realizar el pedido
 
2. **Scenario**: producto sin disponibilidad
   - **Given** no existen lotes para un producto
   - **When** el pedido requiere el prodcto que no tiene stock
   - **Then** el sistema arroja una "alerta de que no hay stock disponible del producto

2. **Scenario**: la cantidad de producto que requiere el pedido es mayor al stock
   - **Given** existen lotes de un producto en el sistema
   - **When** el pedido requiere ua cantidad A y el lote disponible tiene una cantidad A
   - **And**  cantidad A (stock) < cantidad B (cantidad del pedido)
   - **Then** el sistema arroja una alerta de: "falta stock para el prodcuto en el momento"
---
### Edge Cases
- ¿podría fallar la consulta al tomar como disponible algun lote vencido? absolutamente NO, el sistema de disponibilidad solo buscará en los lotes que están con producto disponible.
---
## Requirements *(mandatory)*
### Functional Requirements

- **FR-067**: El sistema DEBE permitir buscar e informar si hay ese prodcuto en stock pa que se pueda realizar el pedido.

- **FR-068**: El sistema Debe hacer el proceso automaticamente luego de creado un pedido.


---
## Success Criteria *(mandatory)*
- **SC-025**: El sistema debe realizar la consulta 100% de las veces que crea un pedido nuevo.
