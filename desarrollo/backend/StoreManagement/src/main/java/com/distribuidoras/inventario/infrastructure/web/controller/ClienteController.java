package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.application.usecase.ConsultarClienteUseCase;
import com.distribuidoras.inventario.domain.exception.ClienteInactivoException;
import com.distribuidoras.inventario.domain.exception.ClienteNotFoundException;
import com.distribuidoras.inventario.domain.model.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    private final ConsultarClienteUseCase consultarClienteUseCase;

    public ClienteController(ConsultarClienteUseCase consultarClienteUseCase) {
        this.consultarClienteUseCase = consultarClienteUseCase;
    }

    @GetMapping("/{cedula}")
    public ResponseEntity<Cliente> consultarCliente(@PathVariable String cedula) {
        log.info("REST: Consultando cliente con CC {}", cedula);

        try {
            Cliente cliente = consultarClienteUseCase.ejecutar(cedula);
            return ResponseEntity.ok(cliente);
        } catch (ClienteNotFoundException e) {
            log.warn("Cliente no encontrado: {}", cedula);
            return ResponseEntity.notFound().build();
        } catch (ClienteInactivoException e) {
            log.warn("Cliente inactivo: {}", cedula);
            return ResponseEntity.badRequest().build();
        }
    }
}
