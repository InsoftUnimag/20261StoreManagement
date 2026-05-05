package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.domain.repository.OperarioServicePort;
import com.distribuidoras.inventario.infrastructure.security.JwtUtil;
import com.distribuidoras.inventario.infrastructure.web.dto.LoginRequest;
import com.distribuidoras.inventario.infrastructure.web.dto.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final OperarioServicePort operarioService;
    private final JwtUtil jwtUtil;

    public AuthController(OperarioServicePort operarioService, JwtUtil jwtUtil) {
        this.operarioService = operarioService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String cc = request.getCedula();
        
        return operarioService.findByCedula(cc)
                .filter(operario -> operario.getActivo())
                .map(operario -> {
                    String token = jwtUtil.generateToken(
                            operario.getId(),
                            operario.getNombre(),
                            operario.getRol().name()
                    );
                    return ResponseEntity.ok(LoginResponse.builder()
                            .token(token)
                            .nombre(operario.getNombre())
                            .cedula(operario.getCedula())
                            .rol(operario.getRol().name())
                            .expiresIn(86400L)
                            .build());
                })
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/mock-login")
    public ResponseEntity<LoginResponse> mockLogin(@RequestBody LoginRequest request) {
        String cc = request.getCedula();
        String rol = "";
        String nombre = "";

        switch (cc) {
            case "11111111": rol = "OPERARIO_PICKING"; nombre = "Carlos Perez"; break;
            case "22222222": rol = "OPERARIO_DESPACHO"; nombre = "Maria Lopez"; break;
            case "33333333": rol = "SUPERVISOR_INVENTARIO"; nombre = "Pedro Gomez"; break;
            case "44444444": rol = "ASESOR_COMERCIAL"; nombre = "Roberto Sanchez"; break;
            case "55555555": rol = "OPERARIO_RECEPCION"; nombre = "Ana Reception"; break;
            default: return ResponseEntity.status(401).build();
        }

        String token = jwtUtil.generateToken(
                java.util.UUID.randomUUID(),
                nombre,
                rol
        );
        return ResponseEntity.ok(LoginResponse.builder()
                .token(token)
                .nombre(nombre)
                .cedula(cc)
                .rol(rol)
                .expiresIn(86400L)
                .build());
    }
}