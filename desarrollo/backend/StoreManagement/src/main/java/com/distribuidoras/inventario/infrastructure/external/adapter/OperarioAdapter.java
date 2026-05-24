package com.distribuidoras.inventario.infrastructure.external.adapter;

import com.distribuidoras.inventario.domain.exception.ExternalServiceException;
import com.distribuidoras.inventario.domain.model.Operario;
import com.distribuidoras.inventario.domain.model.enums.RolUsuario;
import com.distribuidoras.inventario.domain.repository.OperarioServicePort;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class OperarioAdapter implements OperarioServicePort {
    
    private static final Logger log = LoggerFactory.getLogger(OperarioAdapter.class);
    private static final String SERVICE_NAME = "ModuloUsuarios";
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    
    public OperarioAdapter(
            RestTemplate restTemplate,
            @Value("${modulo.usuarios.base-url:http://localhost:8081}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }
    
    @Override
    @CircuitBreaker(name = "moduloUsuarios", fallbackMethod = "fallbackFindByCedula")
    @Retry(name = "moduloUsuarios")
    public Optional<Operario> findByCedula(String cedula) {
        log.info("Consultando operario con CC: {}", cedula);
        
        try {
            String url = baseUrl + "/api/usuarios/operarios/" + cedula;
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, Objects.requireNonNull(HttpMethod.GET), null, new ParameterizedTypeReference<Map<String, Object>>() {});
            
            Map<String, Object> body = response.getBody();
            
            if (response.getStatusCode().is2xxSuccessful() && body != null) {
                String rolStr = (String) body.get("rol");
                RolUsuario rol = rolStr != null ? RolUsuario.valueOf(rolStr.toUpperCase()) : null;
                
                Operario operario = Operario.builder()
                        .operarioId(((Number) body.get("id")).longValue())
                        .nombre((String) body.get("nombre"))
                        .cedula((String) body.get("cedula"))
                        .activo((Boolean) body.getOrDefault("activo", false))
                        .rol(rol)
                        .build();
                
                log.info("Operario encontrado: {} - {} - {}", operario.getCedula(), operario.getNombre(), operario.getRol());
                return Optional.of(operario);
            }
            
            log.warn("Operario no encontrado: {}", cedula);
            return Optional.empty();
            
        } catch (ResourceAccessException e) {
            log.error("Timeout o error de conectividad con {}: {}", SERVICE_NAME, e.getMessage());
            throw new ExternalServiceException(SERVICE_NAME, 
                    "No se pudo conectar con el módulo de usuarios. Intente más tarde.");
        } catch (Exception e) {
            log.error("Error inesperado consultando operario {}: {}", cedula, e.getMessage());
            throw new ExternalServiceException(SERVICE_NAME, 
                    "Error interno al consultar el módulo de usuarios.");
        }
    }
    
    public Optional<Operario> fallbackFindByCedula(String cedula, Exception ex) {
        log.warn("Fallback invocado para operario CC: {} debido a: {}", cedula, ex.getMessage());
        return Optional.empty(); 
    }
    
    @Override
    @CircuitBreaker(name = "moduloUsuarios", fallbackMethod = "fallbackFindById")
    public Optional<Operario> findById(Long id) {
        log.info("Consultando operario con ID: {}", id);
        try {
            String url = baseUrl + "/api/usuarios/operarios/id/" + id;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, Objects.requireNonNull(HttpMethod.GET), null, new ParameterizedTypeReference<Map<String, Object>>() {});
            
            Map<String, Object> body = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && body != null) {
                String rolStr = (String) body.get("rol");
                RolUsuario rol = rolStr != null ? RolUsuario.valueOf(rolStr.toUpperCase()) : null;
                
                return Optional.of(Operario.builder()
                        .operarioId(((Number) body.get("id")).longValue())
                        .nombre((String) body.get("nombre"))
                        .cedula((String) body.get("cedula"))
                        .activo((Boolean) body.getOrDefault("activo", false))
                        .rol(rol)
                        .build());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Error consultando operario por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Operario> fallbackFindById(Long id, Exception ex) {
        log.warn("Fallback findById para ID: {}", id);
        return Optional.empty();
    }

    @Override
    public List<Operario> findByRol(String rol) {
        log.info("Consultando operarios con rol: {}", rol);
        
        try {
            String url = baseUrl + "/api/usuarios/operarios?rol=" + rol;
            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, Objects.requireNonNull(HttpMethod.GET), null, new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            
            List<Map<String, Object>> body = response.getBody();
            
            if (response.getStatusCode().is2xxSuccessful() && body != null) {
                List<Operario> operarios = new ArrayList<>();
                
                for (Map<String, Object> item : body) {
                    String rolStr = (String) item.get("rol");
                    RolUsuario rolEnum = rolStr != null ? RolUsuario.valueOf(rolStr.toUpperCase()) : null;
                    
                    Operario operario = Operario.builder()
                            .operarioId(((Number) item.get("id")).longValue())
                            .nombre((String) item.get("nombre"))
                            .cedula((String) item.get("cedula"))
                            .activo((Boolean) item.getOrDefault("activo", false))
                            .rol(rolEnum)
                            .build();
                    
                    operarios.add(operario);
                }
                
                log.info("Encontrados {} operarios con rol {}", operarios.size(), rol);
                return operarios;
            }
            
            log.warn("No se encontraron operarios con rol: {}", rol);
            return List.of();
            
        } catch (Exception e) {
            log.error("Error consultando operarios por rol {}: {}", rol, e.getMessage());
            return List.of();
        }
    }
}