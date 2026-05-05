package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.DetalleManifiesto;
import com.distribuidoras.inventario.domain.model.Manifiesto;
import com.distribuidoras.inventario.domain.repository.DetalleManifiestoRepository;
import com.distribuidoras.inventario.domain.repository.ManifiestoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: Listar Manifiestos Pendientes.
 * Solo manifiestos con estado PENDIENTE o RECEPCIONADO_PARCIAL.
 */
@Service
public class ListarManifiestosPendientesUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListarManifiestosPendientesUseCase.class);

    private final ManifiestoRepository manifiestoRepository;
    private final DetalleManifiestoRepository detalleManifiestoRepository;

    public ListarManifiestosPendientesUseCase(ManifiestoRepository manifiestoRepository,
                                              DetalleManifiestoRepository detalleManifiestoRepository) {
        this.manifiestoRepository = manifiestoRepository;
        this.detalleManifiestoRepository = detalleManifiestoRepository;
    }

    @Transactional(readOnly = true)
    public List<ManifiestoResumen> ejecutar() {
        List<Manifiesto> pendientes = manifiestoRepository.findPendientes();

        log.info("Manifiestos pendientes: {}", pendientes.size());

        return pendientes.stream().map(m -> {
            List<DetalleManifiesto> detalles = detalleManifiestoRepository.findByManifiestoId(m.getManifiestoId());
            long completadas = detalles.stream()
                    .filter(d -> d.getCantidadRecibida() >= d.getCantidadEsperada())
                    .count();
            return new ManifiestoResumen(
                    m.getManifiestoId(), m.getNumeroManifiesto(), m.getProveedor(),
                    m.getFechaEmision(), m.getEstado().name(),
                    (int) completadas, detalles.size());
        }).toList();
    }

    public record ManifiestoResumen(UUID manifiestoId, String numeroManifiesto, String proveedor,
                                     java.time.LocalDate fechaEmision, String estado,
                                     int lineasCompletadas, int lineasTotales) {}
}
