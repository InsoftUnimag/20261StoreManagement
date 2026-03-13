# Feature Specification: Solicitar Ruta
**Created**: 12/03/2026
**Conmunicación:** asincrona usando una cola para alamcenar pedidos.


---
## User Scenarios & Testing *(mandatory)*
 
### User Story 1 - solicitar una ruta para el despacho de pedidos (Priority: P1)
Como **Logística de inventario**, se requiere una ruta para cada uno de los pedidos que se crean, para esto pasamos información del pedido a logística de transporte y quedamos a espera de una ruta que me pueden ofrecer para que el pedido llegue a su cliente.
 
**Why this priority**: La ruta de entrega es el canal que tiene el pedido entre la bodega y el cliente, es prioritario tener una ruta para cada pedido, así nos aseguramos que todos los cientes tengas los productos que encargaron.

**Independent Test**: se envian datos clave del pedido (id_pedido, id_cliente, peso_logistico, direccion_entrega), mientras se espera de respuesta una ruta asignada (id_ruta) y una fecha de despacho (fecha_despacho).
 
**Acceptance Scenarios**:
 
1. **Scenario**: asignación de ruta exitosa
   - **Given** se solicita una ruta a logística de transporte para un pedido
   - **When** el Asesor Comercial crea un pedido para un cliente
   - **Then** se envian los datos del pedido a logística de trasnporte
   - **And** procesan los datos y le asignan una ruta con una fecha de despacho.
   - **And** envian los datos de la ruta y fecha de despacho
   - **And** se guardan los datos en la información del pedido
 

 2. **Scenario**: ruta inhabilitada para el pedido
   - **Given** se solicita una ruta a logística de transporte para un pedido
   - **When** el Asesor Comercial crea un pedido para un cliente
   - **Then** se envian los datos del pedido a logística de trasnporte
   - **And** procesan los datos y no encuentran una ruta disponible para el pedido.
   - **And** pedido regresa a la cola para esperar a que se cree una nueva ruta
 

---
### Edge Cases
- ¿Si el dato de dirección es erroneo y nose le puede asignar una ruta? el.
Si logística de trasnporte no encuentra la dirección por dirección erronea, no se puede asignar ruta
se notifica un cambio de dirección.

---
## Requirements *(mandatory)*
### Functional Requirements
- **FR-067**: El sistema DEBE enviar los datos de dirección y peso a logística de trasnporte para que le asignen una ruta.
- **FR-068**: El sistema DEBE guardar la ruta asignada y la fecha de despacho en el pedido.
-
 
### Key Entities
- **RegistroPicking**: id_pedido, id_ruta, fecha_despacho, peso_logistico.
 
---
## Success Criteria *(mandatory)*
- **SC-032**: los pedidos se envian por una cola en donde logística de transporte recibe cada una de las solicitudes.
- **SC-032**: 100% de los pedidos deben tener una dirección de entrega correcta.