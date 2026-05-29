package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ExcepcionNotFoundException;
import com.distribuidoras.inventario.domain.model.ExcepcionInventario;
import com.distribuidoras.inventario.domain.model.Operario;
import com.distribuidoras.inventario.domain.repository.ExcepcionInventarioRepository;
import com.distribuidoras.inventario.domain.repository.OperarioServicePort;
import com.distribuidoras.inventario.infrastructure.web.dto.ExcepcionResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ConsultarDetalleExcepcionUseCase {

    private final ExcepcionInventarioRepository excepcionRepository;
    private final OperarioServicePort operarioServicePort;

    public ConsultarDetalleExcepcionUseCase(ExcepcionInventarioRepository excepcionRepository,
                                             OperarioServicePort operarioServicePort) {
        this.excepcionRepository = excepcionRepository;
        this.operarioServicePort = operarioServicePort;
    }

    @Transactional(readOnly = true)
    public ExcepcionResponseDTO ejecutar(Long excepcionId) {
        ExcepcionInventario e = excepcionRepository.findById(excepcionId)
                .orElseThrow(() -> new ExcepcionNotFoundException(excepcionId));

        Optional<Operario> op = e.getOperarioId() != null
                ? operarioServicePort.findById(e.getOperarioId())
                : Optional.empty();

        return ExcepcionResponseDTO.builder()
                .excepcionId(e.getExcepcionId())
                .tipoExcepcion(e.getTipoExcepcion().name())
                .codigoLote(e.getCodigoLote())
                .skuId(e.getSkuId())
                .cantidadAfectada(e.getCantidadAfectada())
                .fechaRegistro(e.getFechaRegistro())
                .operarioId(e.getOperarioId())
                .operarioNombre(op.map(Operario::getNombre).orElse(null))
                .operarioCedula(op.map(Operario::getCedula).orElse(null))
                .descripcion(e.getDescripcion())
                .evidenciaUrl(e.getEvidenciaUrl())
                .build();
    }
}
