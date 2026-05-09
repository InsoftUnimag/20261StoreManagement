# Feature Specification: Crear Manifiesto de Carga
**Created**: 08/05/2026
---
## User Scenarios & Testing *(mandatory)*

### User Story 1 - Supervisor registra manifiesto anticipado de mercancía esperada (Priority: P1)
Como **Supervisor de Inventario**, necesito crear un manifiesto de carga con los productos, cantidades y proveedor que se esperan recibir, para que el equipo de recepción disponga de un documento de referencia y control al momento de la llegada física de la mercancía.

**Why this priority**: El manifiesto es el eje de control de ingresos a bodega. Su creación anticipada permite al Operario de Recepción validar que lo recibido físicamente coincide con lo planeado, detectar discrepancias de forma inmediata y activar el flujo de excepciones cuando corresponda. Sin este documento, la recepción opera a ciegas y la integridad del inventario queda comprometida desde el origen.

**Independent Test**: Crear un manifiesto con proveedor registrado, fecha estimada de llegada, múltiples SKUs y cantidades esperadas. Verificar que el sistema genera un número único de manifiesto, lo deja en estado "Pendiente de Recepción" y es visible para el Operario de Recepción en el módulo de listar manifiestos (ver 12_listar_manifiesto.md).

**Acceptance Scenarios**:

1. **Scenario**: Creación exitosa de manifiesto completo
   - **Given** el Supervisor de Inventario tiene la información de la compra o envío del proveedor
   - **And** el proveedor está registrado en el sistema
   - **When** ingresa los datos: proveedor, fecha estimada de llegada, SKUs y cantidades esperadas por cada producto
   - **Then** el sistema genera un número único de manifiesto
   - **And** el manifiesto queda en estado "Pendiente de Recepción"
   - **And** el manifiesto es visible para el Operario de Recepción en el módulo de consulta

2. **Scenario**: Validación de productos obligatorios
   - **Given** el Supervisor de Inventario está creando un manifiesto
   - **When** intenta guardar el manifiesto sin agregar ningún producto (SKU)
   - **Then** el sistema rechaza: "El manifiesto debe contener al menos un producto"
   - **And** no se genera número de manifiesto

3. **Scenario**: Validación de cantidades mayores a cero
   - **Given** el Supervisor de Inventario agrega un producto al manifiesto
   - **When** ingresa una cantidad igual a cero o negativa para un SKU
   - **Then** el sistema rechaza: "La cantidad esperada debe ser mayor a cero para el producto {SKU}"
   - **And** no permite guardar hasta corregir la cantidad

4. **Scenario**: Proveedor no registrado en el sistema
   - **Given** el Supervisor de Inventario intenta crear un manifiesto
   - **When** busca un proveedor que no está registrado en el sistema
   - **Then** el sistema informa: "El proveedor no se encuentra registrado. Debe registrarse antes de crear el manifiesto"
   - **And** no permite continuar con la creación

5. **Scenario**: Creación de manifiesto con múltiples SKUs
   - **Given** el Supervisor de Inventario tiene una orden de compra con varios productos
   - **When** agrega múltiples SKUs con sus cantidades esperadas al manifiesto
   - **Then** el sistema registra cada línea individualmente con su SKU y cantidad
   - **And** calcula y muestra el total de unidades esperadas en el manifiesto
   - **And** genera el manifiesto con todas las líneas en una sola operación

6. **Scenario**: Duplicación de SKU en el mismo manifiesto
   - **Given** el Supervisor de Inventario está agregando productos al manifiesto
   - **When** intenta agregar un SKU que ya existe en el manifiesto
   - **Then** el sistema informa: "El producto {SKU} ya está incluido en este manifiesto. ¿Desea actualizar la cantidad?"
   - **And** si confirma, consolida las cantidades en una sola línea

7. **Scenario**: Cancelación de la creación del manifiesto
   - **Given** el Supervisor de Inventario está en el proceso de creación de un manifiesto
   - **When** decide cancelar antes de guardar
   - **Then** el sistema descarta los datos ingresados sin generar ningún registro
   - **And** redirige al Supervisor al módulo principal

---
### Edge Cases
- ¿Se puede editar un manifiesto después de creado? Solo si su estado es "Pendiente de Recepción" y no ha iniciado el proceso de recepción física. Una vez que el Operario de Recepción inicia el registro de ingreso contra el manifiesto, queda bloqueado para edición.

- ¿Se pueden crear manifiestos con fecha estimada en el pasado? No. El sistema valida que la fecha estimada de llegada sea igual o posterior a la fecha actual.

- ¿Qué pasa si un SKU del manifiesto no existe en el catálogo de productos? El sistema rechaza la línea e informa al Supervisor que debe registrar el producto primero mediante el proceso de Crear Plantilla de Producto (ver 01_crear_plantilla_producto.md).

- ¿Un manifiesto puede asociarse a una orden de compra externa? El sistema permite registrar una referencia externa opcional (número de orden de compra del proveedor) para trazabilidad.

---
## Requirements *(mandatory)*
### Functional Requirements
- **FR-113**: El sistema DEBE permitir al Supervisor de Inventario crear un nuevo manifiesto de carga.
- **FR-114**: El sistema DEBE permitir al Supervisor seleccionar un proveedor registrado en el sistema para asociar al manifiesto.
- **FR-115**: El sistema DEBE permitir la carga de ítems (SKU) con cantidades esperadas por cada producto del manifiesto.
- **FR-116**: El sistema DEBE validar que el manifiesto contenga al menos un producto antes de permitir guardarlo.
- **FR-117**: El sistema DEBE validar que todas las cantidades esperadas sean mayores a cero.
- **FR-118**: El sistema DEBE validar que todos los SKUs ingresados existan en el catálogo de productos.
- **FR-119**: El sistema DEBE generar un número único de manifiesto al momento de guardar exitosamente.
- **FR-120**: El sistema DEBE asignar el estado "Pendiente de Recepción" al manifiesto recién creado.
- **FR-121**: El sistema DEBE hacer visible el manifiesto creado para el Operario de Recepción en el módulo de consulta.
- **FR-122**: El sistema DEBE impedir la creación de manifiestos con fecha estimada de llegada anterior a la fecha actual.
- **FR-123**: El sistema DEBE permitir registrar opcionalmente una referencia externa (número de orden de compra del proveedor).
- **FR-124**: El sistema DEBE restringir la funcionalidad de creación de manifiestos exclusivamente al rol Supervisor de Inventario.

### Key Entities
- **Manifiesto**: id_manifiesto, proveedor_ref, fecha_estimada_llegada, referencia_externa (opcional), estado (Pendiente de Recepción | En Recepción | Recibido), supervisor_ref, fecha_creacion.
- **LineaManifiesto**: manifiesto_ref, SKU_ref, cantidad_esperada, cantidad_recibida (se completa en recepción).

---
## Success Criteria *(mandatory)*
- **SC-056**: 100% de manifiestos creados contienen al menos un producto con cantidad mayor a cero.
- **SC-057**: 100% de manifiestos generados tienen un número único e irrepetible.
- **SC-058**: 100% de manifiestos creados quedan en estado "Pendiente de Recepción" y son visibles para el Operario de Recepción.
- **SC-059**: 0% de manifiestos aceptados con SKUs inexistentes en el catálogo de productos.
- **SC-060**: Integridad de datos: el manifiesto guardado coincide exactamente con lo ingresado por el Supervisor (proveedor, fecha, SKUs y cantidades).
- **SC-061**: Tiempo de creación de manifiesto (hasta 50 líneas) <= 3 segundos.
