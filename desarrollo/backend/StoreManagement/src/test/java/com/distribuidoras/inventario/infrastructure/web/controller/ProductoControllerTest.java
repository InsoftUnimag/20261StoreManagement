package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.*;
import com.distribuidoras.inventario.domain.exception.ProductoDuplicadoException;
import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.model.BitacoraProducto;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para ProductoController.
 * Usa @WebMvcTest para probar la capa web con Spring MVC Test.
 */
@WebMvcTest(ProductoController.class)
@Import(SecurityConfig.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrearProductoUseCase crearProductoUseCase;

    @MockitoBean
    private ModificarProductoUseCase modificarProductoUseCase;

    @MockitoBean
    private EliminarProductoUseCase eliminarProductoUseCase;

    @MockitoBean
    private ConsultarCatalogoUseCase consultarCatalogoUseCase;

    @MockitoBean
    private ConsultarBitacoraUseCase consultarBitacoraUseCase;

    private final UUID skuId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private Producto crearProducto() {
        return new Producto(skuId, "Pilsen", "Six-pack", 330,
                new BigDecimal("2.5"), LocalDateTime.of(2026, 4, 3, 19, 30));
    }

    // --- POST /api/v1/productos ---

    @Test
    @DisplayName("POST /api/v1/productos con datos válidos → 201 Created")
    void crearProducto_exitoso() throws Exception {
        when(crearProductoUseCase.ejecutar(eq("Pilsen"), eq("Six-pack"), eq(330), any(BigDecimal.class)))
                .thenReturn(crearProducto());

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "marca": "Pilsen",
                                    "presentacion": "Six-pack",
                                    "contenidoMl": 330,
                                    "pesoLogisticoKg": 2.5
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.skuId").value(skuId.toString()))
                .andExpect(jsonPath("$.marca").value("Pilsen"))
                .andExpect(jsonPath("$.presentacion").value("Six-pack"))
                .andExpect(jsonPath("$.stockDisponible").value(0))
                .andExpect(jsonPath("$.disponibilidad").value("No disponible"));
    }

    @Test
    @DisplayName("POST /api/v1/productos con duplicado → 409 Conflict")
    void crearProducto_duplicado() throws Exception {
        when(crearProductoUseCase.ejecutar(any(), any(), any(), any()))
                .thenThrow(new ProductoDuplicadoException("Pilsen", "Six-pack"));

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "marca": "Pilsen",
                                    "presentacion": "Six-pack",
                                    "contenidoMl": 330,
                                    "pesoLogisticoKg": 2.5
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/productos con campos inválidos → 400 Bad Request")
    void crearProducto_camposInvalidos() throws Exception {
        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "marca": "",
                                    "contenidoMl": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos").exists());
    }

    // --- PUT /api/v1/productos/{skuId} ---

    @Test
    @DisplayName("PUT /api/v1/productos/{skuId} exitoso → 200 OK con bitácora")
    void modificarProducto_exitoso() throws Exception {
        Producto actualizado = crearProducto();
        actualizado.setPesoLogisticoKg(new BigDecimal("2.8"));

        when(modificarProductoUseCase.ejecutar(eq(skuId), any(), any(), any(), any(), any()))
                .thenReturn(new ModificarProductoUseCase.ResultadoModificacion(
                        actualizado,
                        "El peso logístico fue modificado. Los cálculos de capacidad de flota para rutas no despachadas podrían variar.",
                        List.of()
                ));

        mockMvc.perform(put("/api/v1/productos/{skuId}", skuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "pesoLogisticoKg": 2.8,
                                    "descripcion": "Actualización de peso por cambio de empaque"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skuId").value(skuId.toString()))
                .andExpect(jsonPath("$.alerta").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/productos/{skuId} no existe → 404 Not Found")
    void modificarProducto_noExiste() throws Exception {
        UUID noExiste = UUID.randomUUID();
        when(modificarProductoUseCase.ejecutar(eq(noExiste), any(), any(), any(), any(), any()))
                .thenThrow(new ProductoNotFoundException(noExiste));

        mockMvc.perform(put("/api/v1/productos/{skuId}", noExiste)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "marca": "Aguila"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /api/v1/productos/{skuId} ---

    @Test
    @DisplayName("DELETE /api/v1/productos/{skuId} exitoso → 204 No Content")
    void eliminarProducto_exitoso() throws Exception {
        mockMvc.perform(delete("/api/v1/productos/{skuId}", skuId))
                .andExpect(status().isNoContent());
    }

    // --- GET /api/v1/productos ---

    @Test
    @DisplayName("GET /api/v1/productos → 200 OK con lista")
    void consultarCatalogo_exitoso() throws Exception {
        Producto producto = crearProducto();
        when(consultarCatalogoUseCase.ejecutar(null))
                .thenReturn(List.of(
                        new ConsultarCatalogoUseCase.ProductoConDisponibilidad(producto, 150, "Disponible")
                ));

        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skuId").value(skuId.toString()))
                .andExpect(jsonPath("$[0].stockDisponible").value(150))
                .andExpect(jsonPath("$[0].disponibilidad").value("Disponible"));
    }

    @Test
    @DisplayName("GET /api/v1/productos?busqueda=Pilsen → 200 OK filtrado")
    void consultarCatalogo_conFiltro() throws Exception {
        Producto producto = crearProducto();
        when(consultarCatalogoUseCase.ejecutar("Pilsen"))
                .thenReturn(List.of(
                        new ConsultarCatalogoUseCase.ProductoConDisponibilidad(producto, 0, "No disponible")
                ));

        mockMvc.perform(get("/api/v1/productos").param("busqueda", "Pilsen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].marca").value("Pilsen"));
    }

    // --- GET /api/v1/productos/{skuId}/bitacora ---

    @Test
    @DisplayName("GET /api/v1/productos/{skuId}/bitacora → 200 OK con historial")
    void consultarBitacora_exitoso() throws Exception {
        BitacoraProducto entrada = new BitacoraProducto(
                1L, skuId, "peso_logistico_kg", "2.5", "2.8",
                "Cambio de empaque", LocalDateTime.of(2026, 4, 3, 19, 30), "supervisor01");

        when(consultarBitacoraUseCase.ejecutar(skuId)).thenReturn(List.of(entrada));

        mockMvc.perform(get("/api/v1/productos/{skuId}/bitacora", skuId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].campo").value("peso_logistico_kg"))
                .andExpect(jsonPath("$[0].valorAnterior").value("2.5"))
                .andExpect(jsonPath("$[0].valorNuevo").value("2.8"));
    }
}
