# Plan de Implementación: Gestión de SKU - Frontend

**Date**: 2026-04-03  
**Specs**: 01_crear_plantilla_producto.md · 02_modificar_plantilla_producto.md · 03_consultar_productos.md  
**Module**: Módulo 1 - Inventario  
**Layer**: Frontend  
**Priority**: P1 (Bloqueante para todo el módulo)

---

## Summary

Interfaces de usuario para CRUD de productos (SKU). Incluye formularios para crear/modificar productos, catálogo con búsqueda/filtrado, y vista de detalle con bitácora de cambios. Consume endpoints REST del backend de gestión SKU.

---

## Dependencies

**Blocked by**:
- Plan Maestro Frontend (Phase 1: Setup de proyecto React + Vite)
- Plan Gestión SKU Backend (endpoints REST deben estar disponibles)

**Blocks**:
- Todas las demás features de frontend (requieren productos existentes para pruebas)

**External Dependencies**:
- Backend API: `/api/v1/productos/*`

---

## Pages & Components

### 1. CatalogoProductosPage
**Purpose**: Página principal del catálogo con lista de productos

**Features**:
- Tabla con columnas: Marca, Presentación, Contenido ML, Peso Logístico, Acciones
- Búsqueda por marca o presentación (input con debounce 300ms)
- Filtros: Solo activos, Todos
- Paginación: 20 productos por página
- Botón "Crear Producto" → navigate to CrearProductoPage
- Acciones por fila: Ver Detalle, Editar, Eliminar (con confirmación)

**State Management**:
- `productos`: array de ProductoDTO
- `loading`: boolean
- `error`: string | null
- `searchTerm`: string
- `page`: number
- `totalPages`: number

**API Calls**:
- `GET /api/v1/productos?search={term}&page={page}&size=20`
- `DELETE /api/v1/productos/{id}` (con confirmación modal)

### 2. CrearProductoPage
**Purpose**: Formulario para crear nuevo producto

**Form Fields**:
- Marca: text input (required, max 100 chars)
- Presentación: text input (required, max 50 chars)
- Contenido ML: number input (required, > 0)
- Peso Logístico KG: number input (required, > 0, step 0.01)

**Validations** (client-side):
- Marca y Presentación no vacíos
- Contenido ML entero positivo
- Peso Logístico decimal positivo

**API Call**:
- `POST /api/v1/productos`
- Success: Navigate to catálogo con toast "Producto creado exitosamente"
- Error 409 (duplicado): Mostrar error "Ya existe un producto con esta marca y presentación"
- Error 400: Mostrar errores de validación del backend

**Components**:
- ProductoForm (reusable para crear y editar)
- FormField, Button, Toast

### 3. EditarProductoPage
**Purpose**: Formulario para modificar producto existente

**Features**:
- Cargar datos del producto al montar (GET /api/v1/productos/{id})
- Formulario pre-llenado (excepto SKU ID que es inmutable y solo se muestra)
- Validaciones iguales a CrearProductoPage
- Deshabilitar edición de SKU ID (mostrar como read-only)

**API Calls**:
- `GET /api/v1/productos/{id}` (cargar datos)
- `PUT /api/v1/productos/{id}` (guardar cambios)
- Success: Navigate to detalle con toast "Producto actualizado"
- Error 404: Redirect a catálogo con error "Producto no encontrado"
- Error 409 (duplicado): "Ya existe otro producto con esa marca y presentación"

### 4. DetalleProductoPage
**Purpose**: Vista de detalle completo del producto con bitácora

**Sections**:
- **Información del Producto**:
  - SKU ID (String, con botón copiar)
  - Marca, Presentación, Contenido ML, Peso Logístico
  - Fecha de creación
  - Botones: Editar, Eliminar, Volver
- **Bitácora de Cambios** (tabla):
  - Columnas: Fecha, Usuario, Operación (Creación/Modificación), Cambios (formato diff)
  - Paginación: 10 registros por página
  - Formato de cambios: "Peso: 2.5 kg → 2.8 kg"

**API Calls**:
- `GET /api/v1/productos/{id}`
- `GET /api/v1/productos/{id}/bitacora?page={page}&size=10`
- `DELETE /api/v1/productos/{id}` (con confirmación)

### 5. ProductoForm (Component)
**Purpose**: Componente reutilizable para formulario de producto

**Props**:
- `producto`: ProductoDTO | null (null para crear, objeto para editar)
- `onSubmit`: (data: ProductoFormData) => void
- `onCancel`: () => void
- `loading`: boolean

**Validation Logic**:
- React Hook Form + Yup schema
- Validaciones sincrónicas (required, min, max, pattern)

**Layout**:
- 2 columnas en desktop, 1 columna en mobile
- Botones: Guardar (disabled si form invalid o loading), Cancelar

---

## Services & API Integration

### ProductoService
**Path**: `src/services/ProductoService.js`

**Methods**:
```javascript
// GET /api/v1/productos?search={}&page={}&size={}
async getProductos(searchTerm = '', page = 0, size = 20) {
  const response = await apiClient.get('/api/v1/productos', {
    params: { search: searchTerm, page, size }
  });
  return response.data;
}

// GET /api/v1/productos/{id}
async getProductoById(id) {
  const response = await apiClient.get(`/api/v1/productos/${id}`);
  return response.data;
}

// POST /api/v1/productos
async crearProducto(data) {
  const response = await apiClient.post('/api/v1/productos', data);
  return response.data;
}

// PUT /api/v1/productos/{id}
async actualizarProducto(id, data) {
  const response = await apiClient.put(`/api/v1/productos/${id}`, data);
  return response.data;
}

// DELETE /api/v1/productos/{id}
async eliminarProducto(id) {
  await apiClient.delete(`/api/v1/productos/${id}`);
}

// GET /api/v1/productos/{id}/bitacora
async getBitacora(id, page = 0, size = 10) {
  const response = await apiClient.get(`/api/v1/productos/${id}/bitacora`, {
    params: { page, size }
  });
  return response.data;
}
```

---

## State Management (React Context + Hooks)

### useProductos (Custom Hook)
**Path**: `src/hooks/useProductos.js`

**Purpose**: Encapsular lógica de búsqueda y paginación

```javascript
function useProductos() {
  const [productos, setProductos] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Debounced search
  const debouncedSearch = useDebounce(searchTerm, 300);

  useEffect(() => {
    fetchProductos();
  }, [debouncedSearch, page]);

  const fetchProductos = async () => {
    setLoading(true);
    try {
      const data = await ProductoService.getProductos(debouncedSearch, page);
      setProductos(data.productos);
      setTotalPages(data.pagination.total_pages);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const eliminarProducto = async (id) => {
    try {
      await ProductoService.eliminarProducto(id);
      fetchProductos(); // Reload list
      toast.success('Producto eliminado exitosamente');
    } catch (err) {
      if (err.response?.status === 409) {
        toast.error('No se puede eliminar: el producto tiene lotes activos');
      } else {
        toast.error('Error al eliminar producto');
      }
    }
  };

  return {
    productos,
    loading,
    error,
    searchTerm,
    setSearchTerm,
    page,
    setPage,
    totalPages,
    fetchProductos,
    eliminarProducto
  };
}
```

---

## UI Components

### ProductoTable (Component)
**Purpose**: Tabla reutilizable de productos

**Props**:
- `productos`: ProductoDTO[]
- `loading`: boolean
- `onEdit`: (producto) => void
- `onDelete`: (producto) => void
- `onViewDetail`: (producto) => void

**Features**:
- Loading skeleton (shimmer effect)
- Empty state: "No se encontraron productos"
- Responsive: scroll horizontal en mobile

### SearchBar (Component)
**Purpose**: Input de búsqueda con debounce

**Props**:
- `value`: string
- `onChange`: (value: string) => void
- `placeholder`: string
- `debounceMs`: number (default 300)

### ConfirmDialog (Component)
**Purpose**: Modal de confirmación reutilizable

**Props**:
- `open`: boolean
- `title`: string
- `message`: string
- `onConfirm`: () => void
- `onCancel`: () => void
- `confirmText`: string (default "Confirmar")
- `cancelText`: string (default "Cancelar")

---

## Implementation Tasks

### Phase 1: Setup & Services

**T001: Crear ProductoService**
- Path: `src/services/ProductoService.js`
- Implementar todos los métodos (6 total)
- Usar axios configurado con base URL y error interceptor

**T002: Configurar axios client**
- Path: `src/config/apiClient.js`
- Base URL: `http://localhost:8080/api/v1`
- Interceptor de respuesta: mapear errores 400, 404, 409, 500 a mensajes amigables

**T003: Crear tipos TypeScript (opcional)**
- Path: `src/types/Producto.ts`
- Interfaces: ProductoDTO, ProductoFormData, BitacoraDTO

### Phase 2: Custom Hooks

**T004: Implementar useProductos hook**
- Encapsular lógica de búsqueda, paginación, eliminación
- Usar useDebounce para search

**T005: Implementar useProductoForm hook**
- Encapsular lógica de crear/editar
- Integrar React Hook Form + Yup

### Phase 3: UI Components (Reusables)

**T006: Crear SearchBar component**
- Input con icono de búsqueda
- Debounce integrado

**T007: Crear ProductoTable component**
- Tabla responsive con acciones
- Loading skeleton

**T008: Crear ProductoForm component**
- Formulario con validaciones
- Modo crear/editar

**T009: Crear ConfirmDialog component**
- Modal de confirmación reutilizable
- Usar biblioteca de UI (MUI, Chakra, o Headless UI)

**T010: Crear Pagination component**
- Botones Anterior/Siguiente
- Info: "Página X de Y"

### Phase 4: Pages

**T011: Implementar CatalogoProductosPage**
- Layout: SearchBar + ProductoTable + Pagination
- Integrar useProductos hook
- Navegación a crear/editar/detalle

**T012: Implementar CrearProductoPage**
- Usar ProductoForm
- Llamar ProductoService.crearProducto
- Manejar errores 409, 400

**T013: Implementar EditarProductoPage**
- Cargar producto en useEffect
- Usar ProductoForm (pre-llenado)
- Llamar ProductoService.actualizarProducto

**T014: Implementar DetalleProductoPage**
- Sección de información (card)
- Tabla de bitácora con paginación
- Botón eliminar con ConfirmDialog

### Phase 5: Routing

**T015: Configurar rutas en React Router**
- `/productos` → CatalogoProductosPage
- `/productos/crear` → CrearProductoPage
- `/productos/:id` → DetalleProductoPage
- `/productos/:id/editar` → EditarProductoPage

### Phase 6: Styling

**T016: Aplicar estilos con Tailwind CSS**
- Responsive design (mobile-first)
- Loading states (shimmer skeleton)
- Empty states (ilustración + mensaje)

**T017: Configurar toast notifications**
- Biblioteca: react-hot-toast o react-toastify
- Success: verde, Error: rojo, Warning: amarillo

### Phase 7: Testing

**T018: Unit tests - Services**
- Test ProductoService con mock de axios (msw)
- Test errores 404, 409, 500

**T019: Unit tests - Hooks**
- Test useProductos con React Testing Library
- Test debounce de búsqueda

**T020: Integration tests - Pages**
- Test CatalogoProductosPage: búsqueda y paginación funcionan
- Test CrearProductoPage: form submit exitoso
- Test validaciones: campos required, tipos incorrectos

---

## Tests

### Unit Tests
- Services: 6 tests
- Hooks: 4 tests
- Components: 8 tests

### Integration Tests
- Pages: 6 tests
- End-to-end (opcional): 2 tests (crear producto → verificar en catálogo)

---

## Acceptance Criteria

**FR-001**: Formulario de creación permite ingresar marca, presentación, contenido_ml, peso_logistico_kg.

**FR-003**: SKU ID es generado automáticamente y es inmutable (no editable).

**FR-005**: Sistema valida UNIQUE(marca, presentacion) y muestra error si duplicado.

**FR-006**: Formulario de modificación permite cambiar todos los campos excepto sku_id.

**FR-008**: Catálogo permite buscar por marca o presentación.

**FR-009**: Operarios pueden ver lista completa de productos con paginación.

**FR-010**: Sistema permite eliminar producto.

**FR-011**: Si producto tiene lotes activos, sistema impide eliminación y muestra mensaje de error.

**FR-013**: Bitácora registra quién, cuándo y qué cambió en el producto.

---

## Notes & Best Practices

1. **Debounce en búsqueda**: 300ms es un buen balance entre responsiveness y reducción de requests al backend.

2. **Validación client + server**: Validar en frontend para UX, pero SIEMPRE validar en backend (fuente de verdad).

3. **Confirmación de eliminación**: Siempre mostrar ConfirmDialog antes de DELETE. Mensaje: "¿Está seguro que desea eliminar el producto {marca} {presentacion}? Esta acción no se puede deshacer."

4. **Loading states**: Usar skeleton loaders (shimmer effect) en lugar de spinners para mejor UX.

5. **Empty states**: Si catálogo vacío, mostrar mensaje amigable + botón "Crear primer producto".

6. **Error 409 (duplicado)**: Mensaje específico: "Ya existe un producto con la marca '{marca}' y presentación '{presentacion}'. Por favor, verifique los datos."

7. **Copiar SKU ID**: En DetalleProductoPage, botón para copiar ID al clipboard (útil para debugging).

8. **Responsive design**: Tabla debe hacer scroll horizontal en mobile. Considerar vista de cards en mobile en lugar de tabla.

9. **Paginación server-side**: NO cargar todos los productos en frontend. Siempre usar paginación del backend.

10. **Bitácora legible**: Formatear cambios como diff: "Peso: 2.5 kg → 2.8 kg". Si no hay cambios en un campo, no mostrarlo.
