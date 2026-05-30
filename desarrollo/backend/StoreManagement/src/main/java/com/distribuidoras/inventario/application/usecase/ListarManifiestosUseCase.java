package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.DetalleManifiesto;
import com.distribuidoras.inventario.domain.model.Manifiesto;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.DetalleManifiestoRepository;
import com.distribuidoras.inventario.domain.repository.ManifiestoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.DetalleManifiestoLineaDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.ManifiestoDetalleDTO;
import com.distribuidoras.inventario.infrastructure.web.dto.ManifiestoResumenDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

/**
 * Use Case: Listar Manifiestos disponibles.
 * Spec 12: Listar Manifiesto
 */
@Service
public class ListarManifiestosUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListarManifiestosUseCase.class);

    private final ManifiestoRepository manifiestoRepository;
    private final DetalleManifiestoRepository detalleManifiestoRepository;
    private final ProductoRepository productoRepository;

    public ListarManifiestosUseCase(ManifiestoRepository manifiestoRepository,
            DetalleManifiestoRepository detalleManifiestoRepository,
            ProductoRepository productoRepository) {
        this.manifiestoRepository = manifiestoRepository;
        this.detalleManifiestoRepository = detalleManifiestoRepository;
        this.productoRepository = productoRepository;
    }

    public record FiltrosManifiestoDTO(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String estado,
            boolean incluirHistorico) {
    }

    @Transactional(readOnly = true)
    public List<ManifiestoResumenDTO> ejecutar(FiltrosManifiestoDTO filtros) {
        log.info("Listando manifiestos con filtros: fechaDesde={}, fechaHasta={}, estado={}, incluirHistorico={}",
                filtros.fechaDesde(), filtros.fechaHasta(), filtros.estado(), filtros.incluirHistorico());

        List<Manifiesto> manifiestos;

        if (filtros.incluirHistorico() || filtros.fechaDesde() != null || filtros.fechaHasta() != null) {
            if (filtros.fechaDesde() != null && filtros.fechaHasta() != null) {
                manifiestos = manifiestoRepository.findByFechaEmisionBetweenWithLimit(
                        filtros.fechaDesde(), filtros.fechaHasta(), 100);
            } else {
                LocalDate hoy = LocalDate.now();
                manifiestos = manifiestoRepository.findByFechaEmisionBetweenWithLimit(
                        hoy.minusMonths(1), hoy, 100);
            }
        } else {
            manifiestos = manifiestoRepository.findPendientes();
        }

        List<Long> ids = manifiestos.stream().map(Manifiesto::getManifiestoId).toList();
        Map<Long, List<DetalleManifiesto>> detallesMap = detalleManifiestoRepository.findByManifiestoIds(ids);

        return manifiestos.stream()
                .filter(m -> filtros.estado() == null || m.getEstado().name().equals(filtros.estado()))
                .sorted((m1, m2) -> m2.getFechaEmision().compareTo(m1.getFechaEmision()))
                .map(m -> toManifiestoResumenDTO(m, detallesMap.get(m.getManifiestoId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ManifiestoDetalleDTO obtenerDetalle(Long manifiestoId) {
        log.info("Obteniendo detalle del manifiesto: {}", manifiestoId);

        Manifiesto manifiesto = manifiestoRepository.findById(manifiestoId)
                .orElseThrow(() -> new IllegalArgumentException("Manifiesto no encontrado: " + manifiestoId));

        List<DetalleManifiesto> detalles = detalleManifiestoRepository.findByManifiestoId(manifiestoId);

        Map<String, Producto> productos = detalles.stream()
                .map(DetalleManifiesto::getSkuId)
                .distinct()
                .map(skuId -> Map.entry(skuId, productoRepository.findById(skuId)))
                .filter(e -> e.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));

        List<DetalleManifiestoLineaDTO> lineas = detalles.stream()
                .map(detalle -> toDetalleLineaDTO(detalle, productos.get(detalle.getSkuId())))
                .toList();

        return ManifiestoDetalleDTO.builder()
                .manifiestoId(manifiesto.getManifiestoId().toString())
                .numeroManifiesto(manifiesto.getNumeroManifiesto())
                .fechaEmision(manifiesto.getFechaEmision().atStartOfDay())
                .proveedor(manifiesto.getProveedor())
                .estado(manifiesto.getEstado().name())
                .lineas(lineas)
                .build();
    }

    private ManifiestoResumenDTO toManifiestoResumenDTO(Manifiesto m, List<DetalleManifiesto> detalles) {
        int total = detalles != null ? detalles.size() : 0;
        int recibidas = detalles != null
                ? (int) detalles.stream().filter(d -> d.getCantidadRecibida() > 0).count()
                : 0;
        int sumEsperado = detalles != null
                ? detalles.stream().filter(d -> d.getCantidadEsperada() != null).mapToInt(DetalleManifiesto::getCantidadEsperada).sum()
                : 0;
        int sumRecibido = detalles != null
                ? detalles.stream().filter(d -> d.getCantidadRecibida() != null).mapToInt(DetalleManifiesto::getCantidadRecibida).sum()
                : 0;


        return new ManifiestoResumenDTO(
                m.getManifiestoId().toString(),
                m.getNumeroManifiesto(),
                m.getFechaEmision().atStartOfDay(),
                m.getProveedor(),
                m.getEstado().name(),
                total,
                recibidas,
                m.getCreadoEl(),
                sumEsperado,
                sumRecibido);
    }

    private DetalleManifiestoLineaDTO toDetalleLineaDTO(DetalleManifiesto detalle, Producto producto) {
        return DetalleManifiestoLineaDTO.builder()
                .detalleId(detalle.getDetalleId().toString())
                .skuId(detalle.getSkuId())
                .marca(producto != null ? producto.getMarca() : "N/A")
                .presentacion(producto != null ? producto.getPresentacion() : "N/A")
                .contenidoMl(producto != null ? producto.getContenidoMl() : null)
                .cantidadEsperada(detalle.getCantidadEsperada())
                .cantidadRecibida(detalle.getCantidadRecibida())
                .build();
    }

}
