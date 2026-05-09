package com.distribuidoras.inventario.infrastructure.web.controller;

import com.distribuidoras.inventario.domain.model.Operario;
import com.distribuidoras.inventario.domain.repository.OperarioServicePort;
import com.distribuidoras.inventario.infrastructure.web.dto.OperarioDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operarios")
public class OperarioController {

    private final OperarioServicePort operarioService;

    public OperarioController(OperarioServicePort operarioService) {
        this.operarioService = operarioService;
    }

    @GetMapping("/picking")
    public ResponseEntity<List<OperarioDTO>> listarOperariosPicking() {
        List<Operario> operarios = operarioService.findByRol("OPERARIO_PICKING");
        List<OperarioDTO> response = operarios.stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/despacho")
    public ResponseEntity<List<OperarioDTO>> listarOperariosDespacho() {
        List<Operario> operarios = operarioService.findByRol("OPERARIO_DESPACHO");
        List<OperarioDTO> response = operarios.stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    private OperarioDTO toDTO(Operario operario) {
        return OperarioDTO.builder()
                .id(operario.getOperarioId())
                .nombre(operario.getNombre())
                .cedula(operario.getCedula())
                .rol(operario.getRol() != null ? operario.getRol().name() : null)
                .activo(operario.getActivo())
                .build();
    }
}