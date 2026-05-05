
# FEAT-006: Consultar Datos de Cliente por CC
**Created**: 11/03/2026
**Nota**: El registro y gestión de usuarios es responsabilidad del módulo externo de usuarios. Este módulo solo consume ese servicio mediante consulta por CC/NIT.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Consultar cliente por número de documento (Priority: P1)
Como **Asesor Comercial**, necesito consultar los datos de un cliente registrado en el módulo de usuarios por su número de cédula/NIT, para vincular sus datos al momento de crear un pedido.

**Why this priority**: Este módulo no gestiona usuarios directamente. La identidad del cliente es responsabilidad del módulo de usuarios externo. Toda operación que requiera datos del cliente por parte del Asesor Comercial depende de esta consulta para garantizar consistencia entre módulos.

**Independent Test**: Consultar un CC/NIT existente, verificar que retorna nombre, estado y datos de contacto.

**Acceptance Scenarios**:

1. **Scenario**: Consulta exitosa de cliente existente
   - **Given** el módulo de usuarios tiene registrado un cliente con ese CC/NIT
   - **When** el sistema consulta por CC/NIT
   - **Then** retorna: nombre, CC/NIT, teléfono, estado (Activo/Inactivo)
   - **And** la información queda disponible para vincular al pedido

2. **Scenario**: Cliente no encontrado
   - **Given** no existe cliente con ese CC/NIT en el módulo de usuarios
   - **When** el sistema consulta
   - **Then** retorna mensaje: "No se encontró cliente con ese documento"
   - **And** no se permite continuar con la operación que requería el dato

3. **Scenario**: Cliente inactivo
   - **Given** el cliente existe pero su estado es "Inactivo"
   - **When** el sistema consulta
   - **Then** retorna los datos con indicador de estado "Inactivo"
   - **And** el sistema impide vincular ese cliente a un nuevo pedido

---

### Edge Cases
- ¿El módulo de usuarios no responde? → El sistema muestra error de conectividad y no permite continuar hasta resolver.
- ¿Se almacena localmente la info del cliente? No. Se consulta en tiempo real al módulo de usuarios para garantizar datos actualizados.

---

## Requirements *(mandatory)*

### Functional Requirements
- **FR-042**: El sistema debe consultar datos de cliente al módulo de usuarios externo por CC/NIT.
- **FR-043**: El sistema debe retornar: nombre, CC/NIT, teléfono y estado del cliente.
- **FR-044**: El sistema debe impedir vincular a pedidos clientes con estado "Inactivo".
- **FR-045**: El sistema debe manejar error de cliente no encontrado con mensaje claro.
- **FR-046**: El sistema debe manejar error de conectividad con el módulo externo.

### Key Entities
- Ninguna propia. Los datos del cliente son propiedad del módulo de usuarios.
- **Referencia externa**: cliente_cc como clave foránea lógica en Pedido.

---

## Success Criteria *(mandatory)*
- **SC-019**: 100% de pedidos tienen cliente válido y activo verificado en módulo externo.
- **SC-020**: 0% de pedidos creados con cliente "Inactivo".
- **SC-021**: Tiempo de consulta al módulo externo <= 2 segundos.
