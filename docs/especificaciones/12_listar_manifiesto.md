# Feature Specification: Listar Manifiesto
**Created**: 13/03/2026
---
## User Scenarios & Testing *(mandatory)*
 
### User Story 1 -  Operario de recepción solicita ver los manifiestos de entrega (Priority: P1)

Como **Operario de Recepción**, es de vital importancia conocer los manifiestos precargados en el sistema de los nuevos lotes que van a entrar a la bodega con le objetivo de comparar si la cantidad estimada de entrega conincide con la real al momento de hacer el registro de ingreso.
 
**Why this priority**: Obtener esta información brinda seguridad y respaldo al momento de recibir un abastecimiento, de tal manera que se hace posible saber cuando se debe hacer una excepción por falta de lotes.
 
**Independent Test**: Buscar los manifiestos cargados en el sistema, listarlos al Operario de Recepción, permitir que el Rperario de Recepción vea datos del manifiesto (origen, cantidad de producto o lotes, etc).
 
**Acceptance Scenarios**:
 
1. **Scenario**: lista de manifiesto correcta
   - **Given** hay manifiestos cargados en el sistema
   - **When** Operario de Recepción busca los menifiestos del día
   - **Then** el sistema encuentra los manifiestos que siguen vigente a la fecha y los lista
   - **And** Operario de Recepción puede ver el contenido detallado de los manifiestos
 
2. **Scenario**: no se muestra la lista de manifiesto por inexistencia
   - **Given** no existe un manifiesto cargado en el sistema
   - **When** el Operario de Recepción busca los manifiestos del día
   - **Then** el sistema arroja una alerta de que no se encontro un manifiesto

3. **Scenario**: se listan manifiestos viejos o vencidos
   - **Given** existen manifiestos antiguos o vencidos en el sistema
   - **When** el Operario de Recepción busca los manifiestos antiguos filtrandolos por fecha
   - **Then** el sistema lista todos los manifiestos que ha guardado y cumplan con el requisito de la fecha
---
### Edge Cases
- existencia de un manifiesto corrupto o con datos incorrectos en el sistema, el sistema no detectó que el manifiesto cargado estaba mal, confunde al operario por falta de información al realizar el registro.
---
## Requirements *(mandatory)*
### Functional Requirements
- **FR-025**: El sistema DEBE permitir buscar y listar cada manifiesto disponible con su información.

- **FR-091**: El sistema DEBE poder filtrar por fecha los manifiestos.

- **FR-092**: El sistema DEBE dejar ver la información detallada de cada manifiesto con su origen y el contenido de los lotes.

 
### Key Entities
- id_manifiesto, origen, ,lotes
---
## Success Criteria *(mandatory)*
- **SC-018**: El sistema debe listar correctamente los manifiestos.
- **SC-019**: El sistema debe permitir filtrar manifiestos por fecha.
- **SC-020**: El sistema debe alertar cuando no se encuentre ningun manifiesto.
