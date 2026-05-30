package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Operario;
import com.distribuidoras.inventario.domain.model.Recepcion;
import com.distribuidoras.inventario.domain.repository.OperarioServicePort;
import com.distribuidoras.inventario.domain.repository.RecepcionRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.RecepcionResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ListarRecepcionesUseCase {

    private final RecepcionRepository recepcionRepository;
    private final OperarioServicePort operarioServicePort;

    public ListarRecepcionesUseCase(RecepcionRepository recepcionRepository,
                                     OperarioServicePort operarioServicePort) {
        this.recepcionRepository = recepcionRepository;
        this.operarioServicePort = operarioServicePort;
    }

    @Transactional(readOnly = true)
    public List<RecepcionResponseDTO> ejecutar() {
        return recepcionRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    private RecepcionResponseDTO toDTO(Recepcion r) {
        Optional<Operario> op = r.getOperarioId() != null
                ? operarioServicePort.findById(r.getOperarioId())
                : Optional.empty();

        return RecepcionResponseDTO.builder()
                .recepcionId(r.getRecepcionId())
                .manifiestoId(r.getManifiestoId())
                .operarioId(r.getOperarioId())
                .operarioNombre(op.map(Operario::getNombre).orElse(null))
                .operarioCedula(op.map(Operario::getCedula).orElse(null))
                .fechaRecepcion(r.getFechaRecepcion())
                .notas(r.getNotas())
                .numeroRecepcion(r.getNumeroRecepcion())
                .build();
    }
}
