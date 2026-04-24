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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        private final String skuId = "SKU-001";

        private Producto crearProducto() {
                return Producto.builder()
                                .skuId(skuId)
                                .marca("PILSEN") // ahora el dominio guarda en mayúsculas
                                .presentacion("SIX-PACK")
                                .contenidoMl(330)
                                .pesoLogisticoKg(new BigDecimal("2.5"))
                                .creadoEl(LocalDateTime.of(2026, 4, 3, 19, 30))
                                .build();
        }

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
                                .andExpect(jsonPath("$.skuId").value(skuId))
                                .andExpect(jsonPath("$.marca").value("PILSEN"))
                                .andExpect(jsonPath("$.presentacion").value("SIX-PACK"))
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

        @Test
        @DisplayName("PUT /api/v1/productos/{skuId} exitoso → 200 OK con bitácora")
        void modificarProducto_exitoso() throws Exception {
                Producto base = crearProducto();
                // Ya no hay setters, creamos copia
                Producto actualizado = Producto.builder()
                                .skuId(base.getSkuId())
                                .marca(base.getMarca())
                                .presentacion(base.getPresentacion())
                                .contenidoMl(base.getContenidoMl())
                                .pesoLogisticoKg(new BigDecimal("2.8"))
                                .creadoEl(base.getCreadoEl())
                                .build();

                when(modificarProductoUseCase.ejecutar(eq(skuId), any(), any(), any(), any(), any()))
                                .thenReturn(new ModificarProductoUseCase.ResultadoModificacion(
                                                actualizado,
                                                "El peso logístico fue modificado. Los cálculos de capacidad de flota para rutas no despachadas podrían variar.",
                                                List.of()));

                mockMvc.perform(put("/api/v1/productos/{skuId}", skuId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "pesoLogisticoKg": 2.8,
                                                    "descripcion": "Actualización de peso por cambio de empaque"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.skuId").value(skuId))
                                .andExpect(jsonPath("$.alerta").exists());
        }

        @Test
        @DisplayName("PUT /api/v1/productos/{skuId} no existe → 404 Not Found")
        void modificarProducto_noExiste() throws Exception {
                String noExiste = "SKU-999";
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

        @Test
        @DisplayName("DELETE /api/v1/productos/{skuId} exitoso → 204 No Content")
        void eliminarProducto_exitoso() throws Exception {
                mockMvc.perform(delete("/api/v1/productos/{skuId}", skuId))
                                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("GET /api/v1/productos → 200 OK con lista")
        void consultarCatalogo_exitoso() throws Exception {
                Producto producto = crearProducto();
                var page = new org.springframework.data.domain.PageImpl<>(
                                List.of(new ConsultarCatalogoUseCase.ProductoConDisponibilidad(producto, 150,
                                                "Disponible", new BigDecimal("1500.00"))));
                when(consultarCatalogoUseCase.ejecutarConPaginacion(eq(null),
                                any(org.springframework.data.domain.Pageable.class)))
                                .thenReturn(page);

                mockMvc.perform(get("/api/v1/productos"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].skuId").value(skuId))
                                .andExpect(jsonPath("$.content[0].stockDisponible").value(150))
                                .andExpect(jsonPath("$.content[0].disponibilidad").value("Disponible"));
        }

        @Test
        @DisplayName("GET /api/v1/productos?busqueda=Pilsen → 200 OK filtrado")
        void consultarCatalogo_conFiltro() throws Exception {
                Producto producto = crearProducto();
                var page = new org.springframework.data.domain.PageImpl<>(
                                List.of(new ConsultarCatalogoUseCase.ProductoConDisponibilidad(producto, 0,
                                                "No disponible", null)));
                when(consultarCatalogoUseCase.ejecutarConPaginacion(eq("Pilsen"),
                                any(org.springframework.data.domain.Pageable.class)))
                                .thenReturn(page);

                mockMvc.perform(get("/api/v1/productos").param("busqueda", "Pilsen"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].marca").value("PILSEN"));
        }

        @Test
        @DisplayName("GET /api/v1/productos/{skuId}/bitacora → 200 OK con historial")
        void consultarBitacora_exitoso() throws Exception {
                BitacoraProducto entrada = BitacoraProducto.builder()
                                .id(1L)
                                .skuIdRef(skuId)
                                .campo("peso_logistico_kg")
                                .valorAnterior("2.5")
                                .valorNuevo("2.8")
                                .descripcion("Cambio de empaque")
                                .fecha(LocalDateTime.of(2026, 4, 3, 19, 30))
                                .usuario("supervisor01")
                                .build();

                when(consultarBitacoraUseCase.ejecutar(skuId)).thenReturn(List.of(entrada));

                mockMvc.perform(get("/api/v1/productos/{skuId}/bitacora", skuId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].campo").value("peso_logistico_kg"))
                                .andExpect(jsonPath("$[0].valorAnterior").value("2.5"))
                                .andExpect(jsonPath("$[0].valorNuevo").value("2.8"));
        }
}