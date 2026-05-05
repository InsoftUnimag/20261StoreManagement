
# Feature Specification: Consultar Detalle de Pedido
**Created**: 11/03/2026

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Módulo 2 consulta detalle para planificar ruta (Priority: P1)
Como **Módulo 2 - Logística**, necesito consultar el detalle completo de un pedido para construir la ruta de entrega con la información de cliente, dirección de entrega y peso logístico real.

**Why this priority**: El Módulo 2 depende de estos datos para calcular la capacidad de carga de la flota y secuenciar las paradas. Sin este dato maestro del Módulo 1, la planificación de rutas no puede ejecutarse.

### User Story 2 — Módulo 3 consulta detalle para liquidación (Priority: P1)
Como **Módulo 3 - Financiero**, necesito consultar el detalle de un pedido (cantidades reales despachadas, cliente, SKUs) para ejecutar la conciliación financiera y calcular el cobro correcto.

**Why this priority**: La liquidación del Módulo 3 opera sobre las cantidades reales, no las solicitadas. Esta consulta es la fuente de verdad para la facturación y el cálculo de la remuneración logística.

**Independent Test**: Despachar un pedido parcial, consultar su detalle desde el Módulo 2 y verificar que retorna cantidades reales; consultar desde el Módulo 3 y verificar que retorna el mismo dato con información de cliente y SKUs.

**Acceptance Scenarios**:

1. **Scenario**: Consulta exitosa por Módulo 2
   - **Given** existe un pedido en estado "Despachado" o posterior
   - **When** Módulo 2 consulta el detalle por número de pedido
   - **Then** retorna: número de pedido, cliente (NIT), producto del pedido con SKU, cantidad despachada y peso logístico total del pedido, dirección de entrega.

2. **Scenario**: Consulta exitosa por Módulo 3
   - **Given** existe un pedido en estado "Despachado" o posterior
   - **When** Módulo 3 consulta el detalle por número de pedido
   - **Then** retorna: número de pedido, cliente (NIT), producto del pedido con SKU, cantidad solicitada, cantidad real despachada e indicador Completo/Parcial, dirección de entrega.

3. **Scenario**: Pedido no encontrado
   - **Given** el número de pedido no existe en el sistema
   - **When** cualquier módulo consulta
   - **Then** retorna error: "Pedido {número} no encontrado"

4. **Scenario**: Pedido aún no despachado
   - **Given** el pedido existe pero está en estado previo a "Despachado"
   - **When** Módulo 2 o Módulo 3 consultan
   - **Then** retorna los datos disponibles con indicador de estado actual
   - **And** indica que el despacho aún no ha sido confirmado

---

### Edge Cases
- ¿Se expone información interna de lotes (códigos, fechas vencimiento)? No. Solo se expone lo necesario para cada módulo consumidor.
- ¿Qué pasa si el pedido fue parcial? Se expone cantidad_solicitada y cantidad_despachada por producto para que cada módulo calcule la diferencia.

---

## Requirements *(mandatory)*

### Functional Requirements
- **FR-093**: MUST exponer detalle de pedido consultable por número de pedido.
- **FR-094**: MUST retornar para Módulo 2: cliente, dirección entrega, SKUs, cantidades despachadas y peso logístico total.
- **FR-095**: MUST retornar para Módulo 3: cliente, NIT, SKUs, cantidad solicitada, cantidad despachada e indicador Completo/Parcial.
- **FR-096**: MUST retornar error claro si el pedido no existe.
- **FR-097**: MUST incluir estado actual del pedido en la respuesta.
- **FR-098**: MUST NO exponer detalles internos de lotes (código lote, fecha vencimiento) a módulos externos.

### Key Entities
- Ninguna nueva. Expone datos de: **Pedido**, **LíneaPedido**, **Producto** (peso logístico) y referencia externa de **Cliente** (vía módulo usuarios).

---

## Success Criteria *(mandatory)*
- **SC-044**: 100% de consultas por número de pedido válido retornan detalle completo.
- **SC-045**: 0% de detalles internos de lotes expuestos a módulos externos.
- **SC-046**: Tiempo de respuesta de consulta <= 2 segundos.
