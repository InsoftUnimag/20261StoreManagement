package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.*;
import com.distribuidoras.inventario.domain.model.ExcepcionInventario;
import com.distribuidoras.inventario.domain.model.enums.TipoExcepcion;
import com.distribuidoras.inventario.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExcepcionController.class)
@Import(SecurityConfig.class)
class ExcepcionControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private RegistrarExcepcionUseCase registrarExcepcionUseCase;
    @MockitoBean private ConsultarExcepcionesUseCase consultarExcepcionesUseCase;
    @MockitoBean private ConsultarDetalleExcepcionUseCase consultarDetalleUseCase;

    @Test
    @DisplayName("POST /api/v1/excepciones exitoso → 201")
    void registrarExcepcion_exitoso() throws Exception {
        UUID excId = UUID.randomUUID();
        when(registrarExcepcionUseCase.ejecutar(any())).thenReturn(
                new RegistrarExcepcionUseCase.ExcepcionResultado(
                        excId, "AVERIA", LocalDateTime.now(),
                        new RegistrarExcepcionUseCase.MovimientoInfo(UUID.randomUUID(), "BAJA_AVERIA", -12)));

        mockMvc.perform(post("/api/v1/excepciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tipoExcepcion": "AVERIA",
                                    "skuId": "%s",
                                    "codigoLote": "%s",
                                    "cantidadAfectada": 12,
                                    "descripcion": "Cajas dañadas"
                                }
                                """.formatted(UUID.randomUUID(), "LOTE-001")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.excepcionId").value(excId.toString()))
                .andExpect(jsonPath("$.movimientoGenerado.cantidad").value(-12));
    }

    @Test
    @DisplayName("POST /api/v1/excepciones sin descripción → 400")
    void registrarExcepcion_sinDescripcion() throws Exception {
        mockMvc.perform(post("/api/v1/excepciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tipoExcepcion": "AVERIA",
                                    "skuId": "%s",
                                    "cantidadAfectada": 12,
                                    "descripcion": ""
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/excepciones stock insuficiente → 409")
    void registrarExcepcion_stockInsuficiente() throws Exception {
        when(registrarExcepcionUseCase.ejecutar(any()))
                .thenThrow(new IllegalArgumentException(
                        "Cantidad afectada (50) mayor al stock disponible (10) en lote LOTE-001"));

        mockMvc.perform(post("/api/v1/excepciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tipoExcepcion": "AVERIA",
                                    "skuId": "%s",
                                    "codigoLote": "%s",
                                    "cantidadAfectada": 50,
                                    "descripcion": "Mucho daño"
                                }
                                """.formatted(UUID.randomUUID(), "LOTE-001")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/excepciones → 200")
    void consultarExcepciones_exitoso() throws Exception {
        UUID excId = UUID.randomUUID();
        ExcepcionInventario exc = new ExcepcionInventario(excId, TipoExcepcion.AVERIA,
                null, UUID.randomUUID(), 10, LocalDateTime.now(), UUID.randomUUID(),
                "Daño", null);
        when(consultarExcepcionesUseCase.ejecutar(null, null)).thenReturn(List.of(exc));

        mockMvc.perform(get("/api/v1/excepciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].excepcionId").value(excId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/excepciones/{id} → 200")
    void consultarDetalle_exitoso() throws Exception {
        UUID excId = UUID.randomUUID();
        ExcepcionInventario exc = new ExcepcionInventario(excId, TipoExcepcion.AVERIA,
                null, UUID.randomUUID(), 10, LocalDateTime.now(), UUID.randomUUID(),
                "Daño", null);
        when(consultarDetalleUseCase.ejecutar(excId)).thenReturn(exc);

        mockMvc.perform(get("/api/v1/excepciones/{id}", excId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.excepcionId").value(excId.toString()));
    }
}
