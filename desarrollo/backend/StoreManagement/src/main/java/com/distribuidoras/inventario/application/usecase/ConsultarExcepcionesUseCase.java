package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.ExcepcionInventario;
import com.distribuidoras.inventario.domain.model.Operario;
import com.distribuidoras.inventario.domain.model.enums.TipoExcepcion;
import com.distribuidoras.inventario.domain.repository.ExcepcionInventarioRepository;
import com.distribuidoras.inventario.domain.repository.OperarioServicePort;
import com.distribuidoras.inventario.infrastructure.web.dto.ExcepcionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultarExcepcionesUseCase {

    private final ExcepcionInventarioRepository excepcionRepository;
    private final OperarioServicePort operarioServicePort;

    public ConsultarExcepcionesUseCase(ExcepcionInventarioRepository excepcionRepository,
                                        OperarioServicePort operarioServicePort) {
        this.excepcionRepository = excepcionRepository;
        this.operarioServicePort = operarioServicePort;
    }

    @Transactional(readOnly = true)
    public List<ExcepcionResponseDTO> ejecutar(TipoExcepcion tipo, String skuId) {
        List<ExcepcionInventario> list;
        if (tipo == null && skuId == null) {
            list = excepcionRepository.findAll();
        } else {
            list = excepcionRepository.findByFilters(tipo, skuId);
        }
        return list.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ExcepcionResponseDTO> ejecutar(LocalDateTime desde) {
        return excepcionRepository.findByFechaRegistroAfter(desde)
                .stream().map(this::toDTO).toList();
    }

    public List<ExcepcionResponseDTO> ejecutar(int minutos) {
        LocalDateTime desde = LocalDateTime.now().minusMinutes(minutos);
        return ejecutar(desde);
    }

    @Transactional(readOnly = true)
    public Page<ExcepcionResponseDTO> ejecutarConPaginacion(
            TipoExcepcion tipo, String skuId,
            LocalDateTime desde, LocalDateTime hasta,
            Pageable pageable) {
        return excepcionRepository.findByFiltersWithPagination(tipo, skuId, desde, hasta, pageable)
                .map(this::toDTO);
    }

    private ExcepcionResponseDTO toDTO(ExcepcionInventario e) {
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
