package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.RegistrarRecepcionUseCase;
import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecepcionController.class)
@Import(SecurityConfig.class)
class RecepcionControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private RegistrarRecepcionUseCase registrarRecepcionUseCase;

    @Test
    @DisplayName("POST /api/v1/recepciones exitoso → 201")
    void registrarRecepcion_exitoso() throws Exception {
        UUID recId = UUID.randomUUID();
        UUID skuId = UUID.randomUUID();

        when(registrarRecepcionUseCase.ejecutar(any())).thenReturn(
                new RegistrarRecepcionUseCase.RecepcionResult(
                        recId, LocalDateTime.now(),
                        List.of(new RegistrarRecepcionUseCase.LoteResult("LOT-001", skuId, "LOT-001", 240)),
                        List.of()));

        mockMvc.perform(post("/api/v1/recepciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "operarioId": "%s",
                                    "lineasRecepcion": [
                                        {
                                            "skuId": "%s",
                                            "codigoLote": "LOT-001",
                                            "fechaVencimiento": "%s",
                                            "cantidadRecibida": 240
                                        }
                                    ]
                                }
                                """.formatted(UUID.randomUUID(), skuId,
                                LocalDate.now().plusMonths(6))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recepcionId").value(recId.toString()))
                .andExpect(jsonPath("$.lotesCreados[0].cantidad").value(240));
    }

    @Test
    @DisplayName("POST /api/v1/recepciones sin operario → 400")
    void registrarRecepcion_sinOperario() throws Exception {
        mockMvc.perform(post("/api/v1/recepciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "lineasRecepcion": [
                                        {
                                            "skuId": "%s",
                                            "codigoLote": "LOT-001",
                                            "fechaVencimiento": "%s",
                                            "cantidadRecibida": 240
                                        }
                                    ]
                                }
                                """.formatted(UUID.randomUUID(), LocalDate.now().plusMonths(6))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/recepciones SKU no existe → 404")
    void registrarRecepcion_skuNoExiste() throws Exception {
        when(registrarRecepcionUseCase.ejecutar(any()))
                .thenThrow(new ProductoNotFoundException(UUID.randomUUID()));

        mockMvc.perform(post("/api/v1/recepciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "operarioId": "%s",
                                    "lineasRecepcion": [
                                        {
                                            "skuId": "%s",
                                            "codigoLote": "LOT-001",
                                            "fechaVencimiento": "%s",
                                            "cantidadRecibida": 240
                                        }
                                    ]
                                }
                                """.formatted(UUID.randomUUID(), UUID.randomUUID(),
                                LocalDate.now().plusMonths(6))))
                .andExpect(status().isNotFound());
    }
}
