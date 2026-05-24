package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.domain.repository.OperarioServicePort;
import com.distribuidoras.inventario.infrastructure.security.JwtUtil;
import com.distribuidoras.inventario.infrastructure.web.dto.LoginRequest;
import com.distribuidoras.inventario.infrastructure.web.dto.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Map<String, MockUser> MOCK_USERS = Map.of(
            "11111111", new MockUser(1L, "Carlos Perez", "OPERARIO_PICKING"),
            "22222222", new MockUser(2L, "Maria Lopez", "OPERARIO_DESPACHO"),
            "33333333", new MockUser(3L, "Pedro Gomez", "SUPERVISOR_INVENTARIO"),
            "44444444", new MockUser(4L, "Roberto Sanchez", "ASESOR_COMERCIAL"),
            "55555555", new MockUser(5L, "Ana Reception", "OPERARIO_RECEPCION")
    );

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
                            operario.getOperarioId(),
                            operario.getNombre(),
                            operario.getRol().name()
                    );
                    return ResponseEntity.ok(LoginResponse.builder()
                            .token(token)
                            .operarioId(operario.getOperarioId())
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
        MockUser mockUser = MOCK_USERS.get(cc);

        if (mockUser == null) {
            return ResponseEntity.status(401).build();
        }

        String token = jwtUtil.generateToken(
                mockUser.operarioId(),
                mockUser.nombre(),
                mockUser.rol()
        );
        return ResponseEntity.ok(LoginResponse.builder()
                .token(token)
                .operarioId(mockUser.operarioId())
                .nombre(mockUser.nombre())
                .cedula(cc)
                .rol(mockUser.rol())
                .expiresIn(86400L)
                .build());
    }

    private record MockUser(Long operarioId, String nombre, String rol) {}
}
