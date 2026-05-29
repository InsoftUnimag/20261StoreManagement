package com.distribuidoras.inventario.infrastructure.external.adapter;

import com.distribuidoras.inventario.domain.exception.ExternalServiceException;
import com.distribuidoras.inventario.domain.model.Cliente;
import com.distribuidoras.inventario.domain.repository.ClienteServicePort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Adapter for external User Module service.
 * Implements ClienteServicePort to consult client data by CC/NIT.
 * FR-042, SC-021 (≤ 2 seconds)
 */
@Component
public class ModuloUsuariosAdapter implements ClienteServicePort {

    private static final Logger log = LoggerFactory.getLogger(ModuloUsuariosAdapter.class);
    private static final String SERVICE_NAME = "ModuloUsuarios";

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ModuloUsuariosAdapter(
            RestTemplate restTemplate,
            @Value("${modulo.usuarios.base-url:http://localhost:8081}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    @CircuitBreaker(name = "moduloUsuarios", fallbackMethod = "fallbackFindByCedula")
    @Retry(name = "moduloUsuarios")
    public Optional<Cliente> findByCedula(String cedula) {
        log.info("Consultando cliente con CC: {}", cedula);

        try {
            String url = baseUrl + "/api/usuarios/clientes/" + cedula;

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, Objects.requireNonNull(HttpMethod.GET), null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            Map<String, Object> body = response.getBody();

            if (response.getStatusCode().is2xxSuccessful() && body != null) {
                Cliente cliente = Cliente.builder()
                        .cedula((String) body.get("cedula"))
                        .nombre((String) body.get("nombre"))
                        .telefono((String) body.get("telefono"))
                        .email((String) body.get("email"))
                        .direccion((String) body.get("direccion"))
                        .activo((Boolean) body.getOrDefault("activo", false))
                        .build();

                log.info("Cliente encontrado: {} - {}", cliente.getCedula(), cliente.getNombre());
                return Optional.of(cliente);
            }

            log.warn("Cliente no encontrado: {}", cedula);
            return Optional.empty();

        } catch (ResourceAccessException e) {
            log.error("Timeout o error de conectividad con {}: {}", SERVICE_NAME, e.getMessage());
            throw new ExternalServiceException(SERVICE_NAME,
                    "No se pudo conectar con el módulo de usuarios. Intente más tarde.");
        } catch (Exception e) {
            log.error("Error inesperado consultando cliente {}: {}", cedula, e.getMessage());
            throw new ExternalServiceException(SERVICE_NAME,
                    "Error interno al consultar el módulo de usuarios.");
        }
    }

    public Optional<Cliente> fallbackFindByCedula(String cedula, Exception ex) {
        log.warn("Módulo de usuarios no disponible para CC: {}. Motivo: {}", cedula, ex.getMessage());
        return Optional.empty();
    }
}
