package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.ConsultarClienteUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for client consultation (proxy to User Module).
 * Spec 07: Consultar Datos de Cliente
 */
@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    private final ConsultarClienteUseCase consultarClienteUseCase;

    public ClienteController(ConsultarClienteUseCase consultarClienteUseCase) {
        this.consultarClienteUseCase = consultarClienteUseCase;
    }

    /**
     * GET /api/v1/clientes/{cedula}
     * Consultar datos de cliente por CC/NIT.
     */
    @GetMapping("/{cedula}")
    public ResponseEntity<Map<String, Object>> consultarCliente(@PathVariable String cedula) {
        log.info("REST: Consultando cliente con CC (MOCK) {}", cedula);

        Map<String, Object> response = Map.of(
                "cedula", cedula,
                "nombre", "Cliente Simulado " + cedula,
                "telefono", "3001234567",
                "email", "cliente" + cedula + "@simulado.com",
                "direccion", "Calle Falsa 123",
                "activo", true);

        return ResponseEntity.ok(response);
    }
}
