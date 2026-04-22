package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.*;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.infrastructure.web.dto.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;

/**
 * Controlador REST para gestión de Productos (SKU).
 * Endpoints definidos en plan_gestion_sku_backend.md
 */
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    private final CrearProductoUseCase crearProductoUseCase;
    private final ModificarProductoUseCase modificarProductoUseCase;
    private final EliminarProductoUseCase eliminarProductoUseCase;
    private final ConsultarCatalogoUseCase consultarCatalogoUseCase;
    private final ConsultarBitacoraUseCase consultarBitacoraUseCase;

    public ProductoController(CrearProductoUseCase crearProductoUseCase,
                               ModificarProductoUseCase modificarProductoUseCase,
                               EliminarProductoUseCase eliminarProductoUseCase,
                               ConsultarCatalogoUseCase consultarCatalogoUseCase,
                               ConsultarBitacoraUseCase consultarBitacoraUseCase) {
        this.crearProductoUseCase = crearProductoUseCase;
        this.modificarProductoUseCase = modificarProductoUseCase;
        this.eliminarProductoUseCase = eliminarProductoUseCase;
        this.consultarCatalogoUseCase = consultarCatalogoUseCase;
        this.consultarBitacoraUseCase = consultarBitacoraUseCase;
    }

    /**
     * POST /api/v1/productos - Crear nuevo producto (SKU)
     * Actor: Supervisor de Inventario
     * Spec: 01_crear_plantilla_producto.md
     */
    @PostMapping
    public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody ProductoRequest request) {
        log.info("POST /api/v1/productos - Crear producto: marca={}, presentación={}",
                request.getMarca(), request.getPresentacion());

        Producto creado = crearProductoUseCase.ejecutar(
                request.getMarca(),
                request.getPresentacion(),
                request.getContenidoMl(),
                request.getPesoLogisticoKg()
        );

        ProductoResponse response = ProductoResponse.fromCreado(creado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/productos/{skuId} - Modificar producto existente
     * Actor: Supervisor de Inventario
     * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
     * Spec: 02_modificar_plantilla_producto.md
     */
    @PutMapping("/{skuId}")
    public ResponseEntity<ProductoResponse> modificarProducto(
            @PathVariable String skuId,
            @Valid @RequestBody ProductoUpdateRequest request) {
        log.info("PUT /api/v1/productos/{} - Modificar producto", skuId);

        ModificarProductoUseCase.ResultadoModificacion resultado = modificarProductoUseCase.ejecutar(
                skuId,
                request.getMarca(),
                request.getPresentacion(),
                request.getContenidoMl(),
                request.getPesoLogisticoKg(),
                request.getDescripcion()
        );

        ProductoResponse response = ProductoResponse.fromDomain(resultado.producto(), 0, resultado.alerta(), null);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/productos/{skuId} - Eliminar producto
     * Actor: Supervisor de Inventario
     * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
     * Spec: 02_modificar_plantilla_producto.md (FR-011)
     */
    @DeleteMapping("/{skuId}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String skuId) {
        log.info("DELETE /api/v1/productos/{} - Eliminar producto", skuId);

        eliminarProductoUseCase.ejecutar(skuId);

        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/productos - Consultar catálogo con disponibilidad (Paginado)
     * Actor: Asesor Comercial
     * Spec: 03_consultar_productos.md
     */
    @GetMapping
    public ResponseEntity<Page<ProductoResponse>> consultarCatalogo(
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "marca,asc") String[] sort) {
            
        log.info("GET /api/v1/productos - Consultar catálogo paginado, filtro='{}', page={}, size={}", busqueda, page, size);

        // Convert the sort parameter into a Spring Data Sort object
        // Format of sort parameter: [ "marca,asc", "presentacion,desc" ] or "marca,asc"
        Sort sortObj = Sort.unsorted();
        if (sort != null && sort.length > 0) {
            if (sort.length == 2 && (sort[1].equalsIgnoreCase("asc") || sort[1].equalsIgnoreCase("desc"))) {
                // simple case: sort=marca,asc
                sortObj = Sort.by(Sort.Direction.fromString(Objects.requireNonNull(sort[1])), Objects.requireNonNull(sort[0]));
            } else {
                // Multiple sorts, but not expected realistically based on specs. Let's just handle simple case.
                sortObj = Sort.by(Sort.Direction.fromString(Objects.requireNonNull(sort[1])), Objects.requireNonNull(sort[0]));
            }
        }
        
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<ConsultarCatalogoUseCase.ProductoConDisponibilidad> resultado =
                consultarCatalogoUseCase.ejecutarConPaginacion(busqueda, pageable);

        Page<ProductoResponse> response = resultado.map(pcd -> ProductoResponse.fromDomain(
                pcd.producto(),
                pcd.stockDisponible(),
                null,
                pcd.costoCop()));

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/productos/{skuId}/bitacora - Consultar historial de cambios
     * Actor: Supervisor de Inventario
     * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
     * Spec: 02_modificar_plantilla_producto.md (FR-009)
     */
    @GetMapping("/{skuId}/bitacora")
    public ResponseEntity<List<BitacoraResponse>> consultarBitacora(@PathVariable String skuId) {
        log.info("GET /api/v1/productos/{}/bitacora - Consultar historial", skuId);

        List<BitacoraResponse> response = consultarBitacoraUseCase.ejecutar(skuId).stream()
                .map(BitacoraResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(response);
    }
}
