package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.*;
import com.distribuidoras.inventario.infrastructure.config.SecurityConfig;
import com.distribuidoras.inventario.infrastructure.web.dto.ExcepcionResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
        Long excId = 1L;
        when(registrarExcepcionUseCase.ejecutar(any())).thenReturn(
                new RegistrarExcepcionUseCase.ExcepcionResultado(
                        excId, "AVERIA", LocalDateTime.now(),
                        new RegistrarExcepcionUseCase.MovimientoInfo(1L, "BAJA_AVERIA", -12)));

        mockMvc.perform(post("/api/v1/excepciones")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull("""
                                {
                                    "tipoExcepcion": "AVERIA",
                                    "skuId": "%s",
                                    "codigoLote": "%s",
                                    "cantidadAfectada": 12,
                                    "descripcion": "Cajas daÃ±adas",
                                    "operarioId": %s
                                }
                                """.formatted("SKU-001", "LOTE-001", 1L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.excepcionId").value(excId.toString()))
                .andExpect(jsonPath("$.movimientoGenerado.cantidad").value(-12));
    }

    @Test
    @DisplayName("POST /api/v1/excepciones sin descripción → 400")
    void registrarExcepcion_sinDescripcion() throws Exception {
        mockMvc.perform(post("/api/v1/excepciones")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull("""
                                {
                                    "tipoExcepcion": "AVERIA",
                                    "skuId": "%s",
                                    "cantidadAfectada": 12,
                                    "descripcion": ""
                                }
                                """.formatted("SKU-001"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/excepciones stock insuficiente → 400")
    void registrarExcepcion_stockInsuficiente() throws Exception {
        when(registrarExcepcionUseCase.ejecutar(any()))
                .thenThrow(new IllegalArgumentException(
                        "Cantidad afectada (50) mayor al stock disponible (10) en lote LOTE-001"));

        mockMvc.perform(post("/api/v1/excepciones")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull("""
                                {
                                    "tipoExcepcion": "AVERIA",
                                    "skuId": "%s",
                                    "codigoLote": "%s",
                                    "cantidadAfectada": 50,
                                    "descripcion": "Mucho daño",
                                    "operarioId": %s
                                }
                                """.formatted("SKU-001", "LOTE-001", 1L))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/excepciones → 200")
    void consultarExcepciones_exitoso() throws Exception {
        Long excId = 2L;
        ExcepcionResponseDTO exc = ExcepcionResponseDTO.builder()
                .excepcionId(excId)
                .tipoExcepcion("AVERIA")
                .codigoLote(null)
                .skuId("SKU-001")
                .cantidadAfectada(10)
                .fechaRegistro(LocalDateTime.now())
                .operarioId(1L)
                .operarioNombre("Carlos Perez")
                .operarioCedula("11111111")
                .descripcion("Daño")
                .evidenciaUrl(null)
                .build();
        when(consultarExcepcionesUseCase.ejecutar(null, null)).thenReturn(Objects.requireNonNull(List.of(exc)));

        mockMvc.perform(get("/api/v1/excepciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].excepcionId").value(excId.toString()))
                .andExpect(jsonPath("$[0].operarioNombre").value("Carlos Perez"))
                .andExpect(jsonPath("$[0].operarioCedula").value("11111111"));
    }

    @Test
    @DisplayName("GET /api/v1/excepciones/{id} → 200")
    void consultarDetalle_exitoso() throws Exception {
        Long excId = 3L;
        ExcepcionResponseDTO exc = ExcepcionResponseDTO.builder()
                .excepcionId(excId)
                .tipoExcepcion("AVERIA")
                .codigoLote(null)
                .skuId("SKU-001")
                .cantidadAfectada(10)
                .fechaRegistro(LocalDateTime.now())
                .operarioId(1L)
                .operarioNombre("Maria Lopez")
                .operarioCedula("22222222")
                .descripcion("Daño")
                .evidenciaUrl(null)
                .build();
        when(consultarDetalleUseCase.ejecutar(excId)).thenReturn(exc);

        mockMvc.perform(get("/api/v1/excepciones/{id}", excId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.excepcionId").value(excId.toString()))
                .andExpect(jsonPath("$.operarioNombre").value("Maria Lopez"))
                .andExpect(jsonPath("$.operarioCedula").value("22222222"));
    }
}
