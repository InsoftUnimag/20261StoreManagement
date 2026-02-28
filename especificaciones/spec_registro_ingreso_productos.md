# Feature Specification: Registrar ingreso de nuevo producto

**Created**: 27/02/2026  

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Recepción de Mercancía desde Planta (Priority: P1)

Como **operario de recepción**, necesito registrar la mercancía que llega desde la planta de producción, validando contra el manifiesto de fábrica, para actualizar el inventario disponible y poder atender pedidos.

**Why this priority**: El registro de mercancía es fundamental para gestionar el inventario.

**Independent Test**: Se puede probar de forma independiente recibiendo un lote de SKUs desde planta, validando contra manifiesto, y verificando que el stock "Disponible" se actualice correctamente. Entrega valor inmediato: inventario registrado en sistema.

**Acceptance Scenarios**:

1. **Scenario**: Recepción exitosa de mercancía coincidente con manifiesto
   - **Given** existe un manifiesto de fábrica  
   - **And** el SKU está catalogado en el sistema  
   - **When** el operario registra la recepción del pedido  
   - **Then** el sistema valida la coincidencia con el manifiesto  
   - **And** el estado del producto es "Disponible"  
   - **And** el stock "Disponible" incrementa  

2. **Scenario**: Discrepancia entre mercancía física y manifiesto
   - **Given** existe un manifiesto de fábrica  
   - **When** el operario registra la recepción incompleta del pedido  
   - **Then** el operario de recepción reporta una alerta de "Diferencia de Inventario"  
   - **And** el stock se actualiza  

3. **Scenario**: Recepción de producto con fecha de vencimiento próxima
   - **Given** un lote de producto con vencimiento próximo  
   - **When** el operario registra la recepción  
   - **Then** el sistema genera una alerta de "Producto crítico por vencimiento"  
   - **And** marca el lote con prioridad alta de salida  
   - **And** el stock se registra como "Disponible" pero con flag de urgencia  

---

### User Story 2 - Crear SKU durante la recepción (Priority: P2)

Como **Operario de Recepción**, quiero poder crear un nuevo SKU si el producto recibido no existe en el sistema, para no bloquear el proceso de recepción.

**Why this priority**: Permite flexibilidad operativa cuando llegan productos nuevos no registrados previamente.

**Independent Test**: Puede probarse registrando un producto inexistente y verificando que el sistema permita crear el SKU y luego registrar el lote correctamente.

**Acceptance Scenarios**:

1. **Scenario**: Creación de nuevo SKU durante recepción
   - **Given** el SKU no existe en el sistema  
   - **When** el operario ingresa los datos básicos del nuevo producto  
   - **Then** el sistema crea el SKU  
   - **And** permite continuar con el registro del lote  

---

### Edge Cases

- ¿Qué ocurre si el operario intenta registrar el mismo lote para el mismo SKU?
- ¿Qué ocurre si la cantidad ingresada es cero o negativa?
- ¿Qué ocurre si los datos obligatorios del SKU están incompletos?
- ¿Qué ocurre si se interrumpe la conexión durante el registro?

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST permitir registrar recepción de mercancía.
- **FR-002**: El sistema MUST requerir cantidad, lote y fecha de vencimiento.
- **FR-003**: El sistema MUST validar que la fecha de vencimiento sea posterior a la fecha actual.
- **FR-004**: El sistema MUST incrementar el stock disponible tras confirmar la recepción.
- **FR-005**: El sistema MUST permitir crear un SKU si no existe.
- **FR-006**: El sistema MUST generar alerta ante discrepancias de cantidad.
- **FR-007**: El sistema MUST impedir duplicidad de lote para el mismo SKU.

---

### Key Entities

- **SKU**: Producto identificado por código único, nombre y presentación.
- **Lote**: Identificador asociado a un SKU con fecha de vencimiento y cantidad.
- **Recepción**: Evento que agrupa uno o más productos ingresados.
- **MovimientoInventario**: Registro del incremento de stock derivado de la recepción.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de productos recibidos deben registrar lote y fecha de vencimiento.
- **SC-002**: El sistema debe impedir el registro de productos vencidos.
- **SC-003**: El inventario registrado debe coincidir exactamente con el conteo físico.
- **SC-004**: El tiempo promedio de registro de una recepción no debe superar los 3 minutos.