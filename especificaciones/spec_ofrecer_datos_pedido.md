# Feature Specification: ofrecer Datos del Pedido (logística de finanzas)

*Created:** 12-03-2026  
**Conmunicación:** asincrona usando una cola para alamcenar pedidos.

## User Scenarios & Testing *(mandatory)*
 
### User Story 1 - logística de finanzas desea saber datos del pedido (Priority: P1)
Como **Sistema financiero**, requiero saber detalles de un pedido para usar los datos en la liquidación final, por tal motivo es primoldial solicitar los datos de un pedido cada vez que se cree uno nuevo.
 
**Why this priority**: La solicitud por parte del la logística financiera es cricual su fincionamiento, saber los detalles del pedido se vuelve un dato clave en el flujo de entrega al cliente.
 
**Independent Test**: Enviar datos del pedido cada vez que se cree uno nuevo (Id_pedido, id_cliente, precio_total, fecha_despacho, dirección_entrega).

 
**Acceptance Scenarios**:
 
1. **Scenario**: envio de datos exitoso
   - **Given** se crea recientemente un pedido
   - **When** el Asesor Comercial crea un pedido para un cliente
   - **Then** luego de que el pedido se crea, se envian los datos al área de finanzas
   - **And** área de finanzas los recibe exitosamente
   - **And** confirman que esta todo bien
 
2. **Scenario**: envio de datos con inconveniente
   - **Given** se crea recientemente un pedido
   - **When** el Asesor Comercial crea un pedido para un cliente
   - **Then** luego de que el pedido se crea, se envian los datos al área de finanzas
   - **And** existen datos erroneos o faltan datos del pedido en la logistica de finanzas
   - **And** no confirman la recepción exitosa
   - **And** se revisa el pedido con sus datos y se reintenta el envio de los datos. 
 

---
## Requirements *(mandatory)*
### Functional Requirements
- **FR-048**: El sistema DEBE enviar los datos solicitados a logística de finanzas luego de crear el pedido.
- **FR-049**: El sistema espera la verificación del buen recibido
- **FR-051**: El sistema puede volver a enviar los datos si ocurre algú problema y no llega notificación de recibido


---
## Success Criteria *(mandatory)*
- **SC-023**: debe haber 0% de pedidos no enviados a finanzas luego de haber sido creado.

