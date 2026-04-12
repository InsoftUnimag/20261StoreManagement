package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.DetalleManifiesto;
import com.distribuidoras.inventario.domain.model.Manifiesto;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.DetalleManifiestoRepository;
import com.distribuidoras.inventario.domain.repository.ManifiestoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case: Listar Manifiestos disponibles.
 * Spec 12: Listar Manifiesto
 * 
 * FR-025: Listar manifiestos con su información
 * FR-091: Filtrar manifiestos por fecha
 * FR-092: Ver información detallada de cada manifiesto
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
            String estado
    ) {}

    /**
     * Lista manifiestos con filtros opcionales.
     */
    @Transactional(readOnly = true)
    public List<ManifiestoResumenDTO> ejecutar(FiltrosManifiestoDTO filtros) {
        log.info("Listando manifiestos con filtros: fechaDesde={}, fechaHasta={}, estado={}",
                filtros.fechaDesde(), filtros.fechaHasta(), filtros.estado());

        List<Manifiesto> manifiestos = manifiestoRepository.findPendientes();

        // Aplicar filtros funcionales
        return manifiestos.stream()
                .filter(m -> filtros.fechaDesde() == null || !m.getFechaEmision().isBefore(filtros.fechaDesde()))
                .filter(m -> filtros.fechaHasta() == null || !m.getFechaEmision().isAfter(filtros.fechaHasta()))
                .filter(m -> filtros.estado() == null || m.getEstado().name().equals(filtros.estado()))
                .sorted((m1, m2) -> m2.getFechaEmision().compareTo(m1.getFechaEmision()))
                .map(this::toManifiestoResumenDTO)
                .toList();
    }

    /**
     * Obtiene detalle completo de un manifiesto.
     */
    @Transactional(readOnly = true)
    public ManifiestoDetalleDTO obtenerDetalle(UUID manifiestoId) {
        log.info("Obteniendo detalle del manifiesto: {}", manifiestoId);

        Manifiesto manifiesto = manifiestoRepository.findById(manifiestoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Manifiesto no encontrado: " + manifiestoId));

        List<DetalleManifiesto> detalles = detalleManifiestoRepository.findByManifiestoId(manifiestoId);
        
        Map<UUID, Producto> productos = detalles.stream()
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

    private ManifiestoResumenDTO toManifiestoResumenDTO(Manifiesto m) {
        return ManifiestoResumenDTO.builder()
                .manifiestoId(m.getManifiestoId().toString())
                .numeroManifiesto(m.getNumeroManifiesto())
                .fechaEmision(m.getFechaEmision().atStartOfDay())
                .proveedor(m.getProveedor())
                .estado(m.getEstado().name())
                .build();
    }

    private DetalleManifiestoLineaDTO toDetalleLineaDTO(DetalleManifiesto detalle, Producto producto) {
        return DetalleManifiestoLineaDTO.builder()
                .detalleId(detalle.getDetalleId().toString())
                .skuId(detalle.getSkuId().toString())
                .marca(producto != null ? producto.getMarca() : "N/A")
                .presentacion(producto != null ? producto.getPresentacion() : "N/A")
                .cantidadEsperada(detalle.getCantidadEsperada())
                .cantidadRecibida(detalle.getCantidadRecibida())
                .build();
    }

    public record ManifiestoResumenDTO(
            String manifiestoId,
            String numeroManifiesto,
            LocalDateTime fechaEmision,
            String proveedor,
            String estado
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String manifiestoId;
            private String numeroManifiesto;
            private java.time.LocalDateTime fechaEmision;
            private String proveedor;
            private String estado;

            public Builder manifiestoId(String manifiestoId) { this.manifiestoId = manifiestoId; return this; }
            public Builder numeroManifiesto(String numeroManifiesto) { this.numeroManifiesto = numeroManifiesto; return this; }
            public Builder fechaEmision(java.time.LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; return this; }
            public Builder proveedor(String proveedor) { this.proveedor = proveedor; return this; }
            public Builder estado(String estado) { this.estado = estado; return this; }

            public ManifiestoResumenDTO build() {
                return new ManifiestoResumenDTO(manifiestoId, numeroManifiesto, fechaEmision, proveedor, estado);
            }
        }
    }

    public record ManifiestoDetalleDTO(
            String manifiestoId,
            String numeroManifiesto,
            LocalDateTime fechaEmision,
            String proveedor,
            String estado,
            List<DetalleManifiestoLineaDTO> lineas
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String manifiestoId;
            private String numeroManifiesto;
            private LocalDateTime fechaEmision;
            private String proveedor;
            private String estado;
            private List<DetalleManifiestoLineaDTO> lineas;

            public Builder manifiestoId(String manifiestoId) { this.manifiestoId = manifiestoId; return this; }
            public Builder numeroManifiesto(String numeroManifiesto) { this.numeroManifiesto = numeroManifiesto; return this; }
            public Builder fechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; return this; }
            public Builder proveedor(String proveedor) { this.proveedor = proveedor; return this; }
            public Builder estado(String estado) { this.estado = estado; return this; }
            public Builder lineas(List<DetalleManifiestoLineaDTO> lineas) { this.lineas = lineas; return this; }

            public ManifiestoDetalleDTO build() {
                return new ManifiestoDetalleDTO(manifiestoId, numeroManifiesto, fechaEmision, proveedor, estado, lineas);
            }
        }
    }

    public record DetalleManifiestoLineaDTO(
            String detalleId,
            String skuId,
            String marca,
            String presentacion,
            Integer cantidadEsperada,
            Integer cantidadRecibida
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String detalleId;
            private String skuId;
            private String marca;
            private String presentacion;
            private Integer cantidadEsperada;
            private Integer cantidadRecibida;

            public Builder detalleId(String detalleId) { this.detalleId = detalleId; return this; }
            public Builder skuId(String skuId) { this.skuId = skuId; return this; }
            public Builder marca(String marca) { this.marca = marca; return this; }
            public Builder presentacion(String presentacion) { this.presentacion = presentacion; return this; }
            public Builder cantidadEsperada(Integer cantidadEsperada) { this.cantidadEsperada = cantidadEsperada; return this; }
            public Builder cantidadRecibida(Integer cantidadRecibida) { this.cantidadRecibida = cantidadRecibida; return this; }

            public DetalleManifiestoLineaDTO build() {
                return new DetalleManifiestoLineaDTO(detalleId, skuId, marca, presentacion, cantidadEsperada, cantidadRecibida);
            }
        }
    }
}
